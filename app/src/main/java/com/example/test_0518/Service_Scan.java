package com.example.test_0518;

import android.app.Service;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

import static com.example.test_0518.Function.byte2HexStr;
import static com.example.test_0518.MainActivity.ManufacturerData_size;
import static com.example.test_0518.MainActivity.TAG;
import static com.example.test_0518.MainActivity.data_;
import static com.example.test_0518.MainActivity.id_byte;
import static com.example.test_0518.MainActivity.list_device;
import static com.example.test_0518.MainActivity.list_device_detail;
import static com.example.test_0518.MainActivity.mBluetoothLeScanner;
import static com.example.test_0518.MainActivity.matrix;
import static com.example.test_0518.MainActivity.mean_total;
import static com.example.test_0518.MainActivity.num_list;
import static com.example.test_0518.MainActivity.num_time;
import static com.example.test_0518.MainActivity.num_total;
import static com.example.test_0518.MainActivity.peripheralTextView;
import static com.example.test_0518.MainActivity.startScanningButton;
import static com.example.test_0518.MainActivity.stopScanningButton;
import static com.example.test_0518.MainActivity.time_interval;
import static com.example.test_0518.MainActivity.time_previous;
import static com.example.test_0518.Service_Adv.test;
import static com.example.test_0518.Service_scan_function.leScanCallback;
import static com.example.test_0518.Service_scan_function.received_time;
import static com.example.test_0518.Service_scan_function.received_time_Calendar;
import static com.example.test_0518.Service_scan_function.received_time_interval;
import static com.example.test_0518.Service_scan_function.rssi_level_1;
import static com.example.test_0518.Service_scan_function.rssi_level_2;
import static com.example.test_0518.Service_scan_function.rssi_level_3;

public class Service_Scan extends Service {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Service_Scan() {
        Log.e(TAG,"Service_Scan start");
        startScanning();
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
                stopSelf();
            }
        });
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                startScanning();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startScanning() {
        received_time.clear();
        received_time_interval.clear();
        received_time_Calendar.clear();

        Log.e(TAG,"start scanning");


        list_device.clear();
        list_device_detail.clear();

        num_total.clear();
        time_previous.clear();
        mean_total.clear();
        matrix.clear();
        time_interval.clear();

        rssi_level_1.clear();
        rssi_level_2.clear();
        rssi_level_3.clear();

        num_list.clear();
        num_time.clear();


        long zero=0;
        for (int j=0;j<100;j++){  //100 : mac address數量上限
            num_total.add(1);
            time_previous.add(zero);
            mean_total.add(zero);
        }

        //add six row
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());


        peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);

        byte[] data_all = new byte[ManufacturerData_size];
        System.arraycopy(id_byte, 0, data_all, 1, id_byte.length);


        byte[] data_mask = new byte[] {0x00,0x11,0x11,0x11,0x11,0x11,0x11,0x11,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
//        Log.e(TAG,"data_all: "+ byte2HexStr(data_all)+"\n"
//                +"data_mask: "+byte2HexStr(data_mask));


        ScanFilter Mau_filter_extended = new ScanFilter.Builder().setManufacturerData(0xffff,test.getBytes()).build();

        ScanFilter Mau_filter_legacy = new ScanFilter.Builder().setManufacturerData(0xffff,data_all,data_mask).build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(Mau_filter_extended);
        filters.add(Mau_filter_legacy);


        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(2)
                .setLegacy(false)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)  //Fails to start power optimized scan as this feature is not supported
//                .setMatchMode()
//                .setNumOfMatches(1)
//                .setReportDelay()
                .build();
//        btScanner.flushPendingScanResults(leScanCallback);
        mBluetoothLeScanner.startScan(filters, settings, leScanCallback);
    }

    public void stopScanning() {
        Log.e(TAG,"stopping scanning");

        Log.e(TAG,"list_device: "+list_device);
        for(int i =0 ; i< list_device.size() ; i++){
//            Log.e(TAG,"time_interval: "+time_interval.get(i)+rssi_level_1.get(i)+","+rssi_level_2.get(i)+","+rssi_level_3.get(i));
        }

        peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(leScanCallback);
            }
        });
    }
}
