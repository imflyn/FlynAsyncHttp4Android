package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpEntity;

import android.util.Log;

import com.flyn.volcano.RequestParams.Header;
import com.flyn.volcano.SpendTimer.TimerListener;

public abstract class Request<T> implements Comparable<Request<T>>
{
    protected static final int  DEFAULT_RETRY_COUNT = 1;

    private Listener            mListener;
    private final String        url;
    private final RequestParams requestPramas;
    private final int           method;
    private final int           retryCount;
    private boolean             mResponseDelivered;
    private boolean             mCanceled           = false;
    private Integer             sequence;
    private Object              tag;
    private RequestQueue        mRequestQueue;

    public Request(String url, RequestParams requestPramas)
    {
        this(Method.GET, url, requestPramas, null);
    }

    public Request(int method, String url, RequestParams requestPramas)
    {
        this(method, url, requestPramas, null);
    }

    public Request(int method, String url, RequestParams requestPramas, Listener mListener)
    {
        this(method, url, requestPramas, DEFAULT_RETRY_COUNT, mListener);
    }

    public Request(int method, String url, RequestParams requestPramas, int retryCount, Listener mListener)
    {
        this.method = method;
        this.requestPramas = requestPramas;
        this.url = url;
        this.retryCount = retryCount;
        this.mListener = mListener;
    }

    protected abstract Response<?> parseNetworkResponse(NetworkResponse response, final ResponseDelivery responseDelivery) throws IOException;

    protected byte[] getData(NetworkResponse response, final ResponseDelivery responseDelivery) throws IOException
    {
        byte[] responseData = new byte[0];
        HttpEntity entity = response.getEntity();

        InputStream inStream = entity.getContent();
        if (inStream != null)
        {
            if (isCanceled())
                return null;

            BufferedInputStream bufferedInputStream = null;

            if (Utils.parseContentEnconding(response.getHeaders()).equals("gzip"))
            {
                bufferedInputStream = new BufferedInputStream(new GZIPInputStream(inStream));
            } else
            {
                bufferedInputStream = new BufferedInputStream(inStream);
            }

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
                while (!isCanceled() && (count = bufferedInputStream.read(buffer)) != -1 && !Thread.currentThread().isInterrupted())
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
        if (null != requestPramas)
        {
            HashMap<String, String> headersMap = new HashMap<String, String>();
            List<Header> headers = requestPramas.getHeaders();
            Header header;
            for (int i = 0; i < headers.size(); i++)
            {
                header = headers.get(i);
                headersMap.put(header.name, header.value);
            }
            return Collections.unmodifiableMap(headersMap);
        } else
            return Collections.unmodifiableMap(new HashMap<String, String>());
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

    public final Listener getListener()
    {
        return mListener;
    }

    public final void setListener(Listener listener)
    {
        this.mListener = listener;
    }

    protected void finish()
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
