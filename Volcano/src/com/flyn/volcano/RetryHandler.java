package com.flyn.volcano;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

public class RetryHandler implements HttpRequestRetryHandler
{
    private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();
    private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

    static
    {
        exceptionWhitelist.add(NoHttpResponseException.class);
        exceptionWhitelist.add(UnknownHostException.class);
        exceptionWhitelist.add(SocketException.class);

        exceptionBlacklist.add(InterruptedException.class);
        exceptionBlacklist.add(SSLException.class);

    }

    private final int                maxRetries;
    private final int                retrySleepTimeMS;

    public RetryHandler(int maxRetries, int retrySleepTimeMS)
    {
        this.maxRetries = maxRetries;
        this.retrySleepTimeMS = retrySleepTimeMS;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context)
    {
        boolean retry = true;
        Boolean b = (Boolean) context.getAttribute(ExecutionContext.HTTP_REQ_SENT);
        boolean sent = (b != null && b);
        if (executionCount > this.maxRetries)
        {
            retry = false;
        } else if (isInList(exceptionBlacklist, exception))
        {
            retry = false;
        } else if (isInList(exceptionWhitelist, exception))
        {
            retry = true;
        } else if (!sent)
        {
            retry = true;
        }
        if (retry)
        {
            HttpUriRequest currentRequest = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);

            if (currentRequest == null)
                return false;
        }

        if (retry)
            SystemClock.sleep(this.retrySleepTimeMS);
        else
            exception.printStackTrace();

        return retry;
    }

    static void addClassToWhitelist(Class<?> cls)
    {
        exceptionWhitelist.add(cls);
    }

    static void addClassToBlacklist(Class<?> cls)
    {
        exceptionBlacklist.add(cls);
    }

    private boolean isInList(HashSet<Class<?>> list, Throwable error)
    {
        for (Class<?> cls : list)
        {
            if (cls.isInstance(error))
                return true;
        }
        return false;
    }

}
