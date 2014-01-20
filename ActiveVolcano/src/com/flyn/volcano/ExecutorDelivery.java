package com.flyn.volcano;

import android.os.Handler;

public class ExecutorDelivery implements ResponseDelivery
{

    public ExecutorDelivery(Handler handler)
    {

    }

    @Override
    public void sendStartMessage(Request<?> request)
    {

    }

    @Override
    public void sendFinishMessage(Request<?> request)
    {

    }

    @Override
    public void sendProgressMessage(Request<?> request, int bytesWritten, int bytesTotal, int currentSpeed)
    {

    }

    @Override
    public void sendCancleMessage(Request<?> request)
    {

    }

    @Override
    public void sendSuccessMessage(Request<?> request, Response<?> response)
    {
    }

    @Override
    public void sendFailureMessage(Request<?> request, Throwable error)
    {

    }

    @Override
    public void sendRetryMessage(int retryNo)
    {

    }

}
