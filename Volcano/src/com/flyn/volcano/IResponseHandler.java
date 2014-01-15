package com.flyn.volcano;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.http.HttpResponse;

public interface IResponseHandler
{
    void sendResponseMessage(HttpResponse response) throws IOException;

    void sendStartMessage();

    void sendFinishMessage();

    void sendProgressMessage(int bytesWritten, int bytesTotal, int currentSpeed);

    void sendCancleMessage();

    void sendSuccessMessage(int statusCode, Map<String, String> headers, byte[] responseData);

    void sendFailureMessage(int statusCode, Map<String, String> headers, byte[] responseData, Throwable error);

    void sendRetryMessage(int retryNo);

    public URI getRequestURI();

    public void setRequestURI(URI uri);

    public Map<String, String> getRequestHeaders();

    public void setRequestHeaders(Map<String, String> requestHeaders);

    void setUseSynchronousMode(boolean useSynchronousMode);

    boolean getUseSynchronousMode();

}
