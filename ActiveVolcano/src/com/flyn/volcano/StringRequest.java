package com.flyn.volcano;

import java.io.UnsupportedEncodingException;

public class StringRequest extends Request<String>
{
    private final Listener<String> mListener;
    
    public StringRequest(int method, String url, RequestParams requestPramas,Listener<String> mListener)
    {
        super(method, url, requestPramas);
        this.mListener=mListener;
    }
   
    public StringRequest(int method, String url, RequestParams requestPramas, int retryCount,Listener<String> mListener)
    {
        super(method, url, requestPramas, retryCount);
        this.mListener=mListener;
    }

    public StringRequest(String url, RequestParams requestPramas, int retryCount,Listener<String> mListener)
    {
        super(Method.GET, url, requestPramas, retryCount);
        this.mListener=mListener;
    }
    

    @Override
    protected void deliverResponse(String response)
    {
//        this.mListener.onResponse(response);
    }

    @Override
    protected Response<String> getData(byte[] data)
    {
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
