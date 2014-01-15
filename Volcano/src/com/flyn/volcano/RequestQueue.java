package com.flyn.volcano;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import android.content.Context;

public class RequestQueue
{
    private WeakHashMap<Object, RequestHandler>      mRequestHandlers;
    protected final Map<Context, List<RequestQueue>> mRequestMap;

    private Request[]                                mRequest;

    protected RequestQueue()
    {
        this.requestMap = new WeakHashMap<Context, List<RequestQueue>>();
    }
    
    public void start()
    {
        
    }
    
    public void stop()
    {
        
    }

    public void add(Context context, RequestHandler request)
    {
        if (null != context)
        {
            List<RequestQueue> list = this.requestMap.get(context);
            if (null == list)
            {
                list = new LinkedList<RequestQueue>();
                this.requestMap.put(context, list);
            }
            list.add(this);

            Iterator<RequestQueue> iterator = list.iterator();
            while (iterator.hasNext())
            {

                if (iterator.next().shouldBeGarbageCollected())
                    iterator.remove();
            }
        }
    }
    
    

    public boolean cancel(Object tag, boolean mayInterruptIfRunning)
    {
        RequestHandler _request = this.requestHandlers.get(tag);
        return _request == null || _request.cancel(mayInterruptIfRunning);

    }

    public void cancelRequests(Context context, boolean mayInterruptIfRunning)
    {
        List<RequestQueue> requestList = this.requestMap.get(context);
        if (requestList != null)
        {
            for (RequestHandler requestHandle : requestHandlers.get(key))
            {
                requestHandle.cancel(mayInterruptIfRunning);
            }
            this.requestMap.remove(context);
        }
    }

    public boolean isFinished(Object tag)
    {

        RequestHandler _request = this.requestHandlers.get(tag);
        return _request == null || _request.isFinished();

    }

    public boolean isCanceled(Object tag)
    {
        RequestHandler _request = this.requestHandlers.get(tag);
        return _request == null || _request.isCancelled();
    }

    public boolean shouldBeGarbageCollected(Object tag)
    {
        boolean should = isCanceled(tag) || isFinished(tag);

        if (should)
            this.requestHandlers.remove(tag);
        return should;

    }

}
