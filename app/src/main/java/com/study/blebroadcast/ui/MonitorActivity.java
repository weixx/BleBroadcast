package com.study.blebroadcast.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.study.blebroadcast.R;
import com.study.blebroadcast.adpater.MessageListAdapter;
import com.study.blebroadcast.bean.MessageEntity;
import com.study.blebroadcast.utils.Broadcast5Util;
import com.study.blebroadcast.utils.MonitorUtil;

import java.util.ArrayList;

public class MonitorActivity extends Activity implements View.OnClickListener {
    private final String TAG = MonitorActivity.class.getSimpleName();
    /**
     * 收发的数据集合
     */
    private ArrayList<MessageEntity> dataList;
    private RecyclerView mRecycleListView;

    private TextView txtDevice;
    private CheckBox btnStart;
    private CheckBox btnStop;


    private boolean isSend;
    private boolean isScan;
    private MessageListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        initView();
        initData();
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    private void initView() {
        mRecycleListView = findViewById(R.id.mRecycleListView);
        txtDevice = findViewById(R.id.txt_device);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void initData() {
        dataList = new ArrayList<>();
        listAdapter = new MessageListAdapter(dataList, this);
        mRecycleListView.setAdapter(listAdapter);
        mRecycleListView.setLayoutManager(new LinearLayoutManager(this));
    }

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                }
                break;
        }
    }

    public void ibBack(View view) {
        finish();
    }

    private int sum;
    private String sendMsg = "Received broadcast! ";

    @Override
    protected void onResume() {
        super.onResume();
        MonitorUtil.setOnScanResult(new MonitorUtil.OnScanResult() {
            @Override
            public void onResult(String msg) {
                if(isScan) {
                    isScan = false;
                    MessageEntity e = new MessageEntity();
                    e.setContent(msg);
                    e.setSend(false);
                    if(!dataList.contains(e)) {
                        dataList.add(e);
                    }
                    listAdapter.notifyDataSetChanged();
                    mRecycleListView.smoothScrollToPosition(listAdapter.getItemCount()-1);
                    mHandler.sendEmptyMessageDelayed(0,1000);
                }
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            startSend();
        }
    };
    private void startSend() {
        String msg = sendMsg + (sum++);
        Broadcast5Util.getInstance(MonitorActivity.this).stop();
        Broadcast5Util.getInstance(MonitorActivity.this).start(msg);
        MonitorUtil.start();
        isScan = true;

        MessageEntity e = new MessageEntity();
        e.setContent(msg);
        e.setSend(true);
        if(!dataList.contains(e)) {
            dataList.add(e);
        }
        listAdapter.notifyDataSetChanged();
        mRecycleListView.smoothScrollToPosition(listAdapter.getItemCount()-1);
    }


    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            start();
            btnStop.setChecked(false);
            txtDevice.setText("监听中..");
        } else if (v == btnStop) {
            stop();
            btnStart.setChecked(false);
            txtDevice.setText("监听未开启");
        }
    }

    private void start() {
        MonitorUtil.start();
        isScan = true;
    }

    private void stop() {
        mHandler.removeMessages(0);
        MonitorUtil.stop();
        Broadcast5Util.getInstance(MonitorActivity.this).stop();
    }

    @Override
    protected void onDestroy() {
        stop();
        Broadcast5Util.getInstance(MonitorActivity.this).stop();
        super.onDestroy();
    }


}
