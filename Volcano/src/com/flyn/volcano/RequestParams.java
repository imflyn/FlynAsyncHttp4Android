package com.flyn.volcano;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/**
 * The container for request parameters
 * 
 * @author V
 * 
 */
public class RequestParams
{
    protected ConcurrentHashMap<String, String>        urlParams;
    protected ConcurrentHashMap<String, FileWrapper>   fileParams;
    protected ConcurrentHashMap<String, StreamWrapper> streamParams;
    protected boolean                                  isRepeatable;
    protected String                                   contentEncoding      = HTTP.UTF_8;
    private static final String                        PARAMETER_SEPARATOR  = "&";
    private static final String                        NAME_VALUE_SEPARATOR = "=";

    public void setContentEncoding(final String encoding)
    {
        if (encoding != null)
            this.contentEncoding = encoding;
        else
            new NullPointerException("encoding is null.");
    }

    public RequestParams()
    {
        this((Map<String, String>) null);
    }

    public RequestParams(Map<String, String> source)
    {
        init();
        if (source != null)
        {
            for (Map.Entry<String, String> entry : source.entrySet())
            {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("serial")
    public RequestParams(final String key, final String value)
    {
        this(new HashMap<String, String>()
        {
            {
                put(key, value);
            }
        });
    }

    public void setHttpEntityIsRepeatable(boolean isRepeatable)
    {
        this.isRepeatable = isRepeatable;
    }

    public void init()
    {
        this.urlParams = new ConcurrentHashMap<String, String>();
        this.fileParams = new ConcurrentHashMap<String, FileWrapper>();
        this.streamParams = new ConcurrentHashMap<String, StreamWrapper>();
    }

    public void put(String key, String value)
    {
        if (key != null && value != null)
        {
            this.urlParams.put(key, value);
        }
    }

    public void put(String key, File file) throws FileNotFoundException
    {
        put(key, file, null);
    }

    public void put(String key, File file, String contentType) throws FileNotFoundException
    {
        if (file == null || !file.exists())
            throw new FileNotFoundException("wrong file argument was passed");
        if (key != null)
        {
            this.fileParams.put(key, new FileWrapper(file, contentType));
        }
    }

    public void put(String key, InputStream stream, String name, String contentType)
    {
        if (key != null && stream != null)
        {
            this.streamParams.put(key, new StreamWrapper(stream, name, contentType));
        }
    }

    /**
     * 
     * @param startPos
     *            if don't want this set this <0
     * @param endPos
     *            if don't want this set this <0
     */
    public void putByteRange(int startPos, int endPos)
    {
        if (startPos < 0 && endPos < 0)
            return;
        if (startPos < 0 && endPos > 0)
            this.urlParams.put("Range", "bytes=" + "-" + endPos);// 最后endPos个字节
        else if (startPos >= 0 && endPos < 0)
            this.urlParams.put("Range", "bytes=" + startPos + "-");// startPos字节以后的范围
        else if (startPos >= 0 && endPos > 0)
            this.urlParams.put("Range", "bytes=" + startPos + "-" + endPos);
    }

    public void remove(String key)
    {
        this.urlParams.remove(key);
        this.fileParams.remove(key);
        this.streamParams.remove(key);
    }

    protected String getParamString()
    {
        return format(this.urlParams, this.contentEncoding);
    }

    protected HttpEntity getEntity(IResponseHandler progressHandler) throws IOException
    {
        if (this.streamParams.isEmpty() && this.fileParams.isEmpty())
        {
            return createNormalEitity();
        } else
        {
            return createMultipartEntity(progressHandler);
        }
    }

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

    /**
     * @param parameters
     * @param encoding
     * @return
     */
    private String format(Map<String, String> parameters, final String encoding)
    {
        StringBuilder result = new StringBuilder();
        for (Entry<String, String> parameter : parameters.entrySet())
        {
            final String encodedName = encode(parameter.getKey(), encoding);
            final String value = parameter.getValue();
            final String encodedValue = value != null ? encode(value, encoding) : "";
            if (result.length() > 0)
                result.append(PARAMETER_SEPARATOR);
            result.append(encodedName);

            result.append(NAME_VALUE_SEPARATOR);
            result.append(encodedValue);
        }
        return result.toString();

    }

    private String encode(final String content, final String encoding)
    {
        try
        {
            return URLEncoder.encode(content, encoding != null ? encoding : this.contentEncoding);
        } catch (UnsupportedEncodingException problem)
        {
            throw new IllegalArgumentException(problem);
        }
    }

    protected final ConcurrentHashMap<String, String> getUrlParams()
    {
        return this.urlParams;
    }

    protected final ConcurrentHashMap<String, FileWrapper> getFileParams()
    {
        return this.fileParams;
    }

    protected final ConcurrentHashMap<String, StreamWrapper> getStreamParams()
    {
        return this.streamParams;
    }

    public static class FileWrapper
    {
        public File   file;
        public String contentType;

        public FileWrapper(File file, String contentType)
        {
            this.file = file;
            this.contentType = contentType;
        }
    }

    public static class StreamWrapper
    {
        public InputStream inputStream;
        public String      name;
        public String      contentType;

        public StreamWrapper(InputStream inputStream, String name, String contentType)
        {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
        }
    }

}
