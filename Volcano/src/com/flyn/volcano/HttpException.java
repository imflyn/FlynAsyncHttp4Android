package com.flyn.volcano;

public class HttpException extends Exception
{
    private static final long serialVersionUID = 1L;

    public HttpException()
    {
        super();
    }

    public HttpException(Throwable cause)
    {
        super(cause);
    }

    public HttpException(String exceptionMessage)
    {
        super(exceptionMessage);
    }

    public HttpException(String exceptionMessage, Throwable reason)
    {
        super(exceptionMessage, reason);
    }

}
