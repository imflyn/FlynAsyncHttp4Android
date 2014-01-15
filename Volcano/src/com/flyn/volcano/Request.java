package com.flyn.volcano;

import java.util.Map;

public class Request implements Comparable<Request>
{
    private int                 method;
    private String              url;
    private Map<String, String> headers;
    private RequestParams       params;
    private String              contentType;
    private int                 priority;
    private Object              tag;

    public final Object getTag()
    {
        return tag;
    }

    public final void setTag(Object tag)
    {
        this.tag = tag;
    }

    public final int getMethod()
    {
        return method;
    }

    public final void setMethod(int method)
    {
        this.method = method;
    }

    public final String getUrl()
    {
        return url;
    }

    public final void setUrl(String url)
    {
        this.url = url;
    }

    public final Map<String, String> getHeaders()
    {
        return headers;
    }

    public final void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public final RequestParams getParams()
    {
        return params;
    }

    public final void setParams(RequestParams params)
    {
        this.params = params;
    }

    public final String getContentType()
    {
        return contentType;
    }

    public final void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public final int getPriority()
    {
        return priority;
    }

    public final void setPriority(int priority)
    {
        this.priority = priority;
    }

    @Override
    public int compareTo(Request another)
    {
        int left = getPriority();
        int right = another.getPriority();
        return left == right ? -1 : this.getPriority() - another.getPriority();
    }

    public interface Priority
    {
        int LOW       = 0;
        int NORMAL    = 1;
        int HIGH      = 2;
        int IMMEDIATE = 3;
    }

    public interface Method
    {
        int DEPRECATED_GET_OR_POST = -1; // haven't be used
        int GET                    = 0;
        int POST                   = 1;
        int PUT                    = 2;
        int DELETE                 = 3;
        int HEAD                   = 4;
    }

}
