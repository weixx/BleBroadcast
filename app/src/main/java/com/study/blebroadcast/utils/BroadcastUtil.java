package com.study.blebroadcast.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

public class BroadcastUtil {
    private static final String TAG = BroadcastUtil.class.getSimpleName();

    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_WRITE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_ENABLE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

    private BroadcastUtil util;
    private Activity mActivity;
    private String msg = "abc";
    private static BroadcastUtil broadcastUtil;

    public static BroadcastUtil getInstance(Activity activity){
        if(broadcastUtil == null) {
            synchronized (BroadcastUtil.class){
                if(broadcastUtil == null) {
                    broadcastUtil = new BroadcastUtil(activity);
                }
            }
        }
        return broadcastUtil;
    }
    private BroadcastUtil(Activity activity){
        this.mActivity = activity;
        initBLE();
        setServer();
    }


    private BluetoothManager mBluetoothManager;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic characterNotify;
    private BluetoothDevice bluetoothDevice;


//    private AdvertisingSet currentAdvertisingSet;
//    {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            AdvertisingSetParameters.Builder parameters = (new AdvertisingSetParameters.Builder())
//                    .setLegacyMode(false)
//                    .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
//                    .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
//                    .setPrimaryPhy(BluetoothDevice.PHY_LE_2M)
//                    .setSecondaryPhy(BluetoothDevice.PHY_LE_2M);
//            AdvertisingSetCallback callback = new AdvertisingSetCallback() {
//                @Override
//                public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
//                    Log.i(TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: " + status);
//                    currentAdvertisingSet = advertisingSet;
//                }
//
//                @Override
//                public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
//                    Log.i(TAG, "onAdvertisingSetStopped():");
//                }
//            };
//
//            AdvertiseData data = (new AdvertiseData.Builder()).addServiceData(new
//                            ParcelUuid(UUID.randomUUID()),
//                    "You should be able to fit large amounts of data up to maxDataLength.".getBytes()).build();
//
//            BluetoothLeAdvertiser advertiser =
//                    BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
//
//            advertiser.startAdvertisingSet(parameters.build(), data, null, null, null, callback);
//
//
//        }
//    }

    /**
     * 初始化蓝牙
     */
    public boolean initBLE() {
        if (!mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mActivity, "不支持BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        mBluetoothManager = (BluetoothManager) mActivity.getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(mActivity, "蓝牙不支持", Toast.LENGTH_LONG).show();
            return false;
        }
        mBluetoothAdapter.setName("WXX-BLE"); //你想叫啥名字，你愿意就好
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            boolean supported = mBluetoothAdapter.isLeExtendedAdvertisingSupported();
            boolean le2MPhySupported = mBluetoothAdapter.isLe2MPhySupported();
            if(supported) {
                int leMaximumAdvertisingDataLength = mBluetoothAdapter.getLeMaximumAdvertisingDataLength();
                Log.e(TAG,leMaximumAdvertisingDataLength+"<<");
                Toast.makeText(mActivity, "LE Extended Advertising is supported."+leMaximumAdvertisingDataLength, Toast.LENGTH_SHORT).show();
            }
            if (!mBluetoothAdapter.isLe2MPhySupported()) {
                Log.e(TAG, "2M PHY not supported!");
            }
            if (!mBluetoothAdapter.isLeExtendedAdvertisingSupported()) {
                Toast.makeText(mActivity, "LE Extended Advertising is not supported on this device.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "LE Extended Advertising not supported!");
            }
        }

        if(!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Toast.makeText(mActivity, "Bluetooth LE Advertising is not supported on this device.", Toast.LENGTH_SHORT).show();
            return false;
        }
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Toast.makeText(mActivity, "the device not support peripheral", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    /**
     * 添加服务，特征
     */
    public void setServer() {
        //读写特征
        BluetoothGattCharacteristic characterWrite = new BluetoothGattCharacteristic(
                UUID_LOST_WRITE, BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        //使能特征
        characterNotify = new BluetoothGattCharacteristic(UUID_LOST_ENABLE,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        characterNotify.addDescriptor(new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIG,
                BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ));
        //服务
        BluetoothGattService gattService = new BluetoothGattService(UUID_LOST_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //为服务添加特征
        gattService.addCharacteristic(characterWrite);
        gattService.addCharacteristic(characterNotify);
        //管理服务，连接和数据交互回调
        gattServer = mBluetoothManager.openGattServer(mActivity,
                new BluetoothGattServerCallback() {

                    @Override
                    public void onConnectionStateChange(final BluetoothDevice device,
                                                        final int status, final int newState) {
                        super.onConnectionStateChange(device, status, newState);
                        bluetoothDevice = device;
                        Log.d(TAG, "onConnectionStateChange:" + device + "    " + status + "   " + newState);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, device.getAddress() + "   " + device.getName() + "   " + status + "  " + newState);
                            }
                        });
                    }

                    @Override
                    public void onServiceAdded(int status,
                                               BluetoothGattService service) {
                        super.onServiceAdded(status, service);
                        Log.d(TAG, "service added");
                    }

                    @Override
                    public void onCharacteristicReadRequest(
                            BluetoothDevice device, int requestId, int offset,
                            BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicReadRequest(device, requestId,
                                offset, characteristic);
                        Log.d(TAG, "onCharacteristicReadRequest");

                        final String deviceInfo = "Address:" + device.getAddress();
                                   final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + characteristic.getUuid() + "|Value:" +
                                           ByteUtil.bytes2HexString(characteristic.getValue());
                        showInfo("=============================================");
                        showInfo("设备信息 " + deviceInfo);
                        showInfo("数据信息 " + info);
                        showInfo("=========onCharacteristicReadRequest=========");
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                    }

                    @Override
                    public void onCharacteristicWriteRequest(
                            BluetoothDevice device, int requestId,
                            BluetoothGattCharacteristic characteristic,
                            boolean preparedWrite, boolean responseNeeded,
                            int offset, final byte[] value) {
                        super.onCharacteristicWriteRequest(device, requestId,
                                characteristic, preparedWrite, responseNeeded,
                                offset, value);
                        Log.d(TAG, "onCharacteristicWriteRequest" + value[0]);
                        mActivity.runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                Log.e(TAG, value[0] + "");
                            }
                        });


                        final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
                        final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + characteristic.getUuid() + "|Value:" + ByteUtil.bytes2HexString(value);
                        //数据处理
                        showInfo("=============================================");
                        showInfo("设备信息 " + deviceInfo);
                        showInfo("数据信息 " + info);
                        showInfo("=========onCharacteristicWriteRequest=========");
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

                    }

