package com.flyn.volcano;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.auth.AuthScope;

import android.content.Context;

public class HttpUrlStack extends NetStack
{

    private static final String TAG = HttpUrlStack.class.getName();
    private SSLSocketFactory    mSslSocketFactory;

    public HttpUrlStack(Context context)
    {
        super(context);
    }

    private HttpURLConnection openConnection(URL url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(this.timeout);
        connection.setReadTimeout(this.timeout);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory == null)
        {
            if (this.fixNoHttpResponseException)
                this.mSslSocketFactory = HttpUrlSSLSocketFactory.getFixedSocketFactory();
            else
                this.mSslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        } else
            ((HttpsURLConnection) connection).setSSLSocketFactory(this.mSslSocketFactory);

        return connection;
    }

    @Override
    protected RequestFuture sendRequest(String contentType, IResponseHandler responseHandler, Object[] objs)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RequestFuture get(String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RequestFuture post(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RequestFuture delete(String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RequestFuture put(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected RequestFuture head(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEnableRedirects(boolean enableRedirects)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setUserAgent(String userAgent)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaxConnections(int maxConnections)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTimeOut(int timeout)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProxy(String hostname, int port)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setProxy(String hostname, int port, String username, String password)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaxRetriesAndTimeout(int retries, int timeout)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBasicAuth(String username, String password, AuthScope authScope)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setBasicAuth(String username, String password)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearBasicAuth()
    {
        // TODO Auto-generated method stub

    }

}
