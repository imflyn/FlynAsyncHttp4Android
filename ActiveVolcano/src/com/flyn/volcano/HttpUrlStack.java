package com.flyn.volcano;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class HttpUrlStack implements HttpStack
{

    private static final String    HEADER_CONTENT_TYPE = "Content-Type";

    private final Context          context;
    private final SSLSocketFactory mSslSocketFactory;

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
    public HttpResponse performRequest(Request<?> request)
    {
        String url = request.getUrl();
        HashMap<String, String> headerMap = new HashMap<String, String>();
        headerMap.putAll(request.getHeaders());

        URL parsedUrl = new URL(url);

        return null;
    }

    private HttpURLConnection openConnection(String parsedUrl, Request<?> request)
    {
        URL url;
        HttpURLConnection connection = null;

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

        return httpURLConnection;
    }

}
