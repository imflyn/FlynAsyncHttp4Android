package com.flyn.volcano;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.text.TextUtils;

public class RequestParams
{

    private ConcurrentHashMap<String, String>        mUrlParams;
    private ConcurrentHashMap<String, FileWrapper>   mFileParams;
    private ConcurrentHashMap<String, StreamWrapper> mStreamParams;

    public RequestParams()
    {
        this(null);
    }

    public RequestParams(final String key, final String value)
    {
        this(new HashMap<String, String>()
        {
            private static final long serialVersionUID = 1L;
            {
                put(key, value);
            }
        });
    }

    public RequestParams(Map<String, String> urlParams)
    {
        init();
        if (null != urlParams)
            this.mUrlParams.putAll(urlParams);
    }

    private void init()
    {
        this.mUrlParams = new ConcurrentHashMap<String, String>();
        this.mStreamParams = new ConcurrentHashMap<String, RequestParams.StreamWrapper>();
        this.mFileParams = new ConcurrentHashMap<String, RequestParams.FileWrapper>();
    }

    public void put(String key, String value)
    {
        if (!TextUtils.isEmpty(key) && null != value)
            this.mUrlParams.put(key, value);
    }

    public void put(String key, File value)
    {
        put(key, value, null);
    }

    public void put(String key, File value, String contentType)
    {
        this.mFileParams.put(key, new FileWrapper(value, contentType));
    }

    public void put(String key, String name, InputStream inputStream)
    {
        put(key, name, inputStream, null);
    }

    public void put(String key, String name, InputStream inputStream, String contentType)
    {
        this.mStreamParams.put(key, new StreamWrapper(inputStream, name, contentType));
    }

    public void remove(String key)
    {
        this.mUrlParams.remove(key);
        this.mFileParams.remove(key);
        this.mStreamParams.remove(key);
    }

    protected final Map<String, String> getUrlParams()
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(this.mUrlParams);
        return map;
    }

    protected final Map<String, FileWrapper> getFileParams()
    {
        HashMap<String, FileWrapper> map = new HashMap<String, FileWrapper>();
        map.putAll(this.mFileParams);
        return map;
    }

    protected final Map<String, StreamWrapper> getStreamParams()
    {
        HashMap<String, StreamWrapper> map = new HashMap<String, StreamWrapper>();
        map.putAll(this.mStreamParams);
        return map;
    }

    static class FileWrapper
    {
        protected File   file;
        protected String contentType;

        protected FileWrapper(File file, String contentType)
        {
            this.file = file;
            this.contentType = contentType;
        }

    }

    static class StreamWrapper
    {
        protected String      name;
        protected InputStream inputStream;
        protected String      contentType;

        protected StreamWrapper(InputStream inputStream, String name, String contentType)
        {
            this.inputStream = inputStream;
            this.name = name;
            this.contentType = contentType;
        }

    }
}
