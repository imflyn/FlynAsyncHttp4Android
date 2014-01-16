package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Map;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils
{
    /**
     * Returns the charset specified in the Content-Type of this header, or the
     * HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Map<String, String> headers)
    {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null)
        {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++)
            {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2)
                {
                    if (pair[0].equals("charset"))
                    {
                        return pair[1];
                    }
                }
            }
        }

        return HTTP.DEFAULT_CONTENT_CHARSET;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr)
    {
        try
        {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (DateParseException e)
        {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    /**
     * close streams
     * 
     * @param stream
     */
    public static void quickClose(Closeable stream)
    {
        if (null != stream)
            try
            {
                stream.close();
            } catch (IOException e)
            {
            } finally
            {
                stream = null;
            }

    }

    /**
     * get complete url
     * 
     * @param shouldEncodeUrl
     * @param url
     * @param params
     * @return
     */
    public static String getUrlWithParams(boolean shouldEncodeUrl, String url, RequestParams params)
    {
        if (shouldEncodeUrl)
        {
            url = url.replace(" ", "%20");
        }
        if (null != params)
        {
            String paramString = params.getParamString();
            if (!url.contains("?"))
            {
                url += "?" + paramString;
            } else
            {
                url += "&" + paramString;
            }
        }
        return url;
    }
    
    
    public static boolean CMMAP_Request(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        String extraInfo = networkInfo.getExtraInfo();
        if (extraInfo.contains("CMWAP"))
            return true;
        return false;
    }
    
    
    public static KeyStore getKeystoreOfCA(InputStream cert)
    {

        // Load CAs from an InputStream
        InputStream caInput = null;
        Certificate ca = null;
        try
        {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            caInput = new BufferedInputStream(cert);
            ca = cf.generateCertificate(caInput);
        } catch (CertificateException e1)
        {
            e1.printStackTrace();
        } finally
        {
            Utils.quickClose(caInput);
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = null;
        try
        {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return keyStore;
    }

    public static KeyStore getKeystore()
    {
        KeyStore trustStore = null;
        try
        {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
        } catch (Throwable t)
        {
            t.printStackTrace();
        }
        return trustStore;
    }
}
