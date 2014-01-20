package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;

import android.text.TextUtils;
import android.util.Log;

import com.flyn.volcano.SpendTimer.TimerListener;

public class FileRequest extends Request<File>
{

    private final static String TAG          = FileRequest.class.getName();
    private static List<File>   mFileList    = Collections.synchronizedList(new ArrayList<File>(3));

    private long                bytesTotal   = 0;
    private long                bytesWritten = 0;

    private final File          mFile;
    private final File          tempFile;

    private final boolean       isContinue;

    public FileRequest(int method, String url, RequestParams requestPramas, int retryCount, String savePath, String fileName, boolean isContinue)
    {
        super(method, url, requestPramas, retryCount);
        if (TextUtils.isEmpty(savePath) || TextUtils.isEmpty(fileName))
            throw new IllegalArgumentException("Savepath or filename can't be null.");

        this.isContinue = isContinue;

        this.mFile = new File(savePath, fileName);
        String tempname = fileName.substring(0, fileName.lastIndexOf("."));
        this.tempFile = new File(savePath, tempname + ".tmp");

        if (!this.mFile.getParentFile().exists())
            this.mFile.getParentFile().mkdirs();
    }

    public FileRequest(int method, String url, RequestParams requestPramas, String savePath, String fileName, boolean isContinue)
    {
        this(method, url, requestPramas, DEFAULT_RETRY_COUNT, savePath, fileName, isContinue);
    }

    private File getTargetFile()
    {
        return this.mFile;
    }


    @Override
    protected Response<?> parseNetworkResponse(NetworkResponse response, final ResponseDelivery responseDelivery) throws IOException
    {
        HttpEntity entity=response.getEntity();
        long length = entity.getContentLength();
        this.bytesTotal = length > 0 ? length : entity.getContent().available();
        Log.i(TAG, "bytesTotal:" + bytesTotal);

        if (isCanceled())
            return null;

        if (getTargetFile().exists())
            throw new IOException("File already exists.");

        if (this.tempFile.exists())
        {
            if (this.isContinue)
            {
                this.bytesWritten = this.tempFile.length();
                this.bytesTotal += this.bytesWritten;
            } else
                this.tempFile.delete();
        }
        
        SpendTimer timer=new SpendTimer((int)bytesTotal, new TimerListener()
        {
            
            @Override
            public void onProgress(int bytesWritten, int bytesTotal, int speed)
            {
                responseDelivery.sendProgressMessage(FileRequest.this, bytesWritten, bytesTotal, speed);
            }
        });

        BufferedInputStream inputStream = null;
        RandomAccessFile accessFile = null;
        timer.start();
        try
        {
            if (mFileList.contains(this.mFile))
                throw new IOException("This file is downloading.");
            else
                mFileList.add(this.mFile);

            inputStream = new BufferedInputStream(entity.getContent());
            accessFile = new RandomAccessFile(this.tempFile, "rw");

            if (this.bytesWritten != 0)
            {
                // 支持断点续传
                accessFile.seek(this.bytesWritten);
            } else
            {
                // accessFile.setLength(this.bytesTotal);
            }

            int count;
            byte[] buffer = new byte[4096];

            while (!isCanceled() && (count = inputStream.read(buffer)) != -1)
            {
                accessFile.write(buffer, 0, count);
                timer.updateProgress(count);
            }
            if (isCanceled())
                return null;

            this.tempFile.renameTo(getTargetFile());

        } catch (Exception e)
        {
            throw new IOException("EntityToData exception:" + e.getMessage());
        } finally
        {
            timer.stop();
            if (null != getTargetFile())
                mFileList.remove(getTargetFile());

            Utils.quickClose(accessFile);
            Utils.quickClose(inputStream);
        }
        
        return Response.build(getTargetFile());
    }

    @Override
    protected void deliverResponse(File response)
    {

    }

}
