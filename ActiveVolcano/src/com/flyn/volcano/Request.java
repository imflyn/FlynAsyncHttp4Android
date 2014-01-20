package com.flyn.volcano;

import java.util.Map;

public abstract class Request<T> implements Comparable<Request<T>>
{
    private final String        url;
    private final RequestParams requestPramas;
    private final int           method;
    private final int           retryCount;
    private boolean             mResponseDelivered;
    private boolean             mCanceled = false;
    private Integer             sequence;
    private Object              tag;
    private RequestQueue        mRequestQueue;

    public Request(int method, String url, RequestParams requestPramas)
    {
        this(method, url, requestPramas, 1);
    }

    public Request(int method, String url, RequestParams requestPramas, int retryCount)
    {
        this.method = method;
        this.requestPramas = requestPramas;
        this.url = url;
        this.retryCount = retryCount;
    }

    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

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
