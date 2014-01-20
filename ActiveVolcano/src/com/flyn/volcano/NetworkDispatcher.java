package com.flyn.volcano;

import java.util.concurrent.BlockingQueue;

import android.os.Process;

public class NetworkDispatcher extends Thread
{
    private final Network                   mNetwork;
    private final ResponseDelivery          mDelivery;
    private final BlockingQueue<Request<?>> mQueue;
    private volatile boolean                mQuit = false;

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, Network network, ResponseDelivery delivery)
    {
        this.mQueue = queue;
        this.mNetwork = network;
        this.mDelivery = delivery;
    }

    @Override
    public void run()
    {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Request<?> request = null;

        while (true)
        {
            try
            {
                request = this.mQueue.take();
            } catch (InterruptedException e)
            {
                if (this.mQuit)
                    return;

                continue;
            }

            if (request.isCanceled())
            {
                request.finish();
                this.mDelivery.sendCancleMessage(request);
                continue;
            }
            for (int retryCount = 0; retryCount < request.getRetryCount(); retryCount++)
            {
                try
                {
                    NetworkResponse networkResponse = this.mNetwork.executeRequest(request, this.mDelivery);

                    if (networkResponse.isNotModified() && request.hasHadResponseDelivered())
                    {
                        request.finish();
                        this.mDelivery.sendFinishMessage(request);
                        continue;
                    }

                    Response<?> response=request.getData( request.parseNetworkResponse(networkResponse,this.mDelivery));
                    request.markDelivered();
                    request.finish();
                    this.mDelivery.sendSuccessMessage(request, response);

                    break;
                } catch (Exception e)
                {
                    this.mDelivery.sendFailureMessage(request, e);
                }
            }

            this.mDelivery.sendFinishMessage(request);
        }
    }

    public void quit()
    {
        this.mQuit = true;
        interrupt();
    }
}
