package com.flyn.volcano;

import java.util.Map;

import org.apache.http.HttpResponse;

public class BasicNetwork implements Network
{
    private HttpStack mHttpStack;

    public BasicNetwork(HttpStack httpStack)
    {

    }

    @Override
    public NetworkResponse executeRequest(Request<?> request)
    {
        HttpResponse httpResponse;

        Map<String, String> headers = request.getHeaders();

        httpResponse = mHttpStack.performRequest(request);

        int httpStatusCode = 0;

        return new NetworkResponse();
    }

}
