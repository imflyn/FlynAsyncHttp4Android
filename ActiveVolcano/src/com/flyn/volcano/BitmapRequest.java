package com.flyn.volcano;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapRequest extends Request<Bitmap>
{
    private final static String TAG         = BitmapRequest.class.getName();

    private final Config        mDecodeConfig;
    private final int           mMaxWidth;
    private final int           mMaxHeight;

    private static final Object sDecodeLock = new Object();

    public BitmapRequest(int method, String url, RequestParams requestPramas, int retryCount, int mMaxWidth, int mMaxHeight, Config mDecodeConfig, Listener mListener)
    {
        super(method, url, requestPramas, retryCount, mListener);
        this.mMaxHeight = mMaxHeight;
        this.mMaxWidth = mMaxWidth;
        this.mDecodeConfig = mDecodeConfig;
    }

    public BitmapRequest(int method, String url, RequestParams requestPramas, int mMaxWidth, int mMaxHeight, Config mDecodeConfig, Listener mListener)
    {
        super(method, url, requestPramas, mListener);
        this.mMaxHeight = mMaxHeight;
        this.mMaxWidth = mMaxWidth;
        this.mDecodeConfig = mDecodeConfig;
    }

    public BitmapRequest(String url, int mMaxWidth, int mMaxHeight, Config mDecodeConfig, Listener mListener)
    {
        super(Method.GET, url, null, mListener);
        this.mMaxHeight = mMaxHeight;
        this.mMaxWidth = mMaxWidth;
        this.mDecodeConfig = mDecodeConfig;
    }

    @Override
    protected Response<?> parseNetworkResponse(NetworkResponse response, ResponseDelivery responseDelivery) throws IOException
    {
        byte[] date = getData(response, responseDelivery);
        return doParse(date);
    }

    private Response<Bitmap> doParse(byte[] data) throws IOException
    {
        Bitmap bitmap = null;
        synchronized (sDecodeLock)
        {
            try
            {
                BitmapFactory.Options decodeOptions = new BitmapFactory.Options();

                if (mMaxWidth == 0 && mMaxHeight == 0)
                {
                    decodeOptions.inPreferredConfig = mDecodeConfig;
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
                } else
                {
                    decodeOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);
                    int actualWidth = decodeOptions.outWidth;
                    int actualHeight = decodeOptions.outHeight;

                    int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight, actualWidth, actualHeight);
                    int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth, actualHeight, actualWidth);

                    decodeOptions.inJustDecodeBounds = false;
                    decodeOptions.inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
                    Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOptions);

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
                Log.e(TAG, "Caught OOM for " + data == null ? String.valueOf(0) : data.length + " byte image,", e);
                throw new IOException("OutOfMemoryError:" + e.getMessage());
            }
        }

        return Response.build(bitmap);
    }

    private int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary)
    {
        if (maxPrimary == 0 && maxSecondary == 0)
        {
            return actualPrimary;
        }

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
