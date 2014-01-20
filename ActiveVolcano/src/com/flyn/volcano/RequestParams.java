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

    public void put(String key, InputStream value)
    {
        put(key, value, null);
    }

    public void put(String key, InputStream value, String contentType)
    {
        this.mStreamParams.put(key, new StreamWrapper(value, contentType));
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

    static class FileWrapper
    {
        private File   File;
        private String contentType;

        public FileWrapper(File file, String contentType)
        {
            this.File = file;
            this.contentType = contentType;
        }

        protected final File getFile()
        {
            return File;
        }

        protected final void setFile(File file)
        {
            File = file;
        }

        protected final String getContentType()
        {
            return contentType;
        }

        protected final void setContentType(String contentType)
        {
            this.contentType = contentType;
        }

    }

    static class StreamWrapper
    {
        private InputStream inStream;
        private String      contentType;

        public StreamWrapper(InputStream inStream, String contentType)
        {
            this.inStream = inStream;
            this.contentType = contentType;
        }

        protected final InputStream getInStream()
        {
            return inStream;
        }

        protected final void setInStream(InputStream inStream)
        {
            this.inStream = inStream;
        }

        protected final String getContentType()
        {
            return contentType;
        }

        protected final void setContentType(String contentType)
        {
            this.contentType = contentType;
        }

    }
}
