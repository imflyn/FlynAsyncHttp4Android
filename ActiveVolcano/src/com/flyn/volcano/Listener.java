package com.flyn.volcano;

import org.apache.http.HttpResponse;

public interface Listener
{
    void sendResponseMessage(HttpResponse response) ;

    void sendStart();

    void sendFinish();

    void onProgress(int bytesWritten, int bytesTotal, int currentSpeed);

    void onCancel();

    void onSuccess();

    void onFailure();

    void onRetry(int retryNo);

}
