package com.flyn.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flyn.newvolcano.R;
import com.flyn.volcano.FileRequest;
import com.flyn.volcano.Listener;
import com.flyn.volcano.RequestQueue;
import com.flyn.volcano.Response;
import com.flyn.volcano.Volcano;

public class MainActivity extends Activity
{
    RequestQueue queue;

    ProgressBar  bar;

    TextView     text;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bar = (ProgressBar) findViewById(R.id.progressBar1);
        text = (TextView) findViewById(R.id.textView1);
        findViewById(R.id.button1).setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (null == queue)
                {
                    queue = Volcano.newRequestQueue(getApplicationContext());
                    queue.add(new FileRequest("http://zhangmenshiting.baidu.com/data2/music/110015948/1097533661390222861128.mp3?xcode=19a7f4673af5eeb192391a7959859d7fd12cca986ddbfef2", Environment
                            .getExternalStorageDirectory() + "/yyj", "好个.mp3", true, new Listener()
                    {

                        @Override
                        public void onSuccess(Response<?> response)
                        {
                            System.out.println("ok");
                            // ((ImageView)
                            // findViewById(R.id.imageView1)).setImageBitmap((Bitmap)response.result);
                        }

                        @Override
                        public void onFailure(Throwable error)
                        {
                            error.printStackTrace();
                        }

                        public void onProgress(int bytesWritten, int bytesTotal, int currentSpeed)
                        {
                            bar.setMax(bytesTotal);
                            bar.setProgress(bytesWritten);

                            text.setText(String.valueOf(currentSpeed));

                        };

                    }));

                } else
                {
                    queue.stop();
                    queue = null;
                }
            }
        });

    }
}
