package com.flyn.volcano;

import java.util.Map;


public abstract class Request<T>
{
    private final String        url;
    private final RequestParams requestPramas;

    public Request(String url, RequestParams requestPramas)
    {
        this.requestPramas = requestPramas;
        this.url = url;
    }

    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    public final String getUrl()
    {
        return url;
    }

    public final RequestParams getRequestPramas()
    {
        return requestPramas;
    }
    
    public Map<String,String> getHeaders()
    {
        return this.requestPramas.getUrlParams();
    }

}
