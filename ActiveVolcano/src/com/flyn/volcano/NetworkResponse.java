package com.flyn.volcano;

import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;

public class NetworkResponse
{
    private final int                 statueCode;
    private final HttpEntity          httpEntity;
    private final boolean             notModified;
    private final Map<String, String> headers;

    protected NetworkResponse(int statueCode, HttpEntity httpEntity, Map<String, String> headers, boolean notModified)
    {
        this.statueCode = statueCode;
        this.httpEntity = httpEntity;
        this.notModified = notModified;
        this.headers = headers;
    }

    protected NetworkResponse(HttpEntity httpEntity)
    {
        this(HttpStatus.SC_OK, httpEntity, Collections.<String, String> emptyMap(), false);
    }

    protected NetworkResponse(HttpEntity httpEntity, Map<String, String> headers)
    {
        this(HttpStatus.SC_OK, httpEntity, headers, false);
    }

    protected final int getStatueCode()
    {
        return statueCode;
    }

    protected final HttpEntity getEntity()
    {
        return httpEntity;
    }

    protected final boolean isNotModified()
    {
        return notModified;
    }

    protected final Map<String, String> getHeaders()
    {
        return headers;
    }

}
