package com.flyn.volcano;

import java.lang.ref.WeakReference;

public class RequestFuture
{
    private final WeakReference<Request> request;

    public RequestFuture(Request request)
    {
        this.request = new WeakReference<Request>(request);
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
        Request _request = this.request.get();
        return _request == null || _request.cancel(mayInterruptIfRunning);

    }

    public boolean isFinished()
    {

        Request _request = this.request.get();
        return _request == null || _request.isFinished();

    }

    public boolean isCanceled()
    {
        Request _request = this.request.get();
        return _request == null || _request.isCancelled();
    }

    public boolean shouldBeGarbageCollected()
    {
        boolean should = isCanceled() || isFinished();

        if (should)
            this.request.clear();
        return should;

    }

}
