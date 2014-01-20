package com.flyn.volcano;

public interface Listener
{

    void onStart();

    void onFinish();

    void onProgress(int bytesWritten, int bytesTotal, int currentSpeed);

    void onCancel();

    void onSuccess();

    void onFailure();

    void onRetry(int retryNo);

}
