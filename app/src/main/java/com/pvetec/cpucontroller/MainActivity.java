package com.pvetec.cpucontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btStopThread;
    private EditText etTargetRate;
    private TickerView mCPURate;
    private TickerView mThreadCount;
    private MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initview();
    }

    private void initview() {
        mCPURate = findViewById(R.id.cpu_rate);
        mCPURate.setCharacterLists(TickerUtils.provideNumberList());
        mThreadCount = findViewById(R.id.thread_count);
        mThreadCount.setCharacterLists(TickerUtils.provideNumberList());
        btStopThread = findViewById(R.id.stop_thread);
        etTargetRate = findViewById(R.id.target_rate);

        btStopThread.setOnClickListener(this);
        findViewById(R.id.start_service).setOnClickListener(this);
        findViewById(R.id.stop_service).setOnClickListener(this);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("UPDATE_NOWRATE");
        mIntentFilter.addAction("THREAD_COUNT");
        registerReceiver(myBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_service:
                Intent intentStart = new Intent(this, MyService.class);
                intentStart.putExtra("tagRate", etTargetRate.getText().toString().trim());
                startService(intentStart);
                break;
            case R.id.stop_service:
                Intent storpIntent = new Intent(this, MyService.class);
                stopService(storpIntent);
                mCPURate.setText("0");
                mThreadCount.setText("0");
                break;
            default:
                break;
        }
    }
    class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "UPDATE_NOWRATE":
                    mCPURate.setText(intent.getFloatExtra("nowRate", 0) + "");
                    break;
                case "THREAD_COUNT":
                    mThreadCount.setText(intent.getIntExtra("threadCount", 0) + "");
                    break;
                default:
                    break;
            }
        }
    }
}
