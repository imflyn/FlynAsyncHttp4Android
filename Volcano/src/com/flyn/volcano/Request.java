package com.flyn.volcano;

import java.io.IOException;

import android.util.Log;

public abstract class Request implements Runnable
{

    protected IResponseHandler responseHandler;

    protected int              executionCount;
    protected boolean          isCancelled      = false;
    protected boolean          cancelIsNotified = false;
    protected boolean          isFinished       = false;

    protected Request(IResponseHandler responseHandler)
    {
        this.responseHandler = responseHandler;
    }

    @Override
    public void run()
    {
        if (isCancelled())
            return;

        if (null != this.responseHandler)
            this.responseHandler.sendStartMessage();

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

    public interface Method
    {
        int DEPRECATED_GET_OR_POST = -1; // haven't be used
        int GET                    = 0;
        int POST                   = 1;
        int PUT                    = 2;
        int DELETE                 = 3;
        int HEAD                   = 4;
    }

}
