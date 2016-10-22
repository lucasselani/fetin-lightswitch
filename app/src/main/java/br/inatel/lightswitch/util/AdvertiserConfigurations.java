package br.inatel.lightswitch.util;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by lucas on 15/09/2016.
 */
public class AdvertiserConfigurations {
    private ParcelUuid PARCEL_UUID = new ParcelUuid(UUID.fromString("CDB7950D-73F1-4D4D-8E47-C090502DBD63"));

    public static AdvertiseData getAdvertiseDataLampOn(){
        ByteBuffer mManufacturerData = ByteBuffer.allocate(2);
        mManufacturerData.put(0, (byte) 0xAA);
        mManufacturerData.put(1, (byte) 0x01);

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                //.addServiceUuid(PARCEL_UUID)
                //.addServiceData(PARCEL_UUID, "Data".getBytes(Charset.forName("UTF-8")))
                .addManufacturerData(48812, mManufacturerData.array())
                .build();
        Log.v("AdvertiserData", mManufacturerData.array().toString());
        return advertiseData;
    }

    public static AdvertiseData getAdvertiseDataLampOff(){
        ByteBuffer mManufacturerData = ByteBuffer.allocate(2);
        mManufacturerData.put(0, (byte) 0xAA);
        mManufacturerData.put(1, (byte) 0x02);

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                //.addServiceUuid(PARCEL_UUID)
                //.addServiceData(PARCEL_UUID, "Data".getBytes(Charset.forName("UTF-8")))
                .addManufacturerData(48812, mManufacturerData.array())
                .build();
        Log.v("AdvertiserData", mManufacturerData.array().toString());
        return advertiseData;
    }

    public static AdvertiseData getAdvertiseDataSimOn(){
        ByteBuffer mManufacturerData = ByteBuffer.allocate(2);
        mManufacturerData.put(0, (byte) 0xAA);
        mManufacturerData.put(1, (byte) 0x11);

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                //.addServiceUuid(PARCEL_UUID)
                //.addServiceData(PARCEL_UUID, "Data".getBytes(Charset.forName("UTF-8")))
                .addManufacturerData(48812, mManufacturerData.array())
                .build();
        Log.v("AdvertiserData", mManufacturerData.array().toString());
        return advertiseData;
    }

    public static AdvertiseData getAdvertiseDataSimOff(){
        ByteBuffer mManufacturerData = ByteBuffer.allocate(2);
        mManufacturerData.put(0, (byte) 0xAA);
        mManufacturerData.put(1, (byte) 0x12);

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                //.addServiceUuid(PARCEL_UUID)
                //.addServiceData(PARCEL_UUID, "Data".getBytes(Charset.forName("UTF-8")))
                .addManufacturerData(48812, mManufacturerData.array())
                .build();
        Log.v("AdvertiserData", mManufacturerData.array().toString());
        return advertiseData;
    }

    public static AdvertiseData getAdvertiseDataSensOn(){
        ByteBuffer mManufacturerData = ByteBuffer.allocate(2);
        mManufacturerData.put(0, (byte) 0xAA);
        mManufacturerData.put(1, (byte) 0x21);

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                //.addServiceUuid(PARCEL_UUID)
                //.addServiceData(PARCEL_UUID, "Data".getBytes(Charset.forName("UTF-8")))
                .addManufacturerData(48812, mManufacturerData.array())
                .build();
        Log.v("AdvertiserData", mManufacturerData.array().toString());
        return advertiseData;
    }

    public static AdvertiseData getAdvertiseDataSensOff(){
        ByteBuffer mManufacturerData = ByteBuffer.allocate(2);
        mManufacturerData.put(0, (byte) 0xAA);
        mManufacturerData.put(1, (byte) 0x22);

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                //.addServiceUuid(PARCEL_UUID)
                //.addServiceData(PARCEL_UUID, "Data".getBytes(Charset.forName("UTF-8")))
                .addManufacturerData(48812, mManufacturerData.array())
                .build();
        Log.v("AdvertiserData", mManufacturerData.array().toString());
        return advertiseData;
    }

    public static AdvertiseSettings ADVERTISE_SETTINGS =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build();

}
