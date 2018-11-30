package com.pvetec.cpucontroller;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

public class MyService extends Service {

    private CpuRateBinder cpuRateBinder = new CpuRateBinder();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return cpuRateBinder;
    }

    class CpuRateBinder extends Binder {
        private static final String TAG = "MyService";
        private static final int GET_CPU_RATE = 1;
        private ArrayList<OccupyThread> mThreads;
        private CpuRateGetListener mListener;
        private Runnable runnable;
        private String targetRate;
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GET_CPU_RATE:
                        mListener.onRateGet((Float) msg.obj);
                        handleCPURate((Float) msg.obj);
                }
            }
        };

        public void setOnGetListener(CpuRateGetListener listener) {
            mListener = listener;
        }

        public void startTask(String tagRate) {
            mThreads = new ArrayList<>();
            targetRate = tagRate;
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
                    //3s后执行this，即runable
                    mHandler.postDelayed(this, 3000);
                }
            };
            mHandler.post(runnable);// 打开定时器
        }

        public void stopTask() {
            mHandler.removeCallbacksAndMessages(null);
            for (OccupyThread occupyThread : mThreads) {
                occupyThread.setClose();
            }
            mThreads.clear();
        }

        private void handleCPURate(Float cpuRateNow) {
            if (targetRate == null || targetRate.equals("")) {
                return;
            }
            if (cpuRateNow == 0) {
                return;
            }
            //判断输入值是否合理
            if (Float.parseFloat(targetRate) > 0 && Float.parseFloat(targetRate) <= 100) {
                //如果期望值等于实际值，直接返回
                if (cpuRateNow == Float.parseFloat(targetRate)) {
                    return;
                }
                //判断目标CPU占用率是否大于实际占用率。大于，增加线程；小于，移除线程。
                if (Float.parseFloat(targetRate) > cpuRateNow) {
                    //如果目标占用率大于实际值10以上，则一次性增加10个线程
                    if (Float.parseFloat(targetRate) - cpuRateNow >= 10) {
                        Log.d(TAG, "handleCPURate: " + "add 10-threads");
                        for (int i = 0; i < 10; i++) {
                            addThread();
                        }
                    } else {
                        Log.d(TAG, "addThread: " + "add 1-thread");
                        addThread();
                    }
                    //如果目标占用率小于实际值10以下，则一次性移除10个线程
                } else if (cpuRateNow - Float.parseFloat(targetRate) >= 10) {
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
            mListener.onThreadUpdate(mThreads.size());
        }

        private void removeThread() {
            if (mThreads.size() > 0) {
                mThreads.get(0).setClose();
                mThreads.remove(0);
            }
        }

        private void addThread() {
            OccupyThread mThread = new OccupyThread();
            mThread.start();
            mThreads.add(mThread);
        }
    }
}
