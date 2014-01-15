package com.flyn.volcano;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;

public abstract class BitmapResponseHandler extends HttpResponseHandler
{
    private final static String   TAG         = BitmapResponseHandler.class.getName();
    private final int             mMaxWidth;
    private final int             mMaxHeight;
    private final Config          mDecodeConfig;
    private SoftReference<Bitmap> bitmap;

    /**
     * Decoding lock so that we don't decode more than one image at a time (to
     * avoid OOM's)
     */
    private static final Object   sDecodeLock = new Object();

    public BitmapResponseHandler(int maxWidth, int maxHeight, Config mDecodeConfig)
    {
        super();
        this.mMaxHeight = maxWidth;
        this.mMaxWidth = maxHeight;
        this.mDecodeConfig = mDecodeConfig;
    }

    @Override
    protected final void onSuccess(int statusCode, Map<String, String> headers, byte[] responseBody)
    {
        onSuccess(statusCode, headers, this.bitmap.get() != null ? this.bitmap.get() : null);
    }

    @Override
    protected final void onFailure(int statusCode, Map<String, String> headers, byte[] responseBody, Throwable error)
    {
        onFailure(statusCode, headers, error);
    }

    public abstract void onSuccess(int statusCode, Map<String, String> headers, Bitmap bitmap);

    public abstract void onFailure(int statusCode, Map<String, String> headers, Throwable error);

    @Override
    public void sendResponseMessage(HttpResponse response) throws IOException
    {

        HttpEntity entity = response.getEntity();

        int statusCode = response.getStatusLine().getStatusCode();
        Bitmap bitmap = null;
        byte[] responseData = null;
        if (null != entity)
        {
            synchronized (sDecodeLock)
            {
                try
                {
                    responseData = super.entityToData(entity);
                    BitmapFactory.Options mOptions = new BitmapFactory.Options();
                    if (this.mMaxHeight == 0 && this.mMaxWidth == 0)
                    {
                        mOptions.inPreferredConfig = mDecodeConfig;
                        bitmap = BitmapFactory.decodeByteArray(responseData, 0, responseData.length, mOptions);
                    } else
                    {
                        mOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeByteArray(responseData, 0, responseData.length);
                        int actualWidth = mOptions.outWidth;
                        int actualHeight = mOptions.outHeight;

                        int desiredWidth = getResizedDimension(this.mMaxWidth, this.mMaxHeight, actualWidth, actualHeight);
                        int desiredHeight = getResizedDimension(this.mMaxHeight, this.mMaxWidth, actualHeight, actualWidth);

                        mOptions.inJustDecodeBounds = false;
                        mOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);

                        Bitmap tempBitmap = BitmapFactory.decodeByteArray(responseData, 0, responseData.length, mOptions);

                        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth || tempBitmap.getHeight() > desiredHeight))
                        {
                            bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth, desiredHeight, true);
                            tempBitmap.recycle();
                        } else
                        {
                            bitmap = tempBitmap;
                        }

                    }

                } catch (OutOfMemoryError e)
                {
                    Log.e(TAG, "Caught OOM for " + responseData == null ? String.valueOf(0) : responseData.length + " byte image,", e);
                }
            }
        }

        if (bitmap == null)
        {
            sendFailureMessage(statusCode, super.convertHeaders(response.getAllHeaders()), responseData, new IOException("Decode bitmap failure."));
        } else
        {
            this.bitmap = new SoftReference<Bitmap>(bitmap);
            sendSuccessMessage(statusCode, super.convertHeaders(response.getAllHeaders()), responseData);
        }

    }
    /**
     * Get a resized dimension of bitmap
     * @param maxPrimary
     * @param maxSecondary
     * @param actualPrimary
     * @param actualSecondary
     * @return
     */
    private int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary)
    {
        // If no dominant value at all, just return the actual.
        if (maxPrimary == 0 && maxSecondary == 0)
        {
            return actualPrimary;
        }

        // If primary is unspecified, scale primary to match secondary's scaling
        // ratio.
        if (maxPrimary == 0)
        {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0)
        {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary)
        {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    private int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight)
    {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio)
        {
            n *= 2;
        }

        return (int) n;
    }
}
