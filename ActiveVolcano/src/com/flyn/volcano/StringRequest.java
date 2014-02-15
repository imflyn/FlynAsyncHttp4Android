package com.flyn.volcano;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StringRequest extends Request<String>
{

    public StringRequest(int method, String url, RequestParams requestPramas, Listener mListener)
    {
        super(method, url, requestPramas, mListener);
    }

    public StringRequest(int method, String url, RequestParams requestPramas, int retryCount, Listener mListener)
    {
        super(method, url, requestPramas, retryCount, mListener);
    }

    public StringRequest(String url, RequestParams requestPramas, int retryCount, Listener mListener)
    {
        super(Method.GET, url, requestPramas, retryCount, mListener);
    }

    public StringRequest(String url, RequestParams requestPramas, Listener mListener)
    {
        super(Method.GET, url, requestPramas, DEFAULT_RETRY_COUNT, mListener);
    }

    @Override
    protected Response<?> parseNetworkResponse(NetworkResponse response, ResponseDelivery responseDelivery) throws IOException
    {
        byte[] data = getData(response, responseDelivery);

        String result;
        String charset = Utils.parseCharset(response.getHeaders());
        try
        {
            result = new String(data, charset);
        } catch (UnsupportedEncodingException e)
        {
            result = new String(data);
        }
        return Response.build(result);
    }

}
