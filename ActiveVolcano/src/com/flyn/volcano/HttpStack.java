package com.flyn.volcano;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;

public interface HttpStack
{
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException;
}
