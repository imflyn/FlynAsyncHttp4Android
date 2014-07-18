package com.flyn.volcano;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.text.TextUtils;

public class RequestParams
{

    private ConcurrentHashMap<String, String>        mUrlParams;
    private ConcurrentHashMap<String, FileWrapper>   mFileParams;
    private ConcurrentHashMap<String, StreamWrapper> mStreamParams;
    private CopyOnWriteArrayList<Header>             mHeaderParams;

    public RequestParams()
    {
        this(null);
    }

    public RequestParams(final String key, final String value)
    {
        this(new HashMap<String, String>()
        {
            private static final long serialVersionUID = 1333044953920909830L;

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
        this.mHeaderParams = new CopyOnWriteArrayList<RequestParams.Header>();
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

    public void putHeader(String key, String value)
    {
        if (!TextUtils.isEmpty(key) && null != value)
            this.mHeaderParams.add(new Header(key, value));
    }

    public void put(Header header)
    {
        if (null == header)
            throw new IllegalArgumentException("Header can not be null.");

        this.mHeaderParams.add(header);
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
        return Collections.unmodifiableMap(map);
    }

    protected final Map<String, FileWrapper> getFileParams()
    {
        HashMap<String, FileWrapper> map = new HashMap<String, FileWrapper>();
        map.putAll(this.mFileParams);
        return Collections.unmodifiableMap(map);
    }

    protected final Map<String, StreamWrapper> getStreamParams()
    {
        HashMap<String, StreamWrapper> map = new HashMap<String, StreamWrapper>();
        map.putAll(this.mStreamParams);
        return Collections.unmodifiableMap(map);
    }

    protected final List<Header> getHeaders()
    {
        ArrayList<Header> headers = new ArrayList<RequestParams.Header>();
        headers.addAll(this.mHeaderParams);
        return Collections.unmodifiableList(headers);
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

    static class Header
    {
        protected String name;
        protected String value;

        protected Header(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
    }
}
