package com.flyn.volcano;

import java.util.concurrent.Executor;

import android.os.Handler;

public class ExecutorDelivery implements ResponseDelivery
{
    private final Executor mResponsePoster;
    public ExecutorDelivery(final Handler handler)
    {
        mResponsePoster = new Executor()
        {
            @Override
            public void execute(Runnable command)
            {
                handler.post(command);
            }
        };
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
