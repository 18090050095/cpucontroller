package com.pvetec.cpucontroller;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class MyService extends Service {

    private static final String TAG = "MyService";
    private String tagetRate;
    public static final int GET_CPU_RATE = 1;
    private ArrayList<OccupyThread> mThreads;
    private boolean isContinue = true;
    private Runnable runnable;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_CPU_RATE:
                    handleCPURate((Float) msg.obj);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mThreads = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        tagetRate = intent.getStringExtra("tagRate");
        initTask();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeThreads();
        isContinue = false;
        mHandler.removeCallbacks(runnable);
        Log.d(TAG, "onDestroy: "+ mThreads.size());
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initTask() {
        runnable = new Runnable() {
            @Override
            public void run() {
                // 获取CPU使用率
                Float cpuRate = CPURateUtil.getRate();
                //取出占用率用于计算
                Message msg = Message.obtain();
                msg.obj = cpuRate;
                msg.what = GET_CPU_RATE;
                mHandler.sendMessage(msg);
                //发送广播UI显示占用率
                Intent mIntent = new Intent("UPDATE_NOWRATE");
                mIntent.putExtra("nowRate",cpuRate);
                sendBroadcast(mIntent);
                Log.d(TAG, "sendBroadcast: "+cpuRate);
                //3s后执行this，即runable
                if (isContinue) {
                    mHandler.postDelayed(this, 3000);
                }
            }
        };
        mHandler.post(runnable);// 打开定时器
    }

    public void handleCPURate(Float cpuRateNow) {
        if (tagetRate == null || tagetRate.equals("")) {
            return;
        }
        //判断输入值是否合理
        if (Float.parseFloat(tagetRate) > 0 && Float.parseFloat(tagetRate) <= 100) {
            //判断目标CPU占用率是否大于实际占用率。大于，增加线程；小于，移除线程。
            if (Float.parseFloat(tagetRate) > cpuRateNow) {
                //如果目标占用率大于实际值10以上，则一次性增加10个线程
                if (Float.parseFloat(tagetRate) - cpuRateNow > 10) {
                    Log.d(TAG, "handleCPURate: " + "add 10-threads");
                    for (int i = 0; i < 10; i++) {
                        addThread();
                    }
                } else {
                    Log.d(TAG, "addThread: " + "add 1-thread");
                    addThread();
                }
                //如果目标占用率小于实际值10以下，则一次性移除10个线程
            } else if (cpuRateNow - Float.parseFloat(tagetRate) > 10) {
                Log.d(TAG, "handleCPURate: " + "remove 10-threads");
                for (int i = 0; i < 10; i++) {
                    removeThread();
                }
            } else {
                Log.d(TAG, "removeThread: " + "remove 1-thread");
                removeThread();
            }
        }
        Log.d(TAG, "ThreadCount: " + mThreads.size());
        Intent threadsIntent = new Intent();
        threadsIntent.setAction("THREAD_COUNT");
        threadsIntent.putExtra("threadCount",mThreads.size());
        sendBroadcast(threadsIntent);
    }

    private void removeThread() {
        if (mThreads.size() > 0) {
            mThreads.get(0).setClose(true);
            mThreads.remove(0);
        }
    }

    private void addThread() {
        OccupyThread mThread = new OccupyThread();
        mThread.start();
        mThreads.add(mThread);
    }

    private void closeThreads() {
        for (OccupyThread occupyThread : mThreads) {
            occupyThread.setClose(true);
        }
        mThreads.clear();
    }
}
