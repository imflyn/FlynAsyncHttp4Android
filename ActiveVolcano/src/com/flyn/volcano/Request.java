package com.flyn.volcano;


public abstract class Request<T>
{
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);
}
