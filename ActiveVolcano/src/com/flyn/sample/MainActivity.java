package com.flyn.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.flyn.volcano.Listener;
import com.flyn.volcano.R;
import com.flyn.volcano.RequestQueue;
import com.flyn.volcano.Response;
import com.flyn.volcano.StringRequest;
import com.flyn.volcano.Volcano;

public class MainActivity extends Activity
{
    RequestQueue queue;

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
                if (null == queue)
                {
                    queue = Volcano.newRequestQueue(getApplicationContext());
                    queue.add(new StringRequest("http://www.baidu.com", null, new Listener()
                    {

                        @Override
                        public void onSuccess(Response<?> result)
                        {
                            System.out.println(result.result);
                        }

                        @Override
                        public void onFailure(Throwable error)
                        {
                            error.printStackTrace();
                        }
                    
                    }));
                } else
                {
                    queue.stop();
                }
            }
        });

    }
}
