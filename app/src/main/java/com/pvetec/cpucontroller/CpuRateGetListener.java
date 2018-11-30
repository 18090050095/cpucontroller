package com.pvetec.cpucontroller;

/**
 * Created by Administrator on 2018/11/30.
 */

public interface CpuRateGetListener {

    void onRateGet(float rate);

    void onThreadUpdate(int count);
}
