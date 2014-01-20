package com.flyn.volcano;

import java.util.Timer;
import java.util.TimerTask;

public class SpendTimer
{
    private final static int DEFAULT_TIMER_DURATION=1500;
    private final static int DEFAULT_START_TIMER_DELAY=200;
    private int                bytesTotal    = 0;
    private int                bytesWritten  = 0;

    private Timer               timer;
    private boolean             isScheduleing = true;
    private long                timeStamp     = 0;
    private long                sizeStamp     = 0;
    private int                 currentSpeed  = 0;
    
    private final TimerListener timerListener;
    
    protected SpendTimer(TimerListener timerListener)
    {
        this.timerListener=timerListener;
    }
    
    private void updateProgress(int count)
    {
        this.bytesWritten += count;
    }

    protected void startTimer()
    {
        if (null == this.timer)
            this.timer = new Timer();

        final TimerTask task = new TimerTask()
        {
            private boolean first = true;

            @Override
            public void run()
            {
                if (isScheduleing && !Thread.currentThread().isInterrupted())
                {
                    long nowTime = System.currentTimeMillis();

                    long spendTime = nowTime - timeStamp;
                    timeStamp = nowTime;

                    long getSize = bytesWritten - sizeStamp;
                    sizeStamp = bytesWritten;
                    if (spendTime > 0)
                        currentSpeed = (int) ((getSize / spendTime) / 1.024);

                    if (!first)
                      timerListener.onProgress(bytesWritten, bytesTotal, currentSpeed);
                    else
                        first = false;
                    
                    
                } else
                {
                    stopTimer();
                }
            }
        };
        this.timer.schedule(task, DEFAULT_START_TIMER_DELAY, DEFAULT_TIMER_DURATION);
    }

    protected void stopTimer()
    {
        this.isScheduleing = false;
        if (this.timer != null)
        {
            this.timer.cancel();
            this.timer = null;
        }
    }
    
    
    interface TimerListener
    {
        void onProgress(int bytesWritten, int bytesTotal, int speed);
        
    }
}
