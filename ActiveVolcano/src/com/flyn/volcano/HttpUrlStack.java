package com.flyn.volcano;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.flyn.volcano.Request.Method;

public class HttpUrlStack implements HttpStack
{

    private static final String    HEADER_CONTENT_TYPE        = "Content-Type";
    private static final int       DEFAULT_SOCKET_TIMEOUT     = 10 * 1000;
    
    private final Context          context;
    private final SSLSocketFactory mSslSocketFactory;

    private int                    timeout                    = DEFAULT_SOCKET_TIMEOUT;
    private boolean                isEnableRedirects          = false;
    private boolean                fixNoHttpResponseException = false;
    private boolean                isAccpetCookies            = true;
    private String                 mBasicAuth                 = null;
    private String                 userAgent                  = null;
    private Proxy                  mProxy                     = null;

    public HttpUrlStack(Context context)
    {
        this(context, null);
    }

    public HttpUrlStack(Context context, SSLSocketFactory sslSocketFactory)
    {
        this.mSslSocketFactory = sslSocketFactory;
        this.context = context;
        initCookieManager(context);
    }

    private void initCookieManager(Context context)
    {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeExpiredCookie();
    }

    @Override
    public HttpResponse performRequest(Request<?> request, ResponseDelivery responseDelivery) throws IOException
    {
        String url = request.getUrl();
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.putAll(request.getHeaders());

        HttpURLConnection connection = openConnection(url, request);
        addHeaders(headerMap, connection);
        setParams(request, connection, responseDelivery);

        return null;
    }

    private HttpURLConnection openConnection(String parsedUrl, Request<?> request) throws IOException
    {
        HttpURLConnection connection = null;
        URL url = new URL(parsedUrl);

        // 对移动wap网络的特殊处理
        if (Utils.CMMAP_Request(this.context))
        {
            String myURLStr = "http://10.0.0.172".concat(url.getPath());
            String query = url.getQuery();
            if (query != null)
                myURLStr = myURLStr.concat("?").concat(query);
            url = new URL(myURLStr);
        }

        if (null != this.mProxy)
            connection = (HttpURLConnection) url.openConnection(this.mProxy);
        else
            connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(this.timeout);
        connection.setReadTimeout(this.timeout);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setRequestProperty("Charsert", HTTP.UTF_8);
        connection.setRequestProperty("Connection", "Keep-Alive");

        if (this.isAccpetCookies)
            connection.setRequestProperty("Cookie", getCookies(parsedUrl));

        if (Utils.CMMAP_Request(this.context))
            connection.addRequestProperty("X-Online-Host", parsedUrl);

        if (!TextUtils.isEmpty(this.userAgent))
            connection.setRequestProperty("User-Agent", this.userAgent);
        else
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

        if (!TextUtils.isEmpty(this.mBasicAuth))
            connection.setRequestProperty("Authorization", this.mBasicAuth);

        HttpURLConnection.setFollowRedirects(this.isEnableRedirects);
        connection.setInstanceFollowRedirects(this.isEnableRedirects);

        if ("https".equals(url.getProtocol()) && this.mSslSocketFactory == null)
        {
            SSLSocketFactory sslSocketFactory = null;
            if (null != this.mSslSocketFactory)
                sslSocketFactory = this.mSslSocketFactory;
            else
            {
                if (this.fixNoHttpResponseException)
                    sslSocketFactory = HttpUrlSSLSocketFactory.getFixedSocketFactory();
                else
                    sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            }
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
        }

        return connection;
    }

    private void addHeaders(Map<String, String> headers, HttpURLConnection connection)
    {
        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            connection.addRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    private void setParams(Request<?> request, HttpURLConnection connection, ResponseDelivery responseDelivery) throws ProtocolException
    {
        switch (request.getMethod())
        {
            case Method.GET:
                connection.setRequestMethod("GET");
                break;
            case Method.HEAD:
                connection.setRequestMethod("HEAD");
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                uploadIfNeeded(request, connection, responseDelivery);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                uploadIfNeeded(request, connection, responseDelivery);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private void uploadIfNeeded(Request<?> request, HttpURLConnection connection, ResponseDelivery responseDelivery)
    {
        // byte[] body = request.getBody();
        // if (body != null)
        // {
        // connection.setDoOutput(true);
        // connection.addRequestProperty(HEADER_CONTENT_TYPE,
        // request.getBodyContentType());
        // DataOutputStream out = new
        // DataOutputStream(connection.getOutputStream());
        // out.write(body);
        // out.close();
        // }
    }

    public void setProxy(String host, int port)
    {
        if (!TextUtils.isEmpty(host) && port > 0)
            this.mProxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
    }

    public void setProxy(String host, int port, String username, String password)
    {
        if (!TextUtils.isEmpty(host) && port > 0)
            this.mProxy = new Proxy(Type.HTTP, new InetSocketAddress(host, port));
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
            this.mBasicAuth = Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
    }

    public void clearBasicAuth()
    {
        this.mBasicAuth = null;
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

    public void setEnableRedirects(boolean enableRedirects)
    {
        this.isEnableRedirects = enableRedirects;
    }

    public void setUserAgent(String userAgent)
    {
        if (!TextUtils.isEmpty(userAgent))
            this.userAgent = userAgent;
    }

    public void setTimeOut(int timeout)
    {
        if (timeout > 0)
            this.timeout = timeout;
        else
            this.timeout = DEFAULT_SOCKET_TIMEOUT;
    }

    protected void setFixNoHttpResponseException(boolean fixNoHttpResponseException)
    {
        this.fixNoHttpResponseException = fixNoHttpResponseException;
    }

}
