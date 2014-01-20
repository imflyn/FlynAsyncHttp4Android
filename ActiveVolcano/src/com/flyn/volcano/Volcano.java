package com.flyn.volcano;

import android.content.Context;

public class Volcano
{

    public static RequestQueue newRequestQueue(Context context, HttpStack httpStack)
    {
        NetWork netWork = new BasicNetwork();
        RequestQueue requestQueue = new RequestQueue(netWork);
        requestQueue.start();
        return requestQueue;
    }
}
