package com.flyn.volcano;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class NetworkDispatcher extends Thread
{
    private Network                   mNetwork;
    private ResponseDelivery          mDelivery;
    private BlockingQueue<Request<?>> mQueue;
    private volatile boolean          mQuit = false;

    @Override
    public void run()
    {
        Request<?> request = null;
        try
        {

            request = mQueue.take();

        } catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }

        NetworkResponse networkResponse = null;
        try
        {
            networkResponse = mNetwork.executeRequest(request, mDelivery);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        Response<?> response = request.parseNetworkResponse(networkResponse);

        mDelivery.postResponse(request, response);

    }
}
