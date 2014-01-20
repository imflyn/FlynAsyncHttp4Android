package com.flyn.volcano;

public class VolcanoError extends Exception
{

    private static final long serialVersionUID = 1L;

    public VolcanoError()
    {
    }

    public VolcanoError(String exceptionMessage)
    {
        super(exceptionMessage);
    }

    public VolcanoError(String exceptionMessage, Throwable reason)
    {
        super(exceptionMessage, reason);
    }

    public VolcanoError(Throwable cause)
    {
        super(cause);
    }
}
