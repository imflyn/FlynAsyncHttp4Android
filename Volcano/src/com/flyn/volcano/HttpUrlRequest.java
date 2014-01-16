package com.flyn.volcano;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;

import android.util.Log;

public class HttpUrlRequest extends Request
{
    private final HttpURLConnection connect;
    private final RequestParams     requestParams;

    public HttpUrlRequest(HttpURLConnection connect, RequestParams requestParams, IResponseHandler responseHandler)
    {
        super(responseHandler);
        this.connect = connect;
        this.requestParams = requestParams;
    }

    @Override
    protected void makeRequest() throws IOException
    {
        if (isCancelled())
            return;
        // if (this.request.getURI().getScheme() == null)
        // throw new MalformedURLException("No valid URI scheme was provided.");
        //
        // HttpResponse response = this.client.execute(this.request,
        // this.context);

        // if (!isCancelled() && this.responseHandler != null)
        // this.responseHandler.sendResponseMessage(response);

    }

    @Override
    protected void makeRequestWithRetries() throws IOException
    {
        boolean retry = true;
        IOException cause = null;
        // HttpRequestRetryHandler retryHandler =
        // this.client.getHttpRequestRetryHandler();

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
                    // retry = (this.executionCount > 0) &&
                    // retryHandler.retryRequest(cause, ++this.executionCount,
                    // this.context);
                } catch (NullPointerException e)
                {
                    cause = new IOException("NPE in HttpClient :" + e.getMessage());
                    // retry = retryHandler.retryRequest(cause,
                    // ++this.executionCount, this.context);
                } catch (IOException e)
                {
                    if (isCancelled())
                        return;
                    cause = e;
                    // retry = retryHandler.retryRequest(cause,
                    // ++this.executionCount, this.context);
                }
                // if (retry && retryHandler != null)
                // {
                // this.responseHandler.sendRetryMessage(this.executionCount);
                // }

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
        // if (mayInterruptIfRunning && this.request != null &&
        // !this.request.isAborted())
        // {
        // this.request.abort();
        // }
        return isCancelled();
    }

}