                    @Override
                    public void onNotificationSent(BluetoothDevice device, int status) {
                        super.onNotificationSent(device, status);
                        final String info = "Address:" + device.getAddress() + "|status:" + status;
                        Log.i(TAG, "onNotificationSent: "+info);
                    }

                    @Override
                    public void onMtuChanged(BluetoothDevice device, int mtu) {
                        super.onMtuChanged(device, mtu);
                    }

                    @Override
                    public void onDescriptorReadRequest(BluetoothDevice device,
                                                        int requestId, int offset,
                                                        BluetoothGattDescriptor descriptor) {
                        super.onDescriptorReadRequest(device, requestId,
                                offset, descriptor);
                        Log.d(TAG, "onDescriptorReadRequest");
                        final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
                        final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + descriptor.getUuid();
                        showInfo("=============================================");
                        showInfo("设备信息 " + deviceInfo);
                        showInfo("数据信息 " + info);
                        showInfo("=========onDescriptorReadRequest=========");
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characterNotify.getValue());
                    }

                    @Override
                    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                                         BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                                         int offset, byte[] value) {
                        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                                offset, value);
                        Log.d(TAG, "onDescriptorWriteRequest");

                        final String deviceInfo = "Name:" + device.getAddress() + "|Address:" + device.getAddress();
                        final String info = "Request:" + requestId + "|Offset:" + offset + "|characteristic:" + descriptor.getUuid() + "|Value:" + ByteUtil.bytes2HexString(value);
                        showInfo("=============================================");
                         showInfo("设备信息 " + deviceInfo);
                         showInfo("数据信息 " + info);
                         showInfo("=========onDescriptorWriteRequest=========");
                        // 告诉连接设备做好了
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);

                    }

                    @Override
                    public void onExecuteWrite(BluetoothDevice device,
                                               int requestId, boolean execute) {
                        super.onExecuteWrite(device, requestId, execute);
                        Log.d(TAG, "onExecuteWrite");
                    }
                });
        try {
            gattServer.addService(gattService);
        } catch (Exception e) {
            Toast.makeText(mActivity,"请检查蓝牙开关",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     *广播的一些基本设置
     **/
    public AdvertiseSettings createAdvSettings(boolean connectAble, int timeoutMillis) {
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setConnectable(connectAble);//是否可以连接
        builder.setTimeout(timeoutMillis);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = builder.build();
        if (mAdvertiseSettings == null) {
            Toast.makeText(mActivity, "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }


    public void start(String msg) {
        this.msg = msg;
        /*
            刷新，不用
         *  characterNotify.setValue("HIHHHHH");
            gattServer.notifyCharacteristicChanged(bluetoothDevice, characterNotify, false);
         */

        mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0), createAdvertiseData(), mAdvertiseCallback);
    }


    public void stop() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }


    //广播数据
    public AdvertiseData createAdvertiseData() {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.setIncludeDeviceName(true); //广播名称也需要字节长度
        mDataBuilder.setIncludeTxPowerLevel(true);
        mDataBuilder.addServiceData(ParcelUuid.fromString(Constant.UUID),msg.getBytes());
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        if (mAdvertiseData == null) {
            Toast.makeText(mActivity, "mAdvertiseSettings == null", Toast.LENGTH_LONG).show();
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseData;
    }


    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if (settingsInEffect != null) {
                Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.e(TAG, "onStartSuccess, settingInEffect is null");
            }
            Log.e(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);



        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "onStartFailure errorCode" + errorCode);

            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                Toast.makeText(mActivity, "Advertise failed data too large", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                Toast.makeText(mActivity, "Advertise failed too many advertises", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising because no advertising instance is available.");
            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                Toast.makeText(mActivity, "Advertise failed already started", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Failed to start advertising as the advertising is already started");
            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                Toast.makeText(mActivity, "Advertise failed internal error", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Operation failed due to an internal error");
            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                Toast.makeText(mActivity, "Advertise failed feature unsupported", Toast.LENGTH_LONG).show();
                Log.e(TAG, "This feature is not supported on this platform");
            }
        }
    };

    private void showInfo(String msg){
        Log.e(TAG, msg);
    }
}