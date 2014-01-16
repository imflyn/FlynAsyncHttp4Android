package com.flyn.sample;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.flyn.volcano.FileResponseHandler;
import com.flyn.volcano.R;
import com.flyn.volcano.Request.Method;
import com.flyn.volcano.RequestFuture;
import com.flyn.volcano.Volcano;

public class MainActivity extends Activity
{
    RequestFuture stack;

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
//                RequestParams params = new RequestParams();
//                params.putByteRange((int) new File(Environment.getExternalStorageDirectory() + "/yyj", "好歌.tmp").length(), 4427185);
                    
                Map<String,String> map=new HashMap<String, String>();
                map.put("Range", "bytes="+(int) new File(Environment.getExternalStorageDirectory() + "/yyj", "好歌.tmp").length()+"-");
                 
                if (null == stack)
                {
                    String url = "http://zhangmenshiting.baidu.com/data2/music/109017153/8930817375600128.mp3?xcode=2a9536886231123c387700702f9919cd17c9e7c86eb6cec7";
                    stack = Volcano.newNetStack(Volcano.TYPE_HTTP_URLCONNECT, v.getContext()).makeRequest(Method.GET, null, url, map, null,
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

                    // final long time = System.currentTimeMillis();
                    // System.out.println("开始上传");
                    // String ip = "192.168.0.100";
                    // String url = "http://" + ip +
                    // ":8080/upload/upload/execute.do";
                    // RequestParams params1 = new RequestParams();
                    // params1.put("username", "张三");
                    // params1.put("pwd", "zhangsan");
                    // params1.put("age", "21");
                    // File file = new
                    // File(Environment.getExternalStorageDirectory() + "/yyj",
                    // "1.mp3");
                    // params1.put("fileName", file.getName());
                    // try
                    // {
                    // params1.put("image", file);
                    // } catch (FileNotFoundException e1)
                    // {
                    // e1.printStackTrace();
                    // }
                    // Volcano.newNetStack(Volcano.TYPE_HTTP_CLIENT,
                    // v.getContext(), false).makeRequest(Method.POST,
                    // "application/octet-stream", url, null, params1, new
                    // HttpResponseHandler()
                    // {
                    //
                    // @Override
                    // public void onSuccess(int statusCode, Map<String, String>
                    // headers, byte[] responseBody)
                    // {
                    // System.out.println("共耗时:" + (System.currentTimeMillis() -
                    // time));
                    // try
                    // {
                    // System.out.println(new String(responseBody, "utf-8"));
                    // } catch (UnsupportedEncodingException e)
                    // {
                    // e.printStackTrace();
                    // }
                    // }
                    //
                    // @Override
                    // public void onFailure(int statusCode, Map<String, String>
                     // headers, byte[] responseBody, Throwable error)
                    // {
                    // System.out.println(error.toString());
                    // }
                    //
                    // @Override
                    // public void onProgress(int bytesWritten, int bvtesTotal,
                    // int speed)
                    // {
                    // System.out.println("速度1:" + speed);
                    // }
                    // });
                } else
                {
                    stack.cancel(true);
                    stack = null;
                }
            }
        });
    }

}
