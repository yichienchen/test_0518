package com.example.test_0518;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import static com.example.test_0518.MainActivity.AdvertiseCallbacks_map;
import static com.example.test_0518.MainActivity.TAG;

import static com.example.test_0518.MainActivity.extendedAdvertiseCallbacks_map;
import static com.example.test_0518.MainActivity.mAdvertiseCallback;
import static com.example.test_0518.MainActivity.mBluetoothLeAdvertiser;

import static com.example.test_0518.MainActivity.startAdvButton;
import static com.example.test_0518.MainActivity.stopAdvButton;
import static com.example.test_0518.MainActivity.version;
import static com.example.test_0518.MainActivity.id_byte;


public class Service_Adv extends Service {
    static int packet_num;
    static int pdu_len;
    static String test = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Service_Adv() {


        startAdvertising();
        stopAdvButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopAdvertising();
                stopSelf();
            }
        });
        startAdvButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAdvertising();
            }
        });


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startAdvertising(){
        Log.e(TAG, "Service: Starting Advertising");


        if(version){
            pdu_len=18;
            packet_num = (test.length()/pdu_len)+1;
        }else {
            pdu_len=1632;
            packet_num = (test.length()/pdu_len)+1;
        }

        if (mAdvertiseCallback == null) {
            if (mBluetoothLeAdvertiser != null) {
                for (int q=0;q<packet_num;q++){
                    startBroadcast(q);
                }
            }
        }

        startAdvButton.setVisibility(View.INVISIBLE);
        stopAdvButton.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startBroadcast(Integer order) {
        String localName =  String.valueOf(order) ;
        BluetoothAdapter.getDefaultAdapter().setName(localName);
        Log.e(TAG,"localName:"+localName);

        //BLE4.0
        if (version) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData advertiseData = buildAdvertiseData(order);
            AdvertiseData scanResponse = buildAdvertiseData_scan_response(order);
            mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, new Service_Adv.MyAdvertiseCallback(order));

        } else {
            //BLE 5.0
            AdvertiseData advertiseData = buildAdvertiseData(order);  //order
            AdvertiseData advertiseData_extended = buildAdvertiseData_extended(order);
            AdvertiseData periodicData = buildAdvertiseData_periodicData();
            AdvertisingSetParameters parameters = buildAdvertisingSetParameters();
            PeriodicAdvertisingParameters periodicParameters = buildperiodicParameters();
//            mBluetoothLeAdvertiser.startAdvertisingSet(parameters,advertiseData,null,periodicParameters,
//                    periodicData,0,0,new ExtendedAdvertiseCallback(order));

            mBluetoothLeAdvertiser.startAdvertisingSet(parameters,advertiseData_extended,null,
                    null,null,0,0,new ExtendedAdvertiseCallback(order));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopAdvertising(){
        if (mBluetoothLeAdvertiser != null) {
            for (int q=0;q<packet_num;q++){
                stopBroadcast(q);
            }
            mAdvertiseCallback = null;
        }
        stopAdvButton.setVisibility(View.INVISIBLE);
        startAdvButton.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopBroadcast(Integer order) {
        final AdvertiseCallback adCallback = AdvertiseCallbacks_map.get(order);
        final AdvertisingSetCallback exadvCallback = extendedAdvertiseCallbacks_map.get(order);
        if (!version) {
            //BLE 5.0
            if (exadvCallback != null) {
                try {
                    if (mBluetoothLeAdvertiser != null) {
                        mBluetoothLeAdvertiser.stopAdvertisingSet(exadvCallback);
                    }
                    else {
                        Log.w(TAG,"Not able to stop broadcast; mBtAdvertiser is null");
                    }
                }
                catch(RuntimeException e) { // Can happen if BT adapter is not in ON state
                    Log.w(TAG,"Not able to stop broadcast; BT state: {}");
                }
                extendedAdvertiseCallbacks_map.remove(order);
            }
            //Log.e(TAG,order +" Advertising successfully stopped.");
        }else {
            //BLE 4.0
            if (adCallback != null) {
                try {
                    if (mBluetoothLeAdvertiser != null) {
                        mBluetoothLeAdvertiser.stopAdvertising(adCallback);
                    }
                    else {
                        Log.w(TAG,"Not able to stop broadcast; mBtAdvertiser is null");
                    }
                }
                catch(RuntimeException e) { // Can happen if BT adapter is not in ON state
                    Log.w(TAG,"Not able to stop broadcast; BT state: {}");
                }
                AdvertiseCallbacks_map.remove(order);
            }
            Log.e(TAG,order +" Advertising successfully stopped");
        }
    }

    public static byte[][] Adv_data_seg(int x){
        StringBuilder data = new StringBuilder(test);
        for(int c=data.length();c%pdu_len!=0;c++){
            data.append("0");
        }
        byte[] data_byte = data.toString().getBytes();
        byte[][] adv_byte = new byte[packet_num][pdu_len+id_byte.length+1];

        for (int counter = 1 ; counter >0; counter = counter-pdu_size) {

        }

        System.arraycopy(id_byte, 0, adv_byte, 0, id_byte.length);
        System.arraycopy(data_byte, 0, adv_byte, id_byte.length, data_byte.length);
        return adv_byte;
    }


    //BLE 4.0
    public static class MyAdvertiseCallback extends AdvertiseCallback {
        private final Integer _order;
        MyAdvertiseCallback(Integer order) {
            _order = order;
        }
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Advertising failed errorCode: "+errorCode);
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    Log.e(TAG,"ADVERTISE_FAILED_ALREADY_STARTED");
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    Log.e(TAG,"ADVERTISE_FAILED_DATA_TOO_LARGE");
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    Log.e(TAG,"ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    Log.e(TAG,"ADVERTISE_FAILED_INTERNAL_ERROR");
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    Log.e(TAG,"ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
                    break;
                default:
                    Log.e(TAG,"Unhandled error : "+errorCode);
            }
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.e(TAG, _order +" Advertising successfully started");
            AdvertiseCallbacks_map.put(_order, this);
        }
    }

    static AdvertiseData buildAdvertiseData(Integer order) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.setIncludeDeviceName(true);
        dataBuilder.setIncludeTxPowerLevel(false);
        dataBuilder.addManufacturerData(0xffff,Adv_data_seg(order));

        return dataBuilder.build();
    }

    static AdvertiseData buildAdvertiseData_scan_response(Integer order) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.addManufacturerData(0xffff,Adv_data_seg(order));
        return dataBuilder.build();
    }

    //BLE 5.0
    @RequiresApi(api = Build.VERSION_CODES.O)
    public class ExtendedAdvertiseCallback extends AdvertisingSetCallback {
        private final Integer _order;
        ExtendedAdvertiseCallback(Integer order) {
            _order = order;
        }

        @Override
        public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
            if (status==AdvertisingSetCallback.ADVERTISE_FAILED_ALREADY_STARTED)
                Log.e(TAG, "ADVERTISE_FAILED_ALREADY_STARTED");
            else if (status==AdvertisingSetCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
                Log.e(TAG, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED");
            else if (status==AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE)
                Log.e(TAG, "ADVERTISE_FAILED_DATA_TOO_LARGE");
            else if (status==AdvertisingSetCallback.ADVERTISE_FAILED_INTERNAL_ERROR)
                Log.e(TAG, "ADVERTISE_FAILED_INTERNAL_ERROR");
            else if (status==AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS)
                Log.e(TAG, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS");
            else if (status==AdvertisingSetCallback.ADVERTISE_SUCCESS) {
                Log.e(TAG,   "ADVERTISE_SUCCESS" + "(" + _order + ")");
                startAdvButton.setVisibility(View.INVISIBLE);
                stopAdvButton.setVisibility(View.VISIBLE);
                extendedAdvertiseCallbacks_map.put(_order,this);
            }
        }
        @Override
        public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
            Log.e(TAG, "onAdvertisingSetStopped:" + "("+ _order +")");
        }

        @Override
        public void onAdvertisingEnabled (AdvertisingSet advertisingSet, boolean enable, int status) {
            Log.e(TAG,"onAdvertisingEnabled: " + enable + "("+ _order +")");
            stopAdvButton.setVisibility(View.INVISIBLE);
            startAdvButton.setVisibility(View.VISIBLE);
        }
    }

    public static AdvertiseSettings buildAdvertiseSettings() {
        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(false)
                .setTimeout(0);
        return settingsBuilder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static AdvertisingSetParameters buildAdvertisingSetParameters() {
        AdvertisingSetParameters.Builder parametersBuilder = new AdvertisingSetParameters.Builder()
                .setConnectable(false)
                .setInterval(400)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
                .setLegacyMode(false);
        return parametersBuilder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static PeriodicAdvertisingParameters buildperiodicParameters() {
        PeriodicAdvertisingParameters.Builder periodicparametersBuilder = new PeriodicAdvertisingParameters.Builder()
                .setInterval(200);
        return periodicparametersBuilder.build();
    }

    static AdvertiseData buildAdvertiseData_extended(int order) {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.setIncludeDeviceName(true);
//        Log.e(TAG,"data: "+Data_adv.getBytes().length);
//        dataBuilder.addManufacturerData(0xffff,adv_data.get(order).getBytes());
        dataBuilder.addManufacturerData(0xffff,test.getBytes());
        return dataBuilder.build();
    }

    //TODO data要改
    static AdvertiseData buildAdvertiseData_periodicData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        byte[] data = {0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a,0x00,0x11,0xf,0x1a};
        dataBuilder.addManufacturerData(0xffff,data);
        return dataBuilder.build();
    }
}