package com.flyn.volcano;

public interface ResponseDelivery
{

    void sendStartMessage(Request<?> request);

    void sendFinishMessage(Request<?> request);

    void sendProgressMessage(Request<?> request, int bytesWritten, int bytesTotal, int currentSpeed);

    void sendCancleMessage(Request<?> request);

    void sendSuccessMessage(Request<?> request, Response<?> response);

    void sendFailureMessage(Request<?> request, Throwable error);

    void sendRetryMessage(Request<?> request, int retryNo);

}
