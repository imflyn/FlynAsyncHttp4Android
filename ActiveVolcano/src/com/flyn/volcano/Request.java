package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpEntity;

import android.util.Log;

import com.flyn.volcano.SpendTimer.TimerListener;

public abstract class Request<T> implements Comparable<Request<T>>
{
    protected static final int  DEFAULT_RETRY_COUNT = 1;

    private final String        url;
    private final RequestParams requestPramas;
    private final int           method;
    private final int           retryCount;
    private boolean             mResponseDelivered;
    private boolean             mCanceled           = false;
    private Integer             sequence;
    private Object              tag;
    private RequestQueue        mRequestQueue;

    public Request(int method, String url, RequestParams requestPramas)
    {
        this(method, url, requestPramas, DEFAULT_RETRY_COUNT);
    }

    public Request(int method, String url, RequestParams requestPramas, int retryCount)
    {
        this.method = method;
        this.requestPramas = requestPramas;
        this.url = url;
        this.retryCount = retryCount;
    }

    protected abstract Response<?> parseNetworkResponse(NetworkResponse response, final ResponseDelivery responseDelivery) throws IOException;

    protected byte[] getData(NetworkResponse response, final ResponseDelivery responseDelivery) throws IOException
    {
        byte[] responseData = new byte[0];
        HttpEntity entity = response.getEntity();

        BufferedInputStream inStream = new BufferedInputStream(entity.getContent());
        if (inStream != null)
        {
            if (isCanceled())
                return null;

            long contentLength = entity.getContentLength();
            if (contentLength > Integer.MAX_VALUE)
            {
                throw new IllegalArgumentException("HttpEntity is too large to be buffered.");
            }

            ByteArrayPool mPool = new ByteArrayPool(4096);
            PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) contentLength);
            byte[] buffer = mPool.getBuf(1024);
            SpendTimer timer = new SpendTimer((int) contentLength, new TimerListener()
            {

                @Override
                public void onProgress(int bytesWritten, int bytesTotal, int speed)
                {
                    responseDelivery.sendProgressMessage(Request.this, bytesWritten, bytesTotal, speed);
                }

            });
            try
            {
                timer.start();
                int count;
                while (!isCanceled() && (count = inStream.read(buffer)) != -1 && !Thread.currentThread().isInterrupted())
                {
                    bytes.write(buffer, 0, count);
                    timer.updateProgress(count);
                }

                if (isCanceled())
                    return null;

                responseData = bytes.toByteArray();
            } catch (OutOfMemoryError e)
            {
                System.gc();
                throw new IOException("Data too large to get in memory.");
            } finally
            {
                timer.stop();
                try
                {
                    // 释放http连接所占用的资源
                    entity.consumeContent();
                    mPool.returnBuf(buffer);
                } catch (IOException e)
                {
                    Log.e(Request.class.getName(), "Error occured when calling consumingContent", e);
                }
                Utils.quickClose(bytes);
            }
        }
        return responseData;

    }

    abstract protected void deliverResponse(T response);

    protected final int getMethod()
    {
        return method;
    }

    public final String getUrl()
    {
        return url;
    }

    public final RequestParams getRequestPramas()
    {
        return requestPramas;
    }

    public final Map<String, String> getHeaders()
    {
        return requestPramas.getUrlParams();
    }

    public final void setSequence(int sequence)
    {
        this.sequence = sequence;
    }

    public final int getSequence()
    {
        if (sequence == null)
        {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return sequence;
    }

    public void cancel()
    {
        mCanceled = true;
    }

    public boolean isCanceled()
    {
        return mCanceled;
    }

    public final Object getTag()
    {
        return tag;
    }

    public final void setTag(Object tag)
    {
        this.tag = tag;
    }

    public final int getRetryCount()
    {
        return retryCount;
    }

    protected void markDelivered()
    {
        this.mResponseDelivered = true;
    }

    protected boolean hasHadResponseDelivered()
    {
        return this.mResponseDelivered;
    }

    protected final RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }

    protected final void setRequestQueue(RequestQueue requestQueue)
    {
        this.mRequestQueue = requestQueue;
    }

    void finish()
    {
        if (this.mRequestQueue != null)
        {
            this.mRequestQueue.finish(this);
        }
    }

    public interface Method
    {
        int GET    = 0;
        int POST   = 1;
        int PUT    = 2;
        int DELETE = 3;
        int HEAD   = 4;
    }

    @Override
    public int compareTo(Request<T> another)
    {
        return this.sequence - another.sequence;
    }
}
