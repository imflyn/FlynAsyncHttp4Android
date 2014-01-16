package com.flyn.volcano;

import android.content.Context;

public class Volcano
{
    public final static int TYPE_HTTP_CLIENT     = 0;
    public final static int TYPE_HTTP_URLCONNECT = 1;

    public static NetStack newNetStack(int type, Context context, boolean isUseSynchronousMode)
    {
        switch (type)
        {
            case TYPE_HTTP_CLIENT:
                return new HttpClientStack(context, isUseSynchronousMode);
            case TYPE_HTTP_URLCONNECT:
                return new HttpUrlStack(context, isUseSynchronousMode);
            default:
                throw new IllegalArgumentException("Error argument type:" + type);
        }

    }

    public static NetStack newNetStack(Context context)
    {
        return newNetStack(TYPE_HTTP_URLCONNECT, context, false);
    }

    public static NetStack newNetStack(int type)
    {
        return newNetStack(type, null, false);
    }

}
