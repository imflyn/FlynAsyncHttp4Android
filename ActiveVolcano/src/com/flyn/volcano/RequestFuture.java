package com.flyn.volcano;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RequestFuture<T> implements Future<T>
{
    private final Listener mListner;
    private Request<?>     mRequest;
    private boolean        mResultReceived = false;
    private T              mResult;
    public Throwable       mException;

    public static <E> RequestFuture<E> newFuture()
    {
        return new RequestFuture<E>();
    }

    private RequestFuture()
    {
        mListner = new Listener()
        {

            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Response<?> response)
            {
                mResultReceived = true;
                mResult = (T) response.getResult();
                notifyAll();
            }

            @Override
            public void onFailure(Throwable error)
            {
                mException = error;
                notifyAll();
            }
        };
    }

    public void setRequest(Request<?> request)
    {
        mRequest = request;
        mRequest.setListener(mListner);
    }

    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning)
    {
        if (mRequest == null)
        {
            return false;
        }

        if (!isDone())
        {
            mRequest.cancel();
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException
    {
        try
        {
            return doGet(null);
        } catch (TimeoutException e)
        {
            throw new AssertionError(e);
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return doGet(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    private synchronized T doGet(Long timeoutMs) throws InterruptedException, ExecutionException, TimeoutException
    {
        if (mException != null)
        {
            throw new ExecutionException(mException);
        }

        if (mResultReceived)
        {
            return mResult;
        }

        if (timeoutMs == null)
        {
            wait(0);
        } else if (timeoutMs > 0)
        {
            wait(timeoutMs);
        }

        if (mException != null)
        {
            throw new ExecutionException(mException);
        }

        if (!mResultReceived)
        {
            throw new TimeoutException();
        }

        return mResult;
    }

    @Override
    public boolean isCancelled()
    {
        if (mRequest == null)
        {
            return false;
        }
        return mRequest.isCanceled();
    }

    @Override
    public synchronized boolean isDone()
    {
        return mResultReceived || mException != null || isCancelled();
    }

}