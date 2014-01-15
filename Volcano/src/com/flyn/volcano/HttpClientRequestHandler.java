package com.flyn.volcano;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class HttpClientRequestHandler extends RequestHandler
{

    private AbstractHttpClient mHttpClient;
    private HttpContext        mHttpContext;
    private HttpUriRequest     mHttpUriRequest;
    private HttpClientStack    mHttpClientStack;

    protected HttpClientRequestHandler(Request request, IResponseHandler responseHandler)
    {
        super(request, responseHandler);
    }

    @Override
    protected void init()
    {
        this.mHttpClientStack = new HttpClientStack();

        int method = this.request.getMethod();
        String contentType = this.request.getContentType();
        String url = this.request.getUrl();
        Map<String, String> headers = this.request.getHeaders();
        RequestParams params = this.request.getParams();
        this.mHttpClientStack.makeRequest(method, contentType, url, headers, params, this.responseHandler);

        this.mHttpContext = this.mHttpClientStack.getHttpContext();
        this.mHttpUriRequest = this.mHttpClientStack.getHttpUriRequest();
        this.mHttpClient = (AbstractHttpClient) this.mHttpClientStack.getHttpClient();

    }

    @Override
    protected void makeRequest() throws IOException
    {

        if (isCancelled())
            return;
        if (this.mHttpUriRequest.getURI().getScheme() == null)
            throw new MalformedURLException("No valid URI scheme was provided.");

        HttpResponse response = this.mHttpClient.execute(this.mHttpUriRequest, this.mHttpContext);

        if (!isCancelled() && this.responseHandler != null)
            this.responseHandler.sendResponseMessage(response);

    }

    @Override
    protected void makeRequestWithRetries() throws IOException
    {
        boolean retry = true;
        IOException cause = null;
        HttpRequestRetryHandler retryHandler = this.mHttpClient.getHttpRequestRetryHandler();

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
                    retry = (this.executionCount > 0) && retryHandler.retryRequest(cause, ++this.executionCount, this.mHttpContext);
                } catch (NullPointerException e)
                {
                    cause = new IOException("NPE in HttpClient :" + e.getMessage());
                    retry = retryHandler.retryRequest(cause, ++this.executionCount, this.mHttpContext);
                } catch (IOException e)
                {
                    if (isCancelled())
                        return;
                    cause = e;
                    retry = retryHandler.retryRequest(cause, ++this.executionCount, this.mHttpContext);
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
        if (mayInterruptIfRunning && this.request != null && !this.mHttpUriRequest.isAborted())
        {
            this.mHttpUriRequest.abort();
        }
        return isCancelled();
    }

}
