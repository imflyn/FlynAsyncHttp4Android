package com.flyn.volcano;

import java.io.IOException;

import org.apache.http.HttpResponse;

public interface HttpStack
{

    public abstract HttpResponse performRequest(Request<?> request, ResponseDelivery responseDelivery) throws IOException;

}
