package com.pvetec.cpucontroller;

import android.util.Log;

/**
 * Created by Administrator on 2018/11/23.
 */

class OccupyThread extends Thread implements Runnable {
    private boolean isClose = false;
    private static final String TAG = "OccupyThread";

    public void setClose() {
        isClose = true;
    }

    @Override
    public void run() {
        while (!isClose){
            Log.d(TAG, "run: "+"It is trying to occupy the CPU isClose:" + isClose);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
