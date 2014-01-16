package com.flyn.volcano;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class HttpUrlRequest extends Request
{
    private final HttpURLConnection connection;
    private final RequestParams     requestParams;

    public HttpUrlRequest(HttpURLConnection connection, RequestParams requestParams, IResponseHandler responseHandler)
    {
        super(responseHandler);
        this.connection = connection;
        this.requestParams = requestParams;
    }

    @Override
    protected void makeRequest() throws IOException
    {
        if (isCancelled())
            return;

        if (null != this.requestParams)
        {
            this.connection.setDoOutput(true);
            HttpEntity httpEntity = this.requestParams.getEntity(this.responseHandler);
            OutputStream outStream = this.connection.getOutputStream();
            httpEntity.writeTo(outStream);
        }

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = this.connection.getResponseCode();
        if (responseCode == -1)
        {
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        StatusLine responseStatus = new BasicStatusLine(protocolVersion, this.connection.getResponseCode(), this.connection.getResponseMessage());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        response.setEntity(entityFromConnection(this.connection));

        Map<String, String> headersMap = new HashMap<String, String>();
        for (Entry<String, List<String>> header : this.connection.getHeaderFields().entrySet())
        {
            if (header.getKey() != null)
            {
                String key = header.getKey();
                String value = header.getValue().get(0);
                headersMap.put(key, value);
                Header h = new BasicHeader(key, value);
                response.addHeader(h);
            }
        }

        Map<String, List<String>> headerFields = this.connection.getHeaderFields();
        if (headerFields != null)
        {
            List<String> cookies = headerFields.get("set-cookie");
            if (cookies != null)
            {
                CookieManager cookieManager = CookieManager.getInstance();
                for (String cookie : cookies)
                {
                    if (cookie == null)
                        continue;
                    cookieManager.setCookie(this.connection.getURL().toString(), cookie);
                }
                if (Build.VERSION.SDK_INT >= 14)
                    CookieSyncManager.getInstance().sync();
            }
        }

        if (!isCancelled() && null != this.responseHandler)
        {
            this.responseHandler.setRequestHeaders(headersMap);
            try
            {
                this.responseHandler.setRequestURI(connection.getURL().toURI());
            } catch (URISyntaxException e)
            {
                Log.e(HttpUrlRequest.class.getName(), "URISyntaxException:", e);
            }
        }
        if (!isCancelled() && this.responseHandler != null)
            this.responseHandler.sendResponseMessage(response);

    }

    @Override
    protected void makeRequestWithRetries() throws IOException
    {
        IOException cause = null;

        if (this.isCancelled)
            return;

        try
        {
            makeRequest();
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
        return isCancelled();
    }

    private HttpEntity entityFromConnection(HttpURLConnection connection)
    {
        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try
        {
            inputStream = connection.getInputStream();
        } catch (IOException ioe)
        {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }
}
