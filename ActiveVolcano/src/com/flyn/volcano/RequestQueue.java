package com.flyn.volcano;

public class RequestQueue
{
    private NetworkDispatcher[] mDispatchers;

    protected RequestQueue(Network netWork)
    {

    }

    public void start()
    {
        for (int i = 0; i < mDispatchers.length; i++)
        {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher();
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    public void stop()
    {

    }

    public void cancelAll()
    {

    }

    public Request<?> add(Request<?> request)
    {
        return null;
    }

    public void finish()
    {

    }

}
