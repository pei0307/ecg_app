package com.example.user.mtu_size;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static UUID DATA_BLE =
            UUID.fromString(SampleGattAttributes.DATA_BLE);
    public final static UUID UUID_WRITE_TIME =
            UUID.fromString(SampleGattAttributes.WRITE_TIME);
    public final static UUID UUID_BLE_SERVICE =
            UUID.fromString(SampleGattAttributes.BLE_SERVICE);
    public final static UUID UUID_READ_TIME =
            UUID.fromString(SampleGattAttributes.READ_TIME);
    public final static UUID UUID_REQUEST =
            UUID.fromString(SampleGattAttributes.REQUEST);
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    int MTU_size = 251;
    byte packet_num;
    int fragment;
    double last_time = 0;
    byte[] total = new byte[550];

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.requestMtu(MTU_size);
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        //
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int MTU, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "change MTU success ");
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void testTXT(byte[] data) {
        try {
            FileWriter fw = new FileWriter("/sdcard/output1.txt", true);
            BufferedWriter bw = new BufferedWriter(fw); //�NBufferedWeiter�PFileWrite���󰵳s��
            for (byte byteChar : data) {
                bw.write(String.format("%02X ", byteChar));
            }
            bw.write("\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_REQUEST.equals(characteristic.getUuid())) {
            if (mBluetoothGatt.requestMtu(MTU_size) == true) {
                Log.d(TAG, "Success request MTU");
                starttonotify();
            }
        }
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (DATA_BLE.equals(characteristic.getUuid())) {        //char4
            // For all other profiles, writes the data formatted in HEX.

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {


                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                stringBuilder.append("\n");
                //寫入file
                try {
                    FileWriter fw = new FileWriter("/sdcard/output.txt", true);
                    BufferedWriter bw = new BufferedWriter(fw); //�NBufferedWeiter�PFileWrite���󰵳s��
                    bw.append(data.length + "\n");
                    bw.append(stringBuilder.toString());
                    //bw.append(String.format("%02X ", data[7])+String.format("%02X ", data[0])+String.format("%02X ", data[1])+String.format("%02X ", data[2])+"\n");
                    //connection_event_count / success_packet / total_packet /fragment / time
                    //bw.append((data[7]&0xFF)+" "+(data[0]&0xFF)+" "+(data[1]&0xFF)+" "+(data[2]&0xFF)+" "+(data[3]&0xFF)+(data[4]&0xFF)+(data[5]&0xFF)+(data[6]&0xFF)+"\n");
                    bw.newLine();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (data[1] == 0x01)
                {
                    fragment++;
                    packet_num = data[0] ;
                    System.arraycopy(data,0,total,0,data.length);

                }
                else if(data[0] == packet_num && data[1] == 0x02)
                {
                    fragment++;
                    System.arraycopy(data,8,total,248,data.length-8);

                }
                else if(data[0] == packet_num && data[1] == 0x03)
                {
                    fragment = 0x01;
                    System.arraycopy(data,8,total,488,data.length-8);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable(EXTRA_DATA, UploadData(total));
                    intent.putExtras(bundle);

                    //intent.putExtra(EXTRA_DATA, UploadData(total));
                }
                else
                {
                    fragment = 0x01;
                    Log.d("PacketLoss",packet_num+"");
                    //total_strBuilder.delete(0,total_strBuilder.length());
                }

                //intent.putExtra(EXTRA_DATA,data);
                //intent.putExtra(EXTRA_DATA, stringBuilder.toString());

            }
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);

    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
//                mBluetoothGatt.requestMtu(MTU_size);
//                mGattCallback.onMtuChanged (mBluetoothGatt,MTU_size,mConnectionState);
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
//        if (DATA_BLE.equals(characteristic.getUuid()))
//        {
//            temp = temp+1;
//            if (temp != 1) {
//                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
//                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                        UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                mBluetoothGatt.writeDescriptor(descriptor);
//            }
//        }
        if (DATA_BLE.equals(characteristic.getUuid()))
            mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // This is specific to Heart Rate Measurement.
        if (DATA_BLE.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    public void callMTU() {
        if (mBluetoothGatt.requestMtu(MTU_size) == true) {
            Log.d(TAG, "Success request MTU");
            starttonotify();
        }
    }

    public void starttonotify() {
        BluetoothGattService sendService = mBluetoothGatt.getService(UUID_BLE_SERVICE);//��?��00001805...�O?�ҡA????�ݭn??�w��?
        if (sendService != null) {
            BluetoothGattCharacteristic sendCharacteristic = sendService.getCharacteristic(DATA_BLE);//��?��00002a08...�O?�ҡA????�ݭn??�w��?
            if (sendCharacteristic != null) {
                setCharacteristicNotification(sendCharacteristic, true);
                readCharacteristic(sendCharacteristic);
            }
        }
    }
    private ECG_DATA[] UploadData(byte[] data) {
        String method="insert";
        Date t4 = new Date();
        SimpleDateFormat time4 = new SimpleDateFormat("HH:mm:ss.SSS");
        String T4 = time4.format(t4);
        Log.d("insert date", String.format("Time before insert: " + T4));
        int sample = 256;
        int Sequence_number = (data[0] & 0xFF);
        int Packet_num = (data[1] & 0xFF);
        int hr = 60 * 60 * 1000 * (data[2] & 0xFF);
        int min = 60 * 1000 * (data[3] & 0xFF);
        int sec = 1000 * (data[4] & 0xFF);
        int msec = 256 * (data[5] & 0xFF) + (data[6] & 0xFF);
        int totaltime = hr + min + sec + msec;
        if(last_time == 0) last_time = totaltime-1000;
        double interval = (totaltime - last_time) / 256;
        ECG_DATA[] Data_Per_Sec = new ECG_DATA[256];
        JSONArray json_ecg = new JSONArray();
        JSONObject count = new JSONObject();
        try {
            count.put("count",sample);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        json_ecg.put(count);
        for(int i = 0 ;i < 256 ; i++)
        {
            double temp = (data[8+(i*2)] & 0xFF)*256 +(data[8+(i*2)+1]& 0xFF);
            if(temp>32767)
                temp = temp-65535;
            double t = last_time + (i * interval) ;
            Data_Per_Sec[i] = new ECG_DATA(0,t,(temp/32768*72.2));
            json_ecg.put(Data_Per_Sec[i].toJsonObject());
        }
        Gsensor[] Gsensor_Per_Sec = new Gsensor[10];
        double ginterval = (totaltime - last_time) / 10;
        for(int j =0 ; j < 10 ; j++)
        {
            Gsensor_Per_Sec[j].setDeviceid(0);
            Gsensor_Per_Sec[j].setTime(last_time + (j * ginterval));
            double axis[] = new double[3];
            for(int u=0;u<3;u++){
                double t = (data[520+(j*3)+u] & 0xFF);
                if(t > 127) t = t -256;
                axis[u] = t*15.6/1000;
            }
            Gsensor_Per_Sec[j].getAxis(axis);
            //Gsensor_Per_Sec[j].setAxis_X((data[520+(j*3)] & 0xFF)*15.6/1000);
            //Gsensor_Per_Sec[j].setAxis_Y((data[520+(j*3)+1] & 0xFF)*15.6/1000);
            //Gsensor_Per_Sec[j].setAxis_Z((data[520+(j*3)+2] & 0xFF)*15.6/1000);
            json_ecg.put(Gsensor_Per_Sec[j].toJsonObject());
        }
        //BackgroundTask backgroundTask=new BackgroundTask(this);
        //backgroundTask.execute(method,json_ecg.toString());
        last_time = totaltime;
        return Data_Per_Sec;
    }
}