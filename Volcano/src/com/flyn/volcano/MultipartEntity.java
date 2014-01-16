/**
 This code is taken from Rafael Sanches' blog.
 http://blog.rafaelsanches.com/2011/01/29/upload-using-multipart-post-using-httpclient-in-android/
 **/
package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicHeader;

import android.util.Log;

/**
 * 简洁版apache-mine中的MultipartEntity,省去添加额外jar包的空间
 * 
 * @author V
 * 
 */
class MultipartEntity implements HttpEntity
{

    private static final String          TAG                      = MultipartEntity.class.getName();
    private static final String          APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final byte[]          CR_LF                    = ("\r\n").getBytes();
    private static final byte[]          TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary\r\n".getBytes();
    private static final char[]          MULTIPART_CHARS          = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int             DEFAULT_BUFFER_SIZE      = 8192;

    private String                       boundary;
    private byte[]                       boundaryLine;
    private byte[]                       boundaryEnd;
    private boolean                      isRepeatable             = false;

    private List<FilePart>               fileParts                = new LinkedList<FilePart>();

    private PoolingByteArrayOutputStream out;
    private ByteArrayPool                mPool;

    private final IResponseHandler       progressHandler;

    private int                          bytesWritten             = 0;
    private int                          totalSize                = 0;
    private Timer                        timer;

    private boolean                      isScheduleing            = true;
    private long                         timeStamp                = System.currentTimeMillis();
    private int                          currentSpeed             = 0;
    private int                          sizeStamp                = 0;

    public MultipartEntity(IResponseHandler progressHandler)
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

        this.progressHandler = progressHandler;

        this.mPool = new ByteArrayPool(DEFAULT_BUFFER_SIZE);
        this.out = new PoolingByteArrayOutputStream(this.mPool);

    }

    public void addPart(final String key, final String value, final String contentType)
    {
        if(this.mQuit)
            return ;
        
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
        if(this.mQuit)
            return ;
        this.fileParts.add(new FilePart(key, file, type));
    }

    public void addPart(String key, String streamName, InputStream inputStream, String type) throws IOException
    {
        if(this.mQuit)
            return ;
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
        while (!mQuit&&(l = inputStream.read(tmp)) != -1)
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

    private void updateProgress(int count)
    {
        this.bytesWritten += count;
    }

    private void startTimer()
    {
        if (null == this.timer)
            this.timer = new Timer();

        final TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if (isScheduleing)
                {
                    long nowTime = System.currentTimeMillis();
                    long spendTime = nowTime - timeStamp;
                    timeStamp = nowTime;

                    long getSize = bytesWritten - sizeStamp;
                    sizeStamp = bytesWritten;
                    if (spendTime > 0)
                        currentSpeed = (int) ((getSize / spendTime) / 1.024);

                    progressHandler.sendProgressMessage(bytesWritten, totalSize, currentSpeed);
                }
            }
        };
        this.timer.schedule(task, 500, 2000);
    }

    private void stopTimer()
    {
        this.isScheduleing = false;
        if (this.timer != null)
        {
            this.timer.cancel();
            this.timer = null;
        }
    }
    
     boolean mQuit=false;
    protected void stop()
    {
        this.mQuit=true;
        stopTimer();
    }

    private class FilePart
    {
        public File   file;
        public byte[] header;

        public FilePart(String key, File file, String type)
        {
            this.header = createHeader(key, file.getName(), type);
            this.file = file;
        }

        private byte[] createHeader(String key, String filename, String type)
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

        public long getTotalLength()
        {
            long streamLength = this.file.length() + CR_LF.length;
            return this.header.length + streamLength;
        }

        public void writeTo(OutputStream out) throws IOException
        {
            if(mQuit)
                return ;
            
            out.write(this.header);
            updateProgress(this.header.length);

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(this.file));
            final byte[] tmp = new byte[DEFAULT_BUFFER_SIZE];
            int l;
            while (!mQuit&&(l = inputStream.read(tmp)) != -1)
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

    @Override
    public long getContentLength()
    {
        long contentLen = this.out.size();
        for (FilePart filePart : this.fileParts)
        {
            long len = filePart.getTotalLength();
            if (len < 0)
            {
                return -1;
            }
            contentLen += len;
        }
        contentLen += this.boundaryEnd.length;
        return contentLen;
    }

    @Override
    public Header getContentType()
    {
        return new BasicHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
    }

    @Override
    public boolean isChunked()
    {
        return false;
    }

    public void setIsRepeatable(boolean isRepeatable)
    {
        this.isRepeatable = isRepeatable;
    }

    @Override
    public boolean isRepeatable()
    {
        return isRepeatable;
    }

    @Override
    public boolean isStreaming()
    {
        return false;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException
    {
        if(this.mQuit)
            return ;
        try
        {
            startTimer();
            this.totalSize = (int) getContentLength();
            this.out.writeTo(outstream);
            updateProgress(this.out.size());

            for (FilePart filePart : this.fileParts)
            {
                if(this.mQuit)
                    return ;
                filePart.writeTo(outstream);
            }
            if(this.mQuit)
                return ;
            outstream.write(this.boundaryEnd);
            updateProgress(this.boundaryEnd.length);

        } catch (Exception e)
        {
            throw new IOException("HttpEntity WriteTo Exception :" + e.getMessage());
        } finally
        {
            stopTimer();
        }
    }

    @Override
    public Header getContentEncoding()
    {
        return null;
    }

    @Override
    public void consumeContent() throws IOException, UnsupportedOperationException
    {
        if (isStreaming())
        {
            throw new UnsupportedOperationException("Streaming entity does not implement #consumeContent()");
        }
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("getContent() is not supported. Use writeTo() instead.");
    }
}