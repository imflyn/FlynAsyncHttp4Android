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
}
