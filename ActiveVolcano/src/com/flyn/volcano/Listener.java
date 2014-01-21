package com.flyn.volcano;

public abstract class Listener<T>
{

    public void onStart()
    {

    };

    public void onFinish()
    {

    };

    public void onProgress(int bytesWritten, int bytesTotal, int currentSpeed)
    {

    };

    public void onCancel()
    {

    };

    public abstract void onSuccess(Object result);

    public abstract void onFailure(Throwable error);

    public void onRetry(int retryNo)
    {

    };

}
