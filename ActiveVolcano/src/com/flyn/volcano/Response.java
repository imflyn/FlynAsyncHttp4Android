package com.flyn.volcano;

public class Response<T>
{

    public final T result;

    public boolean intermediate = false;

    public static <T> Response<T> build(T result)
    {
        return new Response<T>(result);
    }

    private Response(T result)
    {
        this.result = result;
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public <T> T getResult()
    {
        return (T) result;
    }

}
