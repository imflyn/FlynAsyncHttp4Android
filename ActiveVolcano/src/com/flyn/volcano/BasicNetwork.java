package com.flyn.volcano;

import java.io.IOException;
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

    private final HttpStack mHttpStack;

    public BasicNetwork(HttpStack httpStack)
    {
        this.mHttpStack = httpStack;
    } 
    
    @Override
    public NetworkResponse executeRequest(final Request<?> request, final ResponseDelivery responseDelivery) throws IOException
    {
        Map<String, String> responseHeaders = new HashMap<String, String>();
        NetworkResponse networkResponse = null;
        HttpResponse httpResponse = null;
        HttpEntity entity = null;
        int statusCode = -1;

        try
        {
            httpResponse = this.mHttpStack.performRequest(request, responseDelivery);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            responseHeaders = convertHeaders(httpResponse.getAllHeaders());

            entity = httpResponse.getEntity();

            if (statusCode < 200 || statusCode > 299)
            {
                throw new IOException("Error statusCode:" + statusCode);
            }
            if (entity == null)
            {
                throw new IOException("ExecuteRequesting HttpEntity is null.");
            }
            networkResponse = new NetworkResponse(statusCode, entity, responseHeaders, false);
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
                throw new IOException("HttpResponse is null:" + e.getMessage());
            }
            if (entity != null)
            {
                networkResponse = new NetworkResponse(statusCode, entity, responseHeaders, false);
                if (statusCode == HttpStatus.SC_UNAUTHORIZED || statusCode == HttpStatus.SC_FORBIDDEN)
                {
                    throw new IOException("Unauthorized error and statusCode:" + statusCode);
                } else
                {
                    throw new IOException("Server error and statusCode:" + statusCode);
                }
            } else
            {
                throw new IOException("ResponseContents is null");
            }
        }
        return networkResponse;
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
