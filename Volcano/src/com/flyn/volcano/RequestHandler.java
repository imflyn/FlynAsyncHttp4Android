package com.flyn.volcano;

import java.io.IOException;

import android.util.Log;

public abstract class RequestHandler implements Runnable
{

    protected final IResponseHandler responseHandler;
    protected final Request          request;

    protected int                    executionCount;
    protected boolean                isCancelled      = false;
    protected boolean                cancelIsNotified = false;
    protected boolean                isFinished       = false;

    protected RequestHandler(Request request, IResponseHandler responseHandler)
    {
        if (this.request == null)
            throw new IllegalArgumentException("Request argument can't be null.");
        this.request = request;
        this.responseHandler = responseHandler;
    }

    protected abstract void init();

    @Override
    public void run()
    {
        if (isCancelled())
            return;

        if (null != this.responseHandler)
            this.responseHandler.sendStartMessage();
        
        init();
        
        if (isCancelled())
        {
            return;
        }
        
        try
        {
            makeRequestWithRetries();
        } catch (IOException e)
        {
            if (!isCancelled() && this.responseHandler != null)
            {
                this.responseHandler.sendFailureMessage(0, null, null, e);
            } else
            {
                Log.e(this.getClass().getName(), "makeRequestWithRetries returned error, but handler is null", e);
            }
        }
        if (isCancelled())
        {
            return;
        }

        if (this.responseHandler != null)
        {
            this.responseHandler.sendFinishMessage();
        }

        this.isFinished = true;
    }

    protected abstract void makeRequest() throws IOException;

    protected abstract void makeRequestWithRetries() throws IOException;

    public final boolean isCancelled()
    {
        if (this.isCancelled)
            sendCancleNotification();

        return this.isCancelled;
    }

    private synchronized void sendCancleNotification()
    {
        if (!this.isFinished && this.isCancelled && !this.cancelIsNotified)
        {
            this.cancelIsNotified = true;
            if (null != this.responseHandler)
                this.responseHandler.sendCancleMessage();
        }
    }

    public final boolean isFinished()
    {
        return isCancelled() || this.isFinished;
    }

    public abstract boolean cancel(boolean mayInterruptIfRunning);

}
