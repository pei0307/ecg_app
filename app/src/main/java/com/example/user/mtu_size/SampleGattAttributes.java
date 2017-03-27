package com.example.user.mtu_size;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String DATA_BLE="0000fff4-0000-1000-8000-00805f9b34fb";
    public static String WRITE_TIME="0000fff1-0000-1000-8000-00805f9b34fb";
    public static String READ_TIME="0000fff2-0000-1000-8000-00805f9b34fb";
    public static String REQUEST="0000fff3-0000-1000-8000-00805f9b34fb";
    public static String BLE_SERVICE="0000fff0-0000-1000-8000-00805f9b34fb";
    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "12345");
        attributes.put("0000fff1-0000-1000-8000-00805f9b34fb", "Characteristic1");
        attributes.put("0000fff2-0000-1000-8000-00805f9b34fb", "Characteristic2");
        attributes.put("0000fff3-0000-1000-8000-00805f9b34fb", "Characteristic3");
        attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "Characteristic4");
        attributes.put("0000fff5-0000-1000-8000-00805f9b34fb", "Characteristic5");
    }
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}