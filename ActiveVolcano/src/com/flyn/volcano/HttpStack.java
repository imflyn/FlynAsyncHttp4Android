package com.flyn.volcano;

import org.apache.http.HttpResponse;

public interface HttpStack
{
    public HttpResponse performRequest(Request<?> request);
}
