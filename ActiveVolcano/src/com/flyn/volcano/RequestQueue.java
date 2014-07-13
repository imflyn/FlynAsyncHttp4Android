package com.flyn.volcano;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Looper;

public class RequestQueue
{
    private static final int                        DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private AtomicInteger                           mSequenceGenerator               = new AtomicInteger();

    private final Set<Request<?>>                   mCurrentRequests                 = new HashSet<Request<?>>();
    private final PriorityBlockingQueue<Request<?>> mNetworkQueue                    = new PriorityBlockingQueue<Request<?>>();

    private final Network                           mNetwork;
    private final ResponseDelivery                  mResponseDelivery;
    private final NetworkDispatcher[]               mDispatchers;

    public RequestQueue(Network netWork, int threadPoolSize, ResponseDelivery responseDelivery)
    {
        this.mNetwork = netWork;
        this.mResponseDelivery = responseDelivery;
        this.mDispatchers = new NetworkDispatcher[threadPoolSize];
    }

    public RequestQueue(Network network, int threadPoolSize)
    {
        this(network, threadPoolSize, new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RequestQueue(Network network)
    {
        this(network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
    }

    public void start()
    {
        stop();
        for (int i = 0; i < this.mDispatchers.length; i++)
        {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(this.mNetworkQueue, this.mNetwork, this.mResponseDelivery);
            this.mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    public void stop()
    {
        cancelAll();
        for (int i = 0; i < this.mDispatchers.length; i++)
        {
            if (this.mDispatchers[i] != null)
                this.mDispatchers[i].quit();
        }
    }

    public void cancelAll()
    {
        synchronized (this.mCurrentRequests)
        {
            for (Request<?> request : this.mCurrentRequests)
            {
                request.cancel();
            }
        }
    }

    public void cancel(Object tag)
    {
        synchronized (this.mCurrentRequests)
        {
            for (Request<?> request : this.mCurrentRequests)
            {
                if (request.getTag() == tag)
                {
                    request.cancel();
                }
            }
        }
    }

    public Request<?> add(Request<?> request)
    {
        synchronized (this.mCurrentRequests)
        {
            this.mCurrentRequests.add(request);
        }
        request.setRequestQueue(this);
        request.setSequence(getSequenceNumber());
        this.mNetworkQueue.add(request);

        return request;
    }

    private int getSequenceNumber()
    {
        return this.mSequenceGenerator.incrementAndGet();
    }

    public void finish(Request<?> request)
    {
        synchronized (this.mCurrentRequests)
        {
            this.mCurrentRequests.remove(request);
        }
    }

}
