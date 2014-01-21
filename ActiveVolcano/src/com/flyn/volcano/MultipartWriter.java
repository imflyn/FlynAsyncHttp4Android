package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.flyn.volcano.SpendTimer.TimerListener;

public class MultipartWriter
{
    private static final String          TAG                      = MultipartWriter.class.getName();
    private static final String          APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final byte[]          CR_LF                    = ("\r\n").getBytes();
    private static final byte[]          TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary\r\n".getBytes();
    private static final char[]          MULTIPART_CHARS          = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int             DEFAULT_BUFFER_SIZE      = 8192;

    private String                       boundary;
    private byte[]                       boundaryLine;
    private byte[]                       boundaryEnd;

    private List<FilePart>               fileParts                = new LinkedList<FilePart>();
    private SpendTimer                   timer;

    private PoolingByteArrayOutputStream out;
    private ByteArrayPool                mPool;

    private final ResponseDelivery       responseDelivery;
    private final Request<?>             request;
    private final HttpURLConnection      connection;

    public MultipartWriter(Request<?> request, HttpURLConnection connection, ResponseDelivery responseDelivery)
    {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++)
        {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        this.boundary = buf.toString();
        this.boundaryLine = ("--" + this.boundary + "\r\n").getBytes();
        this.boundaryEnd = ("--" + this.boundary + "--\r\n").getBytes();

        this.responseDelivery = responseDelivery;
        this.connection = connection;
        this.request = request;

        this.mPool = new ByteArrayPool(DEFAULT_BUFFER_SIZE);
        this.out = new PoolingByteArrayOutputStream(this.mPool);

    }

    public void addPart(final String key, final String value, final String contentType)
    {

        try
        {
            this.out.write(this.boundaryLine);
            this.out.write(createContentDisposition(key));
            this.out.write(createContentType(contentType));
            this.out.write(CR_LF);
            this.out.write(value.getBytes());
            this.out.write(CR_LF);
        } catch (final IOException e)
        {
            Log.e(TAG, "addPart ByteArrayOutputStream exception", e);
        }
    }

    public void addPart(final String key, final String value)
    {
        addPart(key, value, "text/plain; charset=UTF-8");
    }

    public void addPart(String key, File file)
    {
        addPart(key, file, null);
    }

    public void addPart(final String key, File file, String type)
    {
        if (type == null)
        {
            type = APPLICATION_OCTET_STREAM;
        }
        this.fileParts.add(new FilePart(key, file, type));
    }

    public void addPart(String key, String streamName, InputStream inputStream, String type) throws IOException
    {
        if (type == null)
        {
            type = APPLICATION_OCTET_STREAM;
        }
        this.out.write(this.boundaryLine);

        // Headers
        this.out.write(createContentDisposition(key, streamName));
        this.out.write(createContentType(type));
        this.out.write(TRANSFER_ENCODING_BINARY);
        this.out.write(CR_LF);

        // Stream (file)
        final byte[] tmp = new byte[DEFAULT_BUFFER_SIZE];
        int l;
        while ((l = inputStream.read(tmp)) != -1)
        {
            this.out.write(tmp, 0, l);
        }

        this.out.write(CR_LF);
        this.out.flush();

        Utils.quickClose(inputStream);
    }

    private byte[] createContentType(String type)
    {
        String result = "Content-Type: " + type + "\r\n";
        return result.getBytes();
    }

    private byte[] createContentDisposition(final String key)
    {
        return ("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes();
    }

    private byte[] createContentDisposition(final String key, final String fileName)
    {
        return ("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n").getBytes();
    }

    public long getContentLength()
    {
        long contentLen = this.out.size();
        for (FilePart filePart : this.fileParts)
        {
            long len = filePart.getLength();
            if (len < 0)
            {
                return -1;
            }
            contentLen += len;
        }
        contentLen += this.boundaryEnd.length;
        return contentLen;
    }

    public void writeTo(final OutputStream outstream) throws IOException
    {
        int length = (int) getContentLength();
        timer = new SpendTimer(length, new TimerListener()
        {

            @Override
            public void onProgress(int bytesWritten, int bytesTotal, int speed)
            {
                responseDelivery.sendProgressMessage(request, bytesWritten, bytesTotal, speed);
            }
        });

        try
        {
            timer.start();
            this.connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);
            this.connection.setRequestProperty("Content-Length", String.valueOf(length));
            this.out.writeTo(outstream);
            updateProgress(this.out.size());

            for (FilePart filePart : this.fileParts)
            {
                filePart.writeTo(outstream);
            }
            outstream.write(this.boundaryEnd);
            updateProgress(this.boundaryEnd.length);

        } catch (Exception e)
        {
            throw new IOException("HttpEntity WriteTo Exception :" + e.getMessage());
        } finally
        {
            timer.stop();
        }
    }

    private void updateProgress(int count)
    {
        timer.updateProgress(count);
    }

    private class FilePart
    {
        protected File   file;
        protected byte[] header;

        protected FilePart(String key, File file, String type)
        {
            this.header = createHeader(key, file.getName(), type);
            this.file = file;
        }

        protected byte[] createHeader(String key, String filename, String type)
        {

            ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
            try
            {
                headerStream.write(boundaryLine);

                // Headers
                headerStream.write(createContentDisposition(key, filename));
                headerStream.write(createContentType(type));
                headerStream.write(TRANSFER_ENCODING_BINARY);
                headerStream.write(CR_LF);
            } catch (IOException e)
            {
                Log.e(TAG, "createHeader ByteArrayOutputStream exception", e);
            }
            return headerStream.toByteArray();
        }

        protected long getLength()
        {
            long streamLength = this.file.length() + CR_LF.length;
            return this.header.length + streamLength;
        }

        protected void writeTo(OutputStream out) throws IOException
        {

            out.write(this.header);
            updateProgress(this.header.length);

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(this.file));
            final byte[] tmp = new byte[DEFAULT_BUFFER_SIZE];
            int l;
            while ((l = inputStream.read(tmp)) != -1)
            {
                out.write(tmp, 0, l);
                updateProgress(l);
            }
            out.write(CR_LF);
            updateProgress(CR_LF.length);
            out.flush();

            Utils.quickClose(inputStream);
        }
    }
}
