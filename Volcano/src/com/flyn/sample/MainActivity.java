package com.flyn.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.flyn.volcano.R;
import com.flyn.volcano.RequestParams;
import com.flyn.volcano.StringResponseHandler;
import com.flyn.volcano.Volcano;
import com.flyn.volcano.Request.Method;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                RequestParams params = new RequestParams();
                try
                {
                    params.put("aa", new File(Environment.getExternalStorageDirectory() + File.separator + "yyj" + File.separator + "aa.jpg"));
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                try
                {
                    params.put("aa", new File(Environment.getExternalStorageDirectory() + File.separator + "yyj" + File.separator + "ok.mp3"));
                } catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                Volcano.newNetStack(v.getContext()).makeRequest(Method.POST, null, "http://www.qq.com", null, params, new StringResponseHandler()
                {

                    @Override
                    protected void onFailure(int statusCode, Map<String, String> headers, byte[] responseBody, Throwable error)
                    {
                        error.printStackTrace();
                    }

                    @Override
                    public void onSuccess(int status, String content)
                    {
                        System.out.println(content);
                    }

                    @Override
                    public void onProgress(int bytesWritten, int bytesTotal, int speed)
                    {
                        System.out.println("bytesWritten:" + bytesWritten);
                        System.out.println("bytesTotal:" + bytesTotal);
                        System.out.println("speed:" + speed);
                    }
                });
            }
        });
    }

}
