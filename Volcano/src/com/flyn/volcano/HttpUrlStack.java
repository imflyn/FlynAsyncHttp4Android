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

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.flyn.volcano.Request.Method;

public class HttpUrlStack extends NetStack
{

    private static final String TAG               = HttpUrlStack.class.getName();
    private boolean             isEnableRedirects = false;
    private SSLSocketFactory    mSslSocketFactory;
    private Proxy               proxy;
    private String              userAgent;
    private String              basicAuth;
    private boolean             isAccpetCookies   = true;

    protected HttpUrlStack(Context context, boolean isUseSynchronousMode)
    {
        super(context, isUseSynchronousMode);

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

            // 对移动wap网络的特殊处理
            if (Utils.CMMAP_Request(this.context))
            {
                String myURLStr = "http://10.0.0.172".concat(url.getPath());
                String query = url.getQuery();
                if (query != null)
                    myURLStr = myURLStr.concat("?").concat(query);
                url = new URL(myURLStr);
            }

            if (null != this.proxy)
                connection = (HttpURLConnection) url.openConnection(this.proxy);
            else
                connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e)
        {
            if (responseHandler != null)
                responseHandler.sendFailureMessage(0, null, null, e);
            else
                Log.e(TAG, "IOException:", e);
        }

        connection.setConnectTimeout(this.timeout);
        connection.setReadTimeout(this.timeout);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        if (this.isAccpetCookies)
            connection.setRequestProperty("Cookie", getCookies(parsedUrl));
        if (Utils.CMMAP_Request(this.context))
            connection.addRequestProperty("X-Online-Host", parsedUrl);
        if (!TextUtils.isEmpty(this.userAgent))
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        else
            connection.setRequestProperty("User-Agent", this.userAgent);
        if (!TextUtils.isEmpty(this.basicAuth))
            connection.setRequestProperty("Authorization", this.basicAuth);

        HttpURLConnection.setFollowRedirects(this.isEnableRedirects);
        connection.setInstanceFollowRedirects(this.isEnableRedirects);

        if ("https".equals(url.getProtocol()) && this.mSslSocketFactory == null)
        {
            if (this.fixNoHttpResponseException)
                this.mSslSocketFactory = HttpUrlSSLSocketFactory.getFixedSocketFactory();
            else
                this.mSslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

            ((HttpsURLConnection) connection).setSSLSocketFactory(this.mSslSocketFactory);
        }

        return connection;
    }

    @Override
    protected RequestFuture sendRequest(String contentType, IResponseHandler responseHandler, Object[] objs)
    {
        HttpURLConnection connection = (HttpURLConnection) objs[0];
        RequestParams requestParams = (RequestParams) objs[1];

        if (!TextUtils.isEmpty(contentType))
            connection.addRequestProperty(HEADER_CONTENT_TYPE, contentType);

        Request request = new HttpUrlRequest(connection, requestParams, responseHandler);
        RequestFuture requestFuture = new RequestFuture(request);

        if (!this.isUseSynchronousMode)
            this.threadPool.submit(request);
        else
            request.run();

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
        HttpURLConnection urlConnection = null;
        String requestMethod = "GET";
        String normalUrl = url;
        try
        {
            switch (method)
            {
                case Method.GET:
                    requestMethod = "GET";
                    // normalUrl=Utils.getUrlWithParams(this.isURLEncodingEnabled,
                    // url, params);
                    break;
                case Method.POST:
                    requestMethod = "POST";
                    // normalUrl=params.getParamString();
                    break;
                case Method.PUT:
                    requestMethod = "PUT";
                    // normalUrl=params.getParamString();
                    break;
                case Method.DELETE:
                    requestMethod = "DELETE";
                    // normalUrl=Utils.getUrlWithParams(this.isURLEncodingEnabled,
                    // url, params);
                    break;
                case Method.HEAD:
                    requestMethod = "HEAD";
                    // normalUrl=Utils.getUrlWithParams(this.isURLEncodingEnabled,
                    // url, params);
                    break;
                default:
                    throw new IllegalStateException("Unknown request method.");
            }
            urlConnection = openConnection(normalUrl, responseHandler);
            urlConnection.setRequestMethod(requestMethod);
        } catch (ProtocolException e)
        {
            if (responseHandler != null)
                responseHandler.sendFailureMessage(0, null, null, e);
            else
                Log.e(TAG, "ProtocolException:", e);
        }
        if(null!=headers)
        addHeaders(urlConnection, headers);

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
        if (!TextUtils.isEmpty(userAgent))
            this.userAgent = userAgent;
    }

    @Override
    public void setMaxConnections(int maxConnections)
    {
        if (timeout > 0)
            this.maxConnections = maxConnections;
        else
            this.timeout = DEFAULT_MAX_CONNETIONS;
    }

    @Override
    public void setTimeOut(int timeout)
    {
        if (timeout > 0)
            this.timeout = timeout;
        else
            this.timeout = DEFAULT_SOCKET_TIMEOUT;
    }

    @Override
    public void setProxy(String host, int port)
    {
        if (!TextUtils.isEmpty(host) && port > 0)
            this.proxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
    }

    @Override
    public void setProxy(String host, int port, String username, String password)
    {
        if (!TextUtils.isEmpty(host) && port > 0)
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

    public void setBasicAuth(String username, String password)
    {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password))
            this.basicAuth = Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
    }

    public void clearBasicAuth()
    {
        this.basicAuth = null;
    }

    public void setAcceptCookie(boolean accept)
    {
        this.isAccpetCookies = accept;
    }

    public String getCookies(String url)
    {
        if (!this.isAccpetCookies || url == null)
            return null;
        return CookieManager.getInstance().getCookie(url);
    }

}
