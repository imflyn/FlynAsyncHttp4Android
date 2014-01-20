package com.flyn.volcano;

public interface NetWork
{
    void executeRequest(Request<?> request) throws VolcanoError;

}
