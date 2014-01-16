package com.flyn.sample;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
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
                RequestParams params=new RequestParams();
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
                    });
            }
        });
    }

   

}
