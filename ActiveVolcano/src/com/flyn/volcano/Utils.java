package com.flyn.volcano;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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

    public static String parseContentEnconding(Map<String, String> headers)
    {
        String contentType = headers.get(HTTP.CONTENT_ENCODING);
        if (contentType != null)
            return contentType;

        return "";
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
    public static String getUrlWithParams(String url, RequestParams params)
    {
        url = url.replace(" ", "%20");
        if (null != params)
        {
            for (Entry<String, String> entry : params.getUrlParams().entrySet())
            {
                if (!url.contains("?"))
                {
                    url += "?" + entry.getKey() + "=" + entry.getValue();
                } else
                {
                    url += "&" + entry.getKey() + "=" + entry.getValue();
                }
            }
        }
        return url;
    }

    public static boolean CMMAP_Request(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        String extraInfo = networkInfo.getExtraInfo();
        if (extraInfo != null && extraInfo.toUpperCase(Locale.ENGLISH).contains("CMWAP"))
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

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't
     * have to rely on any large Base64 libraries. Can be overridden if you
     * like!
     * 
     * @param bytes
     *            byte array to be converted
     * @return string containing hex values
     */
    public static String byteArrayToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes)
        {
            int v = element & 0xff;
            if (v < 16)
            {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte arra
     * 
     * @param hexString
     *            string of hex-encoded values
     * @return decoded byte array
     */
    public static byte[] hexStringToByteArray(String hexString)
    {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
