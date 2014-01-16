package com.flyn.volcano;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

/**
 * Useing SharedPreferences to save Cookies
 * 
 * @author V
 * 
 */
public class PersistentCookieStore implements CookieStore
{
    private static final String                     TAG                = PersistentCookieStore.class.getName();
    private static final String                     COOKIE_PREFS       = "CookiePrefsFile";
    private static final String                     COOKIE_NAME_STORE  = "names";
    private static final String                     COOKIE_NAME_PREFIX = "cookie_";

    private final ConcurrentHashMap<String, Cookie> cookies;
    private final SharedPreferences                 cookiePrefs;

    public PersistentCookieStore(Context context)
    {
        this.cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        this.cookies = new ConcurrentHashMap<String, Cookie>();

        // Load any previously stored cookies into the store
        String storedCookieNames = this.cookiePrefs.getString(COOKIE_NAME_STORE, null);
        if (storedCookieNames != null)
        {
            String[] cookieNames = TextUtils.split(storedCookieNames, ",");
            for (String name : cookieNames)
            {
                String encodedCookie = this.cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                if (encodedCookie != null)
                {
                    Cookie decodedCookie = decodeCookie(encodedCookie);
                    if (decodedCookie != null)
                    {
                        this.cookies.put(name, decodedCookie);
                    }
                }
            }

            // Clear out expired cookies
            clearExpired(new Date());
        }
    }

    @Override
    public void addCookie(Cookie cookie)
    {
        String name = cookie.getName() + cookie.getDomain();

        // Save cookie into local store, or remove if expired
        if (!cookie.isExpired(new Date()))
        {
            this.cookies.put(name, cookie);
        } else
        {
            this.cookies.remove(name);
        }
        // Save cookie into persistent store
        SharedPreferences.Editor prefsWriter = this.cookiePrefs.edit();
        prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", this.cookies.keySet()));
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableCookie(cookie)));
        prefsWriter.commit();
    }

    @Override
    public void clear()
    {
        // Clear cookies from persistent store
        SharedPreferences.Editor prefsWriter = this.cookiePrefs.edit();
        for (String name : this.cookies.keySet())
        {
            prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        }
        prefsWriter.remove(COOKIE_NAME_STORE);
        prefsWriter.commit();

        // Clear cookies from local store
        this.cookies.clear();
    }

    @Override
    public boolean clearExpired(Date date)
    {
        boolean clearedAny = false;
        SharedPreferences.Editor prefsWriter = this.cookiePrefs.edit();

        for (ConcurrentHashMap.Entry<String, Cookie> entry : this.cookies.entrySet())
        {
            String name = entry.getKey();
            Cookie cookie = entry.getValue();
            if (cookie.isExpired(date))
            {
                // Clear cookies from local store
                this.cookies.remove(name);

                // Clear cookies from persistent store
                prefsWriter.remove(COOKIE_NAME_PREFIX + name);

                // We've cleared at least one
                clearedAny = true;
            }
        }

        // Update names in persistent store
        if (clearedAny)
        {
            prefsWriter.putString(COOKIE_NAME_STORE, TextUtils.join(",", this.cookies.keySet()));
        }
        prefsWriter.commit();

        return clearedAny;
    }

    @Override
    public List<Cookie> getCookies()
    {
        return new ArrayList<Cookie>(this.cookies.values());
    }

    /**
     * Serializes Cookie object into String
     * 
     * @param cookie
     *            cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    private String encodeCookie(SerializableCookie cookie)
    {
        if (cookie == null)
            return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try
        {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
            os.flush();
        } catch (Exception e)
        {
            return null;
        }

        return Utils.byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns cookie decoded from cookie string
     * 
     * @param cookieString
     *            string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    private Cookie decodeCookie(String cookieString)
    {
        byte[] bytes = Utils.hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableCookie) objectInputStream.readObject()).getCookie();
        } catch (Exception exception)
        {
            Log.d(TAG, "decodeCookie failed", exception);
        }

        return cookie;
    }

}