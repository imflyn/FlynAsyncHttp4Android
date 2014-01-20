package com.flyn.volcano;

import java.util.Map;

public abstract class Request<T>
{
    private RequestPramas requestPramas;

    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    public final RequestPramas getRequestPramas()
    {
        return requestPramas;
    }

    public final void setRequestPramas(RequestPramas requestPramas)
    {
        this.requestPramas = requestPramas;
    }

    public Map<String, String> getHeaders()
    {
        return null;
    }
}
