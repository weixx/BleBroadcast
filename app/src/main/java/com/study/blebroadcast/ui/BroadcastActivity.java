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
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.study.blebroadcast.R;
import com.study.blebroadcast.adpater.MessageListAdapter;
import com.study.blebroadcast.bean.MessageEntity;
import com.study.blebroadcast.utils.Broadcast5Util;
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
    private TextView tvSendBroadcastContent;
    private EditText etBroadcastContent;
    private RecyclerView mRecycleListView;

    private boolean isSend;//是否是手动输入发送的
    private boolean isScan;

    private String sendMsg = "You should be able to fit large amounts of data up to maxDataLength. This goes" +
            " up to 1650 bytes. For legacy advertising this would not work ";
    private MessageListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);
        Broadcast5Util.getInstance(this).initBLE();
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
        tvSendBroadcastContent = findViewById(R.id.tvSendBroadcastContent);
        etBroadcastContent = findViewById(R.id.etBroadcastContent);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        tvSendBroadcastContent.setOnClickListener(this);
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        if (v == btnStart) {
            startSend();
            btnStop.setChecked(false);
            isSend = false;
        } else if (v == btnStop) {
            stopSend();
            btnStart.setChecked(false);
            txtDevice.setText("广播未开启");
        } else if (v == tvSendBroadcastContent) {
            //发送自定义广播
            String msg = etBroadcastContent.getText().toString().trim();
            if(TextUtils.isEmpty(msg)) {
                Toast.makeText(this, "请输入需要广播的内容", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMsg = msg + " ";
            stopSend();//先停止之前的广播
            btnStop.setChecked(false);
            btnStart.setChecked(false);
            startSend();
            isSend = true;
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
                    if(!isSend) {
                        mHandler.sendEmptyMessageDelayed(0,1000);
                    }
                }
            }
        });
    }


    private void stopSend() {
        mHandler.removeMessages(0);
        Broadcast5Util.getInstance(this).stop();
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
        Broadcast5Util.getInstance(this).stop();
        Broadcast5Util.getInstance(this).start(msg);
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