package com.flyn.volcano;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;

public class BasicNetwork implements Network
{
    private HttpStack mHttpStack;

    public BasicNetwork(HttpStack httpStack)
    {

    }

    @Override
    public NetworkResponse executeRequest(Request<?> request, ResponseDelivery responseDelivery) throws IOException
    {
        HttpResponse httpResponse;

        Map<String, String> headers = request.getHeaders();

        httpResponse = mHttpStack.performRequest(request, responseDelivery);

        int httpStatusCode = 0;

        return new NetworkResponse();
    }

}
