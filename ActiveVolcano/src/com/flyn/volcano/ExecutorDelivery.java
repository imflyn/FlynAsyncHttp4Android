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
        final Listener listener = request.getListener();
        if (listener != null)
            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onStart();
                }
            });
    }

    @Override
    public void sendFinishMessage(final Request<?> request)
    {
        final Listener listener = request.getListener();
        if (listener != null)
            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onFinish();
                }
            });
    }

    @Override
    public void sendProgressMessage(final Request<?> request, final int bytesWritten, final int bytesTotal, final int currentSpeed)
    {
        final Listener listener = request.getListener();
        if (listener != null)
            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onProgress(bytesWritten, bytesTotal, currentSpeed);
                }
            });
    }

    @Override
    public void sendCancleMessage(final Request<?> request)
    {
        final Listener listener = request.getListener();
        if (listener != null)
            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onCancel();
                }
            });
    }

    @Override
    public void sendSuccessMessage(final Request<?> request, final Response<?> response)
    {
        final Listener listener = request.getListener();
        if (listener != null)

            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onSuccess(response);
                }
            });
    }

    @Override
    public void sendFailureMessage(final Request<?> request, final Throwable error)
    {
        final Listener listener = request.getListener();
        if (listener != null)
            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onFailure(error);
                }
            });
    }

    @Override
    public void sendRetryMessage(final Request<?> request, final int retryNo)
    {
        final Listener listener = request.getListener();
        if (listener != null)
            this.mResponsePoster.execute(new Runnable()
            {

                @Override
                public void run()
                {
                    listener.onRetry(retryNo);
                }
            });
    }

}
