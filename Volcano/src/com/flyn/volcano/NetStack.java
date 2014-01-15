package com.flyn.volcano;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;

import com.flyn.volcano.Request.Method;

public abstract class NetStack
{
    protected static final int                  DEFAULT_MAX_CONNETIONS          = 10;
    protected static final int                  DEFAULT_SOCKET_TIMEOUT          = 10 * 1000;
    protected static final int                  DEFAULT_MAX_RETRIES             = 3;
    protected static final int                  DEFAULT_RETRY_SLEEP_TIME_MILLIS = 1500;
    protected static final int                  DEFAULT_SOCKET_BUFFER_SIZE      = 8192;
    protected static final String               HEADER_ACCEPT_ENCODING          = "Accept-Encoding";
    protected static final String               HEADER_CONTENT_TYPE             = "Content-Type";
    protected static final String               ENCODING_GZIP                   = "gzip";
    protected static int                        httpPort                        = 80;
    protected static int                        httpsPort                       = 443;

    protected final Context                     context;
    protected final boolean                     fixNoHttpResponseException      = false;
    protected ExecutorService                   threadPool;
    protected int                               maxConnections                  = DEFAULT_MAX_CONNETIONS;
    protected int                               timeout                         = DEFAULT_SOCKET_TIMEOUT;
    protected Map<Context, List<RequestFuture>> requestMap;
    protected Map<String, String>               httpHeaderMap;
    protected boolean                           isURLEncodingEnabled            = true;

    protected NetStack(Context context)
    {
        this.threadPool = Executors.newCachedThreadPool();
        this.requestMap = new WeakHashMap<Context, List<RequestFuture>>();
        this.httpHeaderMap = new HashMap<String, String>();
        this.context = context;
    }

    protected abstract RequestFuture sendRequest(String contentType, IResponseHandler responseHandler);

    protected abstract void addHeaders(Map<String, String> headers);

    protected abstract void get(String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    protected abstract void post(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler);

    protected abstract void delete(String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler);

    protected abstract void put(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler);

    protected abstract void head(String url, Map<String, String> headers, RequestParams params, String contentType, IResponseHandler responseHandler);

    public RequestFuture makeRequest(int method, String contentType, String url, Map<String, String> headers, RequestParams params, IResponseHandler responseHandler)
    {

        switch (method)
        {
            case Method.GET:
                get(url, headers, params, responseHandler);
                break;
            case Method.POST:
                post(url, headers, params, contentType, responseHandler);
                break;
            case Method.PUT:
                put(url, headers, params, contentType, responseHandler);
                break;
            case Method.DELETE:
                delete(url, headers, params, responseHandler);
                break;
            case Method.HEAD:
                head(url, headers, params, contentType, responseHandler);
                break;
            default:
                throw new IllegalStateException("Unknown request method.");
        }
        addHeaders(headers);
        return sendRequest(contentType, responseHandler);
    }

    public int getMaxConnections()
    {
        return this.maxConnections;
    }

    public void addHeader(String header, String value)
    {
        this.httpHeaderMap.put(header, value);
    }

    public void removeHeader(String header)
    {
        this.httpHeaderMap.remove(header);

    }

    public void setURLEncodingEnabled(boolean isURLEncodingEnabled)
    {
        this.isURLEncodingEnabled = isURLEncodingEnabled;
    }

    public void cancelRequests(Context context, boolean mayInterruptIfRunning)
    {

        List<RequestFuture> requestList = this.requestMap.get(context);
        if (requestList != null)
        {
            for (RequestFuture requestHandle : requestList)
            {
                requestHandle.cancel(mayInterruptIfRunning);
            }
            this.requestMap.remove(context);
        }
    }

    /**
     * Set it before request started
     * 
     * @param threadPool
     */
    public void setThreadPool(ThreadPoolExecutor threadPool)
    {
        this.threadPool = threadPool;
    }

    public int timeOut()
    {
        return this.timeout;
    }

    public abstract void setCookieStore(CookieStore cookieStore);

    public abstract void setEnableRedirects(final boolean enableRedirects);

    public abstract void setUserAgent(String userAgent);

    public abstract void setMaxConnections(int maxConnections);

    public abstract void setTimeOut(int timeout);

    public abstract void setProxy(String hostname, int port);

    public abstract void setProxy(String hostname, int port, String username, String password);

    public abstract void setSSLSocketFactory(SSLSocketFactory sslSocketFactory);

    public abstract void setMaxRetriesAndTimeout(int retries, int timeout);

    public abstract void setBasicAuth(String username, String password, AuthScope authScope);

    public abstract void setBasicAuth(String username, String password);

    public abstract void clearBasicAuth();

}
