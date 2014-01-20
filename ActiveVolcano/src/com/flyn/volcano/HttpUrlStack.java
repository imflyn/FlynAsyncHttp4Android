package com.flyn.volcano;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;

public class HttpUrlStack implements HttpStack
{
    
    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException
    {
        return null;
    }

}
