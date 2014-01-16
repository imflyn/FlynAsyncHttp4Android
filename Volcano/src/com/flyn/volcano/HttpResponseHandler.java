package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.protocol.HTTP;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class HttpResponseHandler implements IResponseHandler
{
    private static final String TAG                 = HttpResponseHandler.class.getName();

    protected static final int  SUCCESS_MESSAGE     = 0;
    protected static final int  FAILURE_MESSAGE     = 1;
    protected static final int  START_MESSAGE       = 2;
    protected static final int  FINISH_MESSAGE      = 3;
    protected static final int  PROGRESS_MESSAGE    = 4;
    protected static final int  RETRY_MESSAGE       = 5;
    protected static final int  CANCEL_MESSAGE      = 6;

    protected static final int  DEFAULT_BUFFER_SIZE = 4096;

    private Handler             handler;
    public static final String  DEFAULT_CHARSET     = HTTP.UTF_8;
    private String              responseCharset     = DEFAULT_CHARSET;
    private boolean             useSynchronousMode  = false;
    private URI                 requestURI          = null;
    private Map<String, String> requestHeaders      = null;
    protected boolean           isCancelled         = false;

    public HttpResponseHandler()
    {
        postRunnable(null);
    }

    protected void postRunnable(Runnable runnable)
    {
        // 无法在子线程中创建ResponseHandler会导致Looper.Loop()阻塞
        // boolean missingLopper = null == Looper.myLooper();
        boolean missingLopper = null == Looper.getMainLooper();
        if (missingLopper)
        {
            Looper.prepare();
        }
        if (null == this.handler)
        {
            this.handler = new ResponderHandler(this);
        }
        if (null != runnable)
        {
            this.handler.post(runnable);
        }
        if (missingLopper)
        {
            Looper.loop();
        }

    }

    static class ResponderHandler extends Handler
    {
        private final WeakReference<HttpResponseHandler> mResponder;

        ResponderHandler(HttpResponseHandler handler)
        {
            this.mResponder = new WeakReference<HttpResponseHandler>(handler);
        }

        @Override
        public void handleMessage(Message msg)
        {
            HttpResponseHandler handler = this.mResponder.get();
            if (null != handler)
            {
                handler.handleMessage(msg);
            }
        }

    }

    public void onStart()
    {

    }

    public void onFinish()
    {

    }

    public void onProgress(int bytesWritten, int bytesTotal, int speed)
    {

    }

    public void onRetry(int retryNo)
    {
    }

    public void onCancel()
    {

    }

    public void cancel()
    {
        this.isCancelled = true;
    }

    protected abstract void onSuccess(int statusCode, Map<String, String> headers, byte[] responseBody);

    protected abstract void onFailure(int statusCode, Map<String, String> headers, byte[] responseBody, Throwable error);

    @Override
    public void sendStartMessage()
    {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    @Override
    public void sendFinishMessage()
    {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }

    @Override
    public void sendFailureMessage(int statusCode, Map<String, String> headers, byte[] responseData, Throwable error)
    {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { statusCode, headers, responseData, error }));
    }

    @Override
    public void sendCancleMessage()
    {
        sendMessage(obtainMessage(CANCEL_MESSAGE, null));
    }

    @Override
    public void sendProgressMessage(int bytesWritten, int bytesTotal, int speed)
    {
        sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] { bytesWritten, bytesTotal, speed }));
    }

    @Override
    public void sendRetryMessage(int retryNo)
    {
        sendMessage(obtainMessage(RETRY_MESSAGE, new Object[] { retryNo }));
    }

    @Override
    public void sendSuccessMessage(int statusCode, Map<String, String> headers, byte[] responseData)
    {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] { statusCode, headers, responseData }));
    }

    @Override
    public void sendResponseMessage(HttpResponse response) throws IOException
    {
        if (!Thread.currentThread().isInterrupted() && !isCancelled)
        {
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == HttpStatus.SC_NOT_MODIFIED)
            {
                // 还未加入Cache缓存处理,加入后可以添加具体处理逻辑
            }

            byte[] responseData = new byte[0];

            if (!Thread.currentThread().isInterrupted() && !isCancelled)
            {
                Log.i(TAG, "statusCode:" + statusCode);
                if (statusCode >= 200 && statusCode < 300)
                {
                    if (response.getEntity() != null)
                        responseData = entityToData(response.getEntity());
                    sendSuccessMessage(statusCode, convertHeaders(response.getAllHeaders()), responseData);
                } else
                    sendFailureMessage(statusCode, convertHeaders(response.getAllHeaders()), responseData, new HttpResponseException(statusCode, statusLine.getReasonPhrase()));
            }
        }
    }

    protected byte[] entityToData(HttpEntity entity) throws IOException
    {

        byte[] responseData = null;

        BufferedInputStream inStream = new BufferedInputStream(entity.getContent());
        if (inStream != null)
        {
            long contentLength = entity.getContentLength();
            if (contentLength > Integer.MAX_VALUE)
            {
                throw new IllegalArgumentException("HttpEntity is too large to be buffered.");
            }

            ByteArrayPool mPool = new ByteArrayPool(DEFAULT_BUFFER_SIZE);
            PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) contentLength);
            byte[] buffer = null;
            try
            {
                buffer = mPool.getBuf(1024);
                int count;
                while (!isCancelled && (count = inStream.read(buffer)) != -1 && !Thread.currentThread().isInterrupted())
                {
                    bytes.write(buffer, 0, count);
                    if (contentLength >= 0 && ((count / (contentLength / 100)) % 10 == 0))
                        sendProgressMessage(count, (int) contentLength, 0);// 下载速度暂时设置为0
                }
                responseData = bytes.toByteArray();
            } catch (OutOfMemoryError e)
            {
                System.gc();
                throw new IOException("Data too large to get in memory.");
            } finally
            {
                try
                {
                    // 释放http连接所占用的资源
                    entity.consumeContent();
                    if (null != buffer)
                        mPool.returnBuf(buffer);
                } catch (IOException e)
                {
                    Log.e(HttpResponseHandler.class.getName(), "Error occured when calling consumingContent", e);
                }
                Utils.quickClose(bytes);
            }
        }

        return responseData;
    }

    private Message obtainMessage(int responseMessageId, Object responseMessage)
    {
        Message msg;
        if (this.handler != null)
        {
            msg = this.handler.obtainMessage(responseMessageId, responseMessage);
        } else
        {
            msg = Message.obtain();
            if (null != msg)
            {
                msg.what = responseMessageId;
                if (null != responseMessage)
                    msg.obj = responseMessage;
            }
        }
        return msg;
    }

    private void sendMessage(Message msg)
    {
        if (this.handler == null || getUseSynchronousMode())
            handleMessage(msg);
        else
            this.handler.sendMessage(msg);
    }

    @SuppressWarnings("unchecked")
    protected void handleMessage(Message msg)
    {
        Object[] response;
        switch (msg.what)
        {
            case SUCCESS_MESSAGE:
                response = (Object[]) msg.obj;
                if (null != response && response.length >= 3)
                    onSuccess((Integer) response[0], (Map<String, String>) response[1], (byte[]) response[2]);
                else
                    Log.e(TAG, "SUCCESS_MESSAGE didn't got enough params");
                break;
            case FAILURE_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 4)
                    onFailure((Integer) response[0], (Map<String, String>) response[1], (byte[]) response[2], (Throwable) response[3]);
                else
                    Log.e(TAG, "FAILURE_MESSAGE didn't got enough params");
                break;
            case START_MESSAGE:
                onStart();
                break;
            case FINISH_MESSAGE:
                onFinish();
                break;
            case PROGRESS_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length >= 2)
                {
                    try
                    {
                        onProgress((Integer) response[0], (Integer) response[1], (Integer) response[2]);
                    } catch (Throwable t)
                    {
                        Log.e(TAG, "custom onProgress contains an error", t);
                    }
                } else
                    Log.e(TAG, "PROGRESS_MESSAGE didn't got enough params");
                break;
            case RETRY_MESSAGE:
                response = (Object[]) msg.obj;
                if (response != null && response.length == 1)
                    onRetry((Integer) response[0]);
                else
                    Log.e(TAG, "RETRY_MESSAGE didn't get enough params");
                break;
            case CANCEL_MESSAGE:
                onCancel();
                break;

        }
    }

    protected Map<String, String> convertHeaders(Header[] headers)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Header header : headers)
        {
            map.put(header.getName(), header.getValue());
        }
        return map;
    }

    @Override
    public URI getRequestURI()
    {
        return this.requestURI;
    }

    @Override
    public Map<String, String> getRequestHeaders()
    {
        return this.requestHeaders;
    }

    @Override
    public void setRequestURI(URI requestURI)
    {
        this.requestURI = requestURI;
    }

    @Override
    public void setRequestHeaders(Map<String, String> requestHeaders)
    {
        this.requestHeaders = requestHeaders;
    }

    @Override
    public boolean getUseSynchronousMode()
    {
        return this.useSynchronousMode;
    }

    @Override
    public void setUseSynchronousMode(boolean value)
    {
        this.useSynchronousMode = value;
    }

    public final String getResponseCharse()
    {
        return this.responseCharset == null ? DEFAULT_CHARSET : this.responseCharset;
    }

    public final void setResponseCharse(String responseCharse)
    {
        this.responseCharset = responseCharse;
    }

}
