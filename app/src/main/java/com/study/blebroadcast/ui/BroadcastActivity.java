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
import com.study.blebroadcast.utils.BroadcastUtil;
import com.study.blebroadcast.utils.MonitorUtil;

import java.util.ArrayList;

public class BroadcastActivity extends Activity implements View.OnClickListener {

    /**
     * 广播次数
     */
    private int broadcastCount = 0;
    /**
     * 收发的数据集合
     */
    private ArrayList<MessageEntity> dataList;

    private static final String TAG = BroadcastActivity.class.getSimpleName();

    private TextView txtDevice;
    private CheckBox btnStart;
    private CheckBox btnStop;
    private RecyclerView mRecycleListView;

    private boolean isSend;
    private boolean isScan;

    private String sendMsg = "abcdefg";
    private MessageListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        BroadcastUtil.getInstance(this).initBLE();
        BroadcastUtil.getInstance(this).setServer();
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

    private void initData() {
        dataList = new ArrayList<>();
        listAdapter = new MessageListAdapter(dataList, this);
        mRecycleListView.setAdapter(listAdapter);
        mRecycleListView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initView() {
        mRecycleListView = findViewById(R.id.mRecycleListView);
        txtDevice = findViewById(R.id.txt_device);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
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


    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            startSend();
            btnStop.setChecked(false);
            txtDevice.setText("当前广播次数："+sum);
        } else if (v == btnStop) {
            stopSend();
            btnStart.setChecked(false);
            txtDevice.setText("广播未开启");
        }
    }
    private int sum;


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


    private void stopSend() {
        mHandler.removeMessages(0);
        BroadcastUtil.getInstance(this).stop();
        MonitorUtil.stop();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            startSend();
        }
    };

    @SuppressLint("SetTextI18n")
    private void startSend() {
        String msg = sendMsg + (sum++);
        BroadcastUtil.getInstance(BroadcastActivity.this).stop();
        BroadcastUtil.getInstance(BroadcastActivity.this).start(msg);
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
        txtDevice.setText("当前广播次数："+sum);
    }

    @Override
    protected void onDestroy() {
        stopSend();
        super.onDestroy();
    }
}