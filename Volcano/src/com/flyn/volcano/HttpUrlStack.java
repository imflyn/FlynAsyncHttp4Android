package com.flyn.volcano;

import java.net.HttpURLConnection;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;

public class HttpUrlStack extends NetStack
{
   
    private static final String TAG = HttpUrlStack.class.getName();
    private HttpURLConnection   connection;
    
    protected HttpUrlStack(Context context)
    {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected RequestFuture sendRequest(String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void addHeaders(Map<String, String> headers)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void get(String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void post(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void delete(String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void put(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void head(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCookieStore(CookieStore cookieStore)
    {
        // TODO Auto-generated method stub
        
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
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory)
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
