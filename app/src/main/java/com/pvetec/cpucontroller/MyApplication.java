package com.pvetec.cpucontroller;

import com.pvetec.systemsdk.base.BaseApplication;
import com.pvetec.systemsdk.crash.CrashSystem;

/**
 * Created by Administrator on 2018/11/26.
 */

public class MyApplication extends BaseApplication {

    public void onCreate() {
        super.onCreate();
        CrashSystem.getInstance(this).init();
    }
}
