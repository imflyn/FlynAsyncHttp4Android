package com.flyn.sample;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.flyn.volcano.R;

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
                URLConnection conn=null;
                try
                {
                    URL url=new URL("http://www.baidu.com");
                     conn= url.openConnection();
                     
                } catch (MalformedURLException e)
                {
                    e.printStackTrace();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                
               
            }
        });
    }

   

}
