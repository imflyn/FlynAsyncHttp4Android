package com.flyn.volcano;

import android.content.Context;

public class Volcano
{
    public final static int TYPE_HTTP_CLIENT     = 0;
    public final static int TYPE_HTTP_URLCONNECT = 1;

    public static NetStack newNetStack(int type, Context context)
    {
        switch (type)
        {
            case TYPE_HTTP_CLIENT:
                return new HttpClientStack(context);
            case TYPE_HTTP_URLCONNECT:
                return new HttpUrlStack(context);
            default:
                throw new IllegalArgumentException("Error argument type:" + type);
        }

    }

    public static NetStack newNetStack()
    {
        return newNetStack(TYPE_HTTP_URLCONNECT, null);
    }

    public static NetStack newNetStack(Context context)
    {
        return newNetStack(TYPE_HTTP_URLCONNECT, context);
    }

    public static NetStack newNetStack(int type)
    {
        return newNetStack(type, null);
    }

}
