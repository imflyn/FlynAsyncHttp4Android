package com.flyn.volcano;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StringRequest extends Request<String>
{

    public StringRequest(int method, String url, RequestParams requestPramas, Listener<String> mListener)
    {
        super(method, url, requestPramas,mListener);
    }

    public StringRequest(int method, String url, RequestParams requestPramas, int retryCount, Listener<String> mListener)
    {
        super(method, url, requestPramas, retryCount,mListener);
    }

    public StringRequest(String url, RequestParams requestPramas, int retryCount, Listener<String> mListener)
    {
        super(Method.GET, url, requestPramas, retryCount,mListener);
    }
    
    public StringRequest(String url, RequestParams requestPramas, Listener<String> mListener)
    {
        super(Method.GET, url, requestPramas, DEFAULT_RETRY_COUNT,mListener);
    }

    @Override
    protected Response<?> parseNetworkResponse(NetworkResponse response, ResponseDelivery responseDelivery) throws IOException
    {
        byte[] data = getData(response, responseDelivery);

        String result;
        try
        {
            result = new String(data, Utils.parseCharset(getHeaders()));
        } catch (UnsupportedEncodingException e)
        {
            result = new String(data);
        }
        return Response.build(result);
    }

}
