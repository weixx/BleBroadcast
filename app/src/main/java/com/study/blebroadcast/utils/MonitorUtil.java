package com.study.blebroadcast.utils;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;

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
    public static void stop(){
        BluetoothAdapter.getDefaultAdapter().stopLeScan(leScanCallback);
    }


    public static void start(){
        BluetoothAdapter.getDefaultAdapter().startLeScan(leScanCallback);
    }
    private static OnScanResult onScanResult;

    public static void setOnScanResult(OnScanResult onScanResult) {
        MonitorUtil.onScanResult = onScanResult;
    }

    public interface OnScanResult{
        void onResult(String msg);
    }
}
