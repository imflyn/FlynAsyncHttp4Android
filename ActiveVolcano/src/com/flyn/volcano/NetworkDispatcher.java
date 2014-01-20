package com.flyn.volcano;

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

        NetworkResponse networkResponse = mNetwork.executeRequest(request);

        Response<?> response = request.parseNetworkResponse(networkResponse);

        mDelivery.postResponse(request, response);

    }
}
