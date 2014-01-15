package com.flyn.volcano;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class HttpClientRequest extends Request
{
    private final AbstractHttpClient client;
    private final HttpContext        context;
    private final HttpUriRequest     request;

    public HttpClientRequest(AbstractHttpClient client, HttpContext context, HttpUriRequest request, IResponseHandler responseHandler)
    {
        super(responseHandler);
        this.client = client;
        this.context = context;
        this.request = request;
    }

    @Override
    protected void makeRequest() throws IOException
    {
        if (isCancelled())
            return;
        if (this.request.getURI().getScheme() == null)
            throw new MalformedURLException("No valid URI scheme was provided.");

        HttpResponse response = this.client.execute(this.request, this.context);

        if (!isCancelled() && this.responseHandler != null)
            this.responseHandler.sendResponseMessage(response);

    }

    @Override
    protected void makeRequestWithRetries() throws IOException
    {
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = this.client.getHttpRequestRetryHandler();

        try
        {
            while (retry)
            {
                try
                {
                    makeRequest();
                    return;
                } catch (UnknownHostException e)
                {
                    cause = new IOException("UnknownHostException :" + e.getMessage());
                    retry = (this.executionCount > 0) && retryHandler.retryRequest(cause, ++this.executionCount, this.context);
                } catch (NullPointerException e)
                {
                    cause = new IOException("NPE in HttpClient :" + e.getMessage());
                    retry = retryHandler.retryRequest(cause, ++this.executionCount, this.context);
                } catch (IOException e)
                {
                    if (isCancelled())
                        return;
                    cause = e;
                    retry = retryHandler.retryRequest(cause, ++this.executionCount, this.context);
                }
                if (retry && retryHandler != null)
                {
                    this.responseHandler.sendRetryMessage(this.executionCount);
                }

            }
        } catch (Exception e)
        {
            Log.e(this.getClass().getName(), "Caused by unhandled exception :", e);

            cause = new IOException("Unhandled exception :" + e.getMessage());
        }

        throw (cause);

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        this.isCancelled = true;
        if (mayInterruptIfRunning && this.request != null && !this.request.isAborted())
        {
            this.request.abort();
        }
        return isCancelled();
    }

}
