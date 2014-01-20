package com.flyn.volcano;

import java.io.IOException;

public interface Network
{
    NetworkResponse executeRequest(Request<?> request, ResponseDelivery responseDelivery) throws IOException;
}
