package com.flyn.volcano;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

/**
 * The container for HttpClient request parameters
 * 
 * @author V
 * 
 */
public class HttpClientRequestParams extends RequestParams
{

    @Override
    protected HttpEntity createNormalEitity()
    {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

        for (ConcurrentHashMap.Entry<String, String> entry : this.urlParams.entrySet())
        {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        try
        {
            return new UrlEncodedFormEntity(lparams, this.contentEncoding);
        } catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

    @Override
    protected HttpEntity createMultipartEntity(IResponseHandler progressHandler) throws IOException
    {
        MultipartEntity entity = new MultipartEntity(progressHandler);
        entity.setIsRepeatable(this.isRepeatable);

        for (ConcurrentHashMap.Entry<String, String> entry : this.urlParams.entrySet())
        {
            entity.addPart(entry.getKey(), entry.getValue());
        }

        for (ConcurrentHashMap.Entry<String, StreamWrapper> entry : this.streamParams.entrySet())
        {
            StreamWrapper stream = entry.getValue();
            if (stream.inputStream != null)
            {
                entity.addPart(entry.getKey(), stream.name, stream.inputStream, stream.contentType);
            }
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : this.fileParams.entrySet())
        {
            FileWrapper fileWrapper = entry.getValue();
            entity.addPart(entry.getKey(), fileWrapper.file, fileWrapper.contentType);
        }
        return entity;
    }

}
