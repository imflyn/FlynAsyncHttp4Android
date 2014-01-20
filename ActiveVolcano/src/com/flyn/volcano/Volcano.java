package com.flyn.volcano;

import android.content.Context;

public class Volcano
{

    public static RequestQueue newRequestQueue(Context context)
    {
        Network network = new BasicNetwork(new HttpUrlStack(context));
        RequestQueue requestQueue = new RequestQueue(network);
        requestQueue.start();
        return requestQueue;
    }

    public static RequestQueue newRequestQueue(Context context, HttpStack httpStack)
    {
        Network network = new BasicNetwork(httpStack);
        RequestQueue requestQueue = new RequestQueue(network);
        requestQueue.start();
        return requestQueue;
    }
}
