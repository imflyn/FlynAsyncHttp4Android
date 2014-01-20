package com.flyn.volcano;

public interface ResponseDelivery
{

    public void postResponse(Request<?> request, Response<?> response);

    public void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    public void postError(Request<?> request, Exception error);
}
