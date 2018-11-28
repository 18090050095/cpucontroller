package com.pvetec.cpucontroller;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/11/23.
 */

class CPURateUtil {
    public static float getRate(){
        Map<String,String> map1 = getMap();//采样第一次CPU信息快照
        long totalTime1 = Long.parseLong(map1.get("user")) + Long.parseLong(map1.get("nice"))
                + Long.parseLong(map1.get("system")) + Long.parseLong(map1.get("idle"))
                + Long.parseLong(map1.get("iowait")) + Long.parseLong(map1.get("irq"))
                + Long.parseLong(map1.get("softirq"));//获取totalTime1
        long idleTime1 = Long.parseLong(map1.get("idle"));//获取idleTime1
        try{
            Thread.sleep(360);//等待360ms
        }catch (Exception e){
            e.printStackTrace();
        }
        Map<String,String> map2 = getMap();//采样第二次CPU快照
        long totalTime2 = Long.parseLong(map2.get("user")) + Long.parseLong(map2.get("nice"))
                + Long.parseLong(map2.get("system")) + Long.parseLong(map2.get("idle"))
                + Long.parseLong(map2.get("iowait")) + Long.parseLong(map2.get("irq"))
                + Long.parseLong(map2.get("softirq"));//获取totalTime2
        long idleTime2 = Long.parseLong(map2.get("idle"));//获取idleTime2
        return (float) (100 * ((totalTime2 - totalTime1) - (idleTime2 - idleTime1)) / (totalTime2 - totalTime1));
    }

    //采样CPU信息快照的函数，返回Map类型
    private static Map<String, String> getMap() {
        String[] cpuInfos = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")));//读取CPU信息文件
            String load = br.readLine();
            Log.d("cpucpucpu", "getMap: "+load);
            br.close();
            cpuInfos = load.split(" ");
        }catch (IOException e){
            e.printStackTrace();
        }
        Map<String, String> map = new HashMap<>();
        assert cpuInfos != null;
        map.put("user",cpuInfos[2]);
        map.put("nice",cpuInfos[3]);
        map.put("system",cpuInfos[4]);
        map.put("idle",cpuInfos[5]);
        map.put("iowait",cpuInfos[6]);
        map.put("irq",cpuInfos[7]);
        map.put("softirq",cpuInfos[8]);
        return map;
    }
}
