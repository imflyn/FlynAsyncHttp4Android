package com.flyn.volcano;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.auth.AuthScope;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.flyn.volcano.Request.Method;

public class HttpUrlStack extends NetStack
{

    private static final String TAG               = HttpUrlStack.class.getName();
    private boolean             isEnableRedirects = false;
    private SSLSocketFactory    mSslSocketFactory;
    private Proxy               proxy;

    public HttpUrlStack(Context context)
    {
        super(context);

        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeExpiredCookie();

    }

    private HttpURLConnection openConnection(String parsedUrl, IResponseHandler responseHandler)
    {
        URL url = null;
        HttpURLConnection connection = null;
        try
        {
            url = new URL(parsedUrl);
            if (null != this.proxy)
                connection = (HttpURLConnection) url.openConnection(this.proxy);
            else
                connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e)
        {
            if (responseHandler != null)
                responseHandler.sendFailureMessage(0, null, null, e);
            else
                e.printStackTrace();
        }

        connection.setConnectTimeout(this.timeout);
        connection.setReadTimeout(this.timeout);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        HttpURLConnection.setFollowRedirects(this.isEnableRedirects);
        connection.setInstanceFollowRedirects(this.isEnableRedirects);

        if ("https".equals(url.getProtocol()) && mSslSocketFactory == null)
        {
            if (this.fixNoHttpResponseException)
                this.mSslSocketFactory = HttpUrlSSLSocketFactory.getFixedSocketFactory();
            else
                this.mSslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        } else
            ((HttpsURLConnection) connection).setSSLSocketFactory(this.mSslSocketFactory);
        // Cookies

        return connection;
    }

    @Override
    protected RequestFuture sendRequest(String contentType, IResponseHandler responseHandler, Object[] objs)
    {
        HttpURLConnection connection = (HttpURLConnection) objs[0];
        RequestParams requestParams = (RequestParams) objs[1];

        if (!TextUtils.isEmpty(contentType))
            connection.addRequestProperty(HEADER_CONTENT_TYPE, contentType);

        // responseHandler.setRequestURI(uriRequest.getURI());

        Request request = new HttpUrlRequest(connection, requestParams, responseHandler);
        RequestFuture requestFuture = new RequestFuture(request);

        this.threadPool.submit(request);

        if (null != this.context)
        {
            List<RequestFuture> list = this.requestMap.get(this.context);
            if (null == list)
            {
                list = new LinkedList<RequestFuture>();
                this.requestMap.put(this.context, list);
            }
            list.add(requestFuture);

            Iterator<RequestFuture> iterator = list.iterator();
            while (iterator.hasNext())
            {
                if (iterator.next().shouldBeGarbageCollected())
                    iterator.remove();
            }
        }
        return requestFuture;
    }

    @Override
    public RequestFuture makeRequest(int method, String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {
        HttpURLConnection urlConnection = openConnection(url, responseHandler);
        try
        {
            switch (method)
            {
                case Method.GET:
                    urlConnection.setRequestMethod("GET");
                    break;
                case Method.POST:
                    urlConnection.setRequestMethod("POST");
                    break;
                case Method.PUT:
                    urlConnection.setRequestMethod("PUT");
                    break;
                case Method.DELETE:
                    urlConnection.setRequestMethod("DELETE");
                    break;
                case Method.HEAD:
                    urlConnection.setRequestMethod("HEAD");
                    break;
                default:
                    throw new IllegalStateException("Unknown request method.");
            }
        } catch (ProtocolException e)
        {
            if (responseHandler != null)
                responseHandler.sendFailureMessage(0, null, null, e);
            else
                e.printStackTrace();
        }

        addHeaders(urlConnection, headers);
//        responseHandler.setRequestHeaders(headers);
        return sendRequest(contentType, responseHandler, prepareArguments(urlConnection, params));
    }

    private void addHeaders(HttpURLConnection urlConnection, Map<String, String> headers)
    {
        for (Entry<String, String> header : headers.entrySet())
        {
            urlConnection.addRequestProperty(header.getKey(), header.getValue());
        }

    }

    private Object[] prepareArguments(HttpURLConnection urlConnection, RequestParams params)
    {
        return new Object[] { urlConnection, params };
    }

    @Override
    public void setEnableRedirects(boolean enableRedirects)
    {
        this.isEnableRedirects = enableRedirects;
    }

    @Override
    public void setUserAgent(String userAgent)
    {
        
    }

    @Override
    public void setMaxConnections(int maxConnections)
    {
        this.maxConnections = maxConnections;
    }

    @Override
    public void setTimeOut(int timeout)
    {
        this.timeout = timeout;
    }

    @Override
    public void setProxy(String host, int port)
    {
        this.proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
    }

    @Override
    public void setProxy(String host, int port, String username, String password)
    {
        this.proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
        Properties propRet = null;
        if (!TextUtils.isEmpty(host))
        {
            propRet = System.getProperties();
            propRet.setProperty("http.proxyHost", host);
            propRet.setProperty("http.proxyPort", String.valueOf(port));
            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password))
            {
                propRet.setProperty("http.proxyUser", username);
                propRet.setProperty("http.proxyPassword", password);
            }
        }
    }

    @Override
    public void setMaxRetriesAndTimeout(int retries, int timeout)
    {
           
    }

    @Override
    public void setBasicAuth(String username, String password, AuthScope authScope)
    {

    }

    @Override
    public void setBasicAuth(String username, String password)
    {

    }

    @Override
    public void clearBasicAuth()
    {

    }

}
