package com.pvetec.cpucontroller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etTargetRate;
    private TickerView mCPURate;
    private TickerView mThreadCount;
    private final MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mCPURate = findViewById(R.id.cpu_rate);
        mCPURate.setCharacterLists(TickerUtils.provideNumberList());
        mThreadCount = findViewById(R.id.thread_count);
        mThreadCount.setCharacterLists(TickerUtils.provideNumberList());
        etTargetRate = findViewById(R.id.target_rate);

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
                Intent stopIntent = new Intent(this, MyService.class);
                stopService(stopIntent);
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
            if (intent == null || intent.getAction() == null) {
                throw new NullPointerException();
            }
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
