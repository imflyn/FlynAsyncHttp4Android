package com.flyn.volcano;

import android.os.Handler;

public class ExecutorDelivery implements ResponseDelivery
{

    public ExecutorDelivery(Handler handler)
    {
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response)
    {

    }

    @Override
    public void postResponse(Request<?> request, Response<?> response, Runnable runnable)
    {

    }

    @Override
    public void postError(Request<?> request, Exception error)
    {

    }

}
