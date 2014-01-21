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
    public void sendStartMessage(final Request<?> request)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onStart();
            }
        });
    }

    @Override
    public void sendFinishMessage(final Request<?> request)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onFinish();
            }
        });
    }

    @Override
    public void sendProgressMessage(final Request<?> request, final int bytesWritten, final int bytesTotal, final int currentSpeed)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onProgress(bytesWritten, bytesTotal, currentSpeed);
            }
        });
    }

    @Override
    public void sendCancleMessage(final Request<?> request)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onCancel();
            }
        });
    }

    @Override
    public void sendSuccessMessage(final Request<?> request, final Response<?> response)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onSuccess(response.result);
            }
        });
    }

    @Override
    public void sendFailureMessage(final Request<?> request, final Throwable error)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onFailure(error);
            }
        });
    }

    @Override
    public void sendRetryMessage(final Request<?> request, final int retryNo)
    {
        this.mResponsePoster.execute(new Runnable()
        {

            @Override
            public void run()
            {
                request.getListener().onRetry(retryNo);
            }
        });
    }

}
