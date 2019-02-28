package com.study.blebroadcast.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.O)
public class Broadcast5Util {
    private static final String TAG = Broadcast5Util.class.getSimpleName();

    private Broadcast5Util util;
    private Activity mActivity;
    private String msg = "abc";
    private static Broadcast5Util broadcastUtil;

    public static Broadcast5Util getInstance(Activity activity){
        if(broadcastUtil == null) {
            synchronized (Broadcast5Util.class){
                if(broadcastUtil == null) {
                    broadcastUtil = new Broadcast5Util(activity);
                }
            }
        }
        return broadcastUtil;
    }
    private Broadcast5Util(Activity activity){
        this.mActivity = activity;
        initBLE();
    }

    private AdvertisingSetParameters.Builder parameters ;

    private AdvertisingSetCallback callback ;

    private BluetoothLeAdvertiser advertiser  ;

    /**
     * 初始化蓝牙
     */
    public boolean initBLE() {
        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mActivity, "not supported BLE", Toast.LENGTH_SHORT).show();
            return false;
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(mActivity, "检查蓝牙开关", Toast.LENGTH_SHORT).show();
            return false;
        }
        mBluetoothAdapter.setName(Constant.DEVICE_NAME);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if(mBluetoothAdapter.isLeExtendedAdvertisingSupported()) {
                int leMaximumAdvertisingDataLength = mBluetoothAdapter.getLeMaximumAdvertisingDataLength();
                Log.e(TAG,leMaximumAdvertisingDataLength+"<<");
                Toast.makeText(mActivity, "LE Extended Advertising is supported."+leMaximumAdvertisingDataLength, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mActivity, "LE Extended Advertising is not supported on this device.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!mBluetoothAdapter.isLe2MPhySupported()) {
                Toast.makeText(mActivity, "2M PHY not supported!", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!mBluetoothAdapter.isLeCodedPhySupported()) {
                Toast.makeText(mActivity, "LE Coded PHY feature is not supported!", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else {
            Toast.makeText(mActivity, "android os not supported!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(mActivity, "Bluetooth LE Advertising is not supported on this device.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mBluetoothAdapter.getBluetoothLeAdvertiser() == null) {
            Toast.makeText(mActivity, "the device not support peripheral", Toast.LENGTH_SHORT).show();
            return false;
        }

        parameters = (new AdvertisingSetParameters.Builder())
                .setLegacyMode(false)
                .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
                .setPrimaryPhy(BluetoothDevice.PHY_LE_CODED)
                .setSecondaryPhy(BluetoothDevice.PHY_LE_2M);

        callback = new AdvertisingSetCallback() {
            @Override
            public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                Log.i(TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: " + status);
            }

            @Override
            public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                Log.i(TAG, "onAdvertisingSetStopped():");
            }
        };

        advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        return true;
    }

    public void start(String msg) {
        this.msg = msg;
        //构造广播数据
        AdvertiseData data = new AdvertiseData.Builder().addServiceData(ParcelUuid.fromString(Constant.UUID),
                msg.getBytes()).build();
        //开始广播
        advertiser.startAdvertisingSet(parameters.build(), data, null, null, null, callback);
    }

    public void stop() {
        //停止广播
        advertiser.stopAdvertisingSet(callback);
    }


    private void showInfo(String msg){
        Log.e(TAG, msg);
    }
}