package com.flyn.sample;

import java.io.File;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.flyn.volcano.FileResponseHandler;
import com.flyn.volcano.R;
import com.flyn.volcano.Request.Method;
import com.flyn.volcano.Volcano;

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
                // RequestParams params = new RequestParams();
                // try
                // {
                // params.put("aa", new
                // File(Environment.getExternalStorageDirectory() +
                // File.separator + "yyj" + File.separator + "aa.jpg"));
                // } catch (FileNotFoundException e)
                // {
                // e.printStackTrace();
                // }
                // try
                // {
                // params.put("aa", new
                // File(Environment.getExternalStorageDirectory() +
                // File.separator + "yyj" + File.separator + "ok.mp3"));
                // } catch (FileNotFoundException e)
                // {
                // e.printStackTrace();
                // }
                // Volcano.newNetStack(v.getContext()).makeRequest(Method.POST,
                // null, "http://www.qq.com", null, params, new
                // StringResponseHandler()
                // {
                //
                // @Override
                // protected void onFailure(int statusCode, Map<String, String>
                // headers, byte[] responseBody, Throwable error)
                // {
                // error.printStackTrace();
                // }
                //
                // @Override
                // public void onSuccess(int status, String content)
                // {
                // System.out.println(content);
                // }
                //
                // @Override
                // public void onProgress(int bytesWritten, int bytesTotal, int
                // speed)
                // {
                // System.out.println("bytesWritten:" + bytesWritten);
                // System.out.println("bytesTotal:" + bytesTotal);
                // System.out.println("speed:" + speed);
                // }
                // });
                String url = "http://zhangmenshiting.baidu.com/data2/music/109017153/8930817375600128.mp3?xcode=2a9536886231123c387700702f9919cd17c9e7c86eb6cec7";
                Volcano.newNetStack(Volcano.TYPE_HTTP_CLIENT, v.getContext()).makeRequest(Method.GET, null, url, null, null,
                        new FileResponseHandler(Environment.getExternalStorageDirectory() + "/yyj", "好歌.mp3", true)
                        {

                            @Override
                            public void onSuccess(int statusCode, Map<String, String> headers, File file)
                            {
                                System.out.println(file.getName());
                            }

                            @Override
                            public void onFailure(int statusCode, Map<String, String> headers, Throwable e)
                            {
                                e.printStackTrace();
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
