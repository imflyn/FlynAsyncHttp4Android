package com.flyn.volcano;

import java.util.Collections;
import java.util.Map;

import org.apache.http.HttpStatus;

public class NetworkResponse
{
    private final int                 statueCode;
    private final byte[]              data;
    private final boolean             notModified;
    private final Map<String, String> headers;

    protected NetworkResponse(int statueCode, byte[] data, Map<String, String> headers, boolean notModified)
    {
        this.statueCode = statueCode;
        this.data = data;
        this.notModified = notModified;
        this.headers = headers;
    }

    protected NetworkResponse(byte[] data)
    {
        this(HttpStatus.SC_OK, data, Collections.<String, String> emptyMap(), false);
    }

    protected NetworkResponse(byte[] data, Map<String, String> headers)
    {
        this(HttpStatus.SC_OK, data, headers, false);
    }

    protected final int getStatueCode()
    {
        return statueCode;
    }

    protected final byte[] getData()
    {
        return data;
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
