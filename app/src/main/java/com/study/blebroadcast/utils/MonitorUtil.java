package com.study.blebroadcast.utils;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.List;

public class MonitorUtil {

    private static final String TAG = MonitorUtil.class.getSimpleName();

    private static BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            final ScanRecordUtil scanRecordUtil = ScanRecordUtil.parseFromBytes(scanRecord);
            String name = device.getName();
            if(name != null && name.equals(Constant.DEVICE_NAME)) {
                Log.e(TAG, "MonitorUtil onLeScan " + ScanRecordUtil.parseFromBytes(scanRecord).toString());
                boolean b = scanRecordUtil.getServiceData().containsKey(ParcelUuid.fromString(Constant.UUID));
                if(b) {
                    byte[] bytes = scanRecordUtil.getServiceData().get(ParcelUuid.fromString(Constant.UUID));
                    String s = new String(bytes);
                    if(onScanResult != null) {
                        onScanResult.onResult(s);
                    }
                    Log.e(TAG, "MonitorUtil onLeScan " + s);
                    Log.e(TAG, "MonitorUtil onLeScan " + name);
                }
            }
        }
    };

    private static ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String name = result.getDevice().getName();
            if(name != null && name.equals(Constant.DEVICE_NAME)) {
                byte[] scanRecord = result.getScanRecord().getBytes();
                final ScanRecordUtil scanRecordUtil = ScanRecordUtil.parseFromBytes(scanRecord);
                Log.e(TAG, "MonitorUtil onLeScan " + ScanRecordUtil.parseFromBytes(scanRecord).toString());
                boolean b = scanRecordUtil.getServiceData().containsKey(ParcelUuid.fromString(Constant.UUID));
                if(b) {
                    byte[] bytes = scanRecordUtil.getServiceData().get(ParcelUuid.fromString(Constant.UUID));
                    String s = new String(bytes);
                    if(onScanResult != null) {
                        onScanResult.onResult(s);
                    }
                    Log.e(TAG, "MonitorUtil onLeScan " + s);
                    Log.e(TAG, "MonitorUtil onLeScan " + name);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public static void stop(){
//        BluetoothAdapter.getDefaultAdapter().stopLeScan(leScanCallback);
        BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().stopScan(scanCallback);
    }


    public static void start(){
//        BluetoothAdapter.getDefaultAdapter().startLeScan(leScanCallback);
        BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner().startScan(scanCallback);
    }
    private static OnScanResult onScanResult;

    public static void setOnScanResult(OnScanResult onScanResult) {
        MonitorUtil.onScanResult = onScanResult;
    }

    public interface OnScanResult{
        void onResult(String msg);
    }
}
