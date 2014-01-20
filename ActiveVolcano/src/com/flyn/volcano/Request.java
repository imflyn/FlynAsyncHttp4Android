package com.flyn.volcano;

import java.util.Map;

public abstract class Request<T>
{
    private final String        url;
    private final RequestParams requestPramas;
    private final int           method;

    public Request(int method, String url, RequestParams requestPramas)
    {
        this.method = method;
        this.requestPramas = requestPramas;
        this.url = url;
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

    public Map<String, String> getHeaders()
    {
        return this.requestPramas.getUrlParams();
    }

    public interface Method
    {
        int GET    = 0;
        int POST   = 1;
        int PUT    = 2;
        int DELETE = 3;
        int HEAD   = 4;
    }
}
