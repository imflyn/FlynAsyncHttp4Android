package com.flyn.volcano;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public abstract class StringResponseHandler extends HttpResponseHandler
{
    public StringResponseHandler()
    {
        super();
    }

    @Override
    protected void onSuccess(int statusCode, Map<String, String> headers, byte[] responseBody)
    {
        String contentType = Utils.parseCharset(headers);
        try
        {
            onSuccess(statusCode, new String(responseBody, contentType));
        } catch (UnsupportedEncodingException e)
        {
            onFailure(statusCode, headers, responseBody, e);
        }
    }

    public abstract void onSuccess(int status, String content);

}
