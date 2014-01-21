package com.flyn.volcano;

public abstract class Listener<T>
{

    void onStart()
    {

    };

    void onFinish()
    {

    };

    void onProgress(int bytesWritten, int bytesTotal, int currentSpeed)
    {

    };

    void onCancel()
    {

    };

    abstract void onSuccess(Object result);

    abstract void onFailure(Throwable error);

    void onRetry(int retryNo)
    {

    };

}
