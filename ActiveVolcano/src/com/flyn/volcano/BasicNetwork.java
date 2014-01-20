package com.flyn.volcano;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;

public class BasicNetwork implements Network
{
    private static int          DEFAULT_POOL_SIZE = 4096;

    private final HttpStack     mHttpStack;
    private final ByteArrayPool mPool;

    public BasicNetwork(HttpStack httpStack)
    {
        this(httpStack, new ByteArrayPool(DEFAULT_POOL_SIZE));
    }

    public BasicNetwork(HttpStack httpStack, ByteArrayPool pool)
    {
        this.mHttpStack = httpStack;
        this.mPool = pool;
    }

    @Override
    public NetworkResponse executeRequest(Request<?> request, ResponseDelivery responseDelivery) throws IOException
    {
        Map<String, String> responseHeaders = new HashMap<String, String>();
        NetworkResponse networkResponse = null;
        HttpResponse httpResponse = null;
        byte[] responseData = null;
        int statusCode = -1;

        try
        {
            httpResponse = this.mHttpStack.performRequest(request, responseDelivery);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            responseHeaders = convertHeaders(httpResponse.getAllHeaders());

            if (httpResponse.getEntity() != null)
            {
                responseData = entityToBytes(httpResponse.getEntity(), responseDelivery);
            } else
            {
                responseData = new byte[0];
            }

            if (statusCode < 200 || statusCode > 299)
            {
                throw new IOException();
            }
            networkResponse = new NetworkResponse(statusCode, responseData, responseHeaders, false);
        } catch (SocketTimeoutException e)
        {
            throw new IOException("SocketTimeoutException:" + e.getMessage());
        } catch (ConnectTimeoutException e)
        {
            throw new IOException("ConnectTimeoutException:" + e.getMessage());
        } catch (MalformedURLException e)
        {
            throw new IOException("Bad URL " + request.getUrl() + e.getMessage());
        } catch (IOException e)
        {
            if (httpResponse != null)
            {
                statusCode = httpResponse.getStatusLine().getStatusCode();
            } else
            {
                throw new IOException("HttpResponse is null");
            }
            if (responseData != null)
            {
                networkResponse = new NetworkResponse(statusCode, responseData, responseHeaders, false);
                if (statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN)
                {
                    throw new IOException("Unauthorized error");
                } else
                {
                    throw new IOException("Server error");
                }
            } else
            {
                throw new IOException("ResponseContents is null");
            }
        }
        return networkResponse;
    }

    private byte[] entityToBytes(HttpEntity entity, ResponseDelivery responseDelivery) throws IOException
    {
        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(this.mPool, (int) entity.getContentLength());
        byte[] buffer = null;
        try
        {
            InputStream in = entity.getContent();
            if (in == null)
            {
                throw new IOException("InputStream is null when HttpEntityToBytes");
            }
            buffer = this.mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1)
            {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally
        {
            try
            {
                entity.consumeContent();
            } catch (IOException e)
            {
            }
            this.mPool.returnBuf(buffer);
            bytes.flush();
            bytes.close();
        }
    }

    private Map<String, String> convertHeaders(Header[] headers)
    {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++)
        {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
