package com.pvetec.cpucontroller;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CpuRateGetListener {

    private EditText etTargetRate;
    private TickerView mCPURate;
    private TickerView mThreadCount;

    private MyService.CpuRateBinder mBinder;
    private ServiceConnection connect = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MyService.CpuRateBinder) service;
            mBinder.setOnGetListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        Intent intentStart = new Intent(this, MyService.class);
        startService(intentStart);
        bindService(intentStart, connect, BIND_AUTO_CREATE);
    }

    private void initView() {
        mCPURate = findViewById(R.id.cpu_rate);
        mCPURate.setCharacterLists(TickerUtils.provideNumberList());
        mThreadCount = findViewById(R.id.thread_count);
        mThreadCount.setCharacterLists(TickerUtils.provideNumberList());
        etTargetRate = findViewById(R.id.target_rate);

        findViewById(R.id.start_service).setOnClickListener(this);
        findViewById(R.id.stop_service).setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connect);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_service:
                mBinder.startTask(etTargetRate.getText().toString().trim());
                break;
            case R.id.stop_service:
                mBinder.stopTask();
                mCPURate.setText("0");
                mThreadCount.setText("0");
                break;
            default:
                break;
        }
    }

    @Override
    public void onRateGet(float rate) {
        mCPURate.setText(rate + "");
    }

    @Override
    public void onThreadUpdate(int count) {
        mThreadCount.setText(count + "");
    }

}
