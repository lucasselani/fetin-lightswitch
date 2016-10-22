package br.inatel.lightswitch.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import br.inatel.lightswitch.R;

import static br.inatel.lightswitch.util.AdvertiserConfigurations.ADVERTISE_SETTINGS;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataLampOff;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataLampOn;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSensOff;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSensOn;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSimOff;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSimOn;
import static br.inatel.lightswitch.util.ScanConfigurations.SCAN_FILTERS;
import static br.inatel.lightswitch.util.ScanConfigurations.SCAN_SETTINGS;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener  {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getName();
    private static final String UUID_BEACON = "88c4649c-9875-4b8f-b2e6-5d06ae55f38c";
    private static final int INTERVAL = 1000;
    private Handler mAdvertiserHandler;
    private Handler mDialogHandler;
    private ProgressDialog mDialog;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private SwitchCompat lampSwitch;
    private SwitchCompat simulatorSwitch;
    private SwitchCompat sensorSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lampSwitch = (SwitchCompat) findViewById(R.id.lampSwitch);
        lampSwitch.setOnCheckedChangeListener(this);
        simulatorSwitch = (SwitchCompat) findViewById(R.id.simSwitch);
        simulatorSwitch.setOnCheckedChangeListener(this);
        sensorSwitch = (SwitchCompat) findViewById(R.id.sensSwitch);
        sensorSwitch.setOnCheckedChangeListener(this);

        mAdvertiserHandler = new Handler();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mDialogHandler = new Handler();
        mDialog = ProgressDialog.show(MainActivity.this,
                "", "Enviando comando.\nPor favor aguarde...", true, true);

        mDialogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
            }
        },INTERVAL);
        switch (buttonView.getId()){
            case R.id.lampSwitch:
                if(isChecked){
                    enableBluetoothAdvertiser(getAdvertiseDataLampOn(), mStartAdvertiseCallback);
                } else{
                    enableBluetoothAdvertiser(getAdvertiseDataLampOff(), mStopAdvertiseCallback);
                }
                break;
            case R.id.simSwitch:
                if(!isChecked){
                    enableBluetoothAdvertiser(getAdvertiseDataSimOn(), mStartAdvertiseCallback);
                }else{
                    enableBluetoothAdvertiser(getAdvertiseDataSimOff(), mStopAdvertiseCallback);
                }
                break;
            case R.id.sensSwitch:
                if(!isChecked){
                    enableBluetoothAdvertiser(getAdvertiseDataSensOn(), mStartAdvertiseCallback);
                }else{
                    enableBluetoothAdvertiser(getAdvertiseDataSensOff(), mStopAdvertiseCallback);
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        askEnableBluetooth();
        askPermissions();
        //enableBluetoothScanner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void askPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 3030);
        }
    }

    private void askEnableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
            Toast.makeText(this, "Seu dispositivo não suporta Bluetooth!", Toast.LENGTH_LONG);
        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
    }

    private void enableBluetoothScanner() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v(TAG, "Bluetooth Adapter Enabled");
        if (!mBluetoothAdapter.isEnabled()) return;
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mBluetoothLeScanner != null) {
            Log.v(TAG, "Bluetooth Scanner Enabled");
            mBluetoothLeScanner.startScan(SCAN_FILTERS, SCAN_SETTINGS, mScanCallback);
        } else {
            Toast.makeText(MainActivity.this,
                    "Seu dispositivo não suporta leitor de BLE!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void enableBluetoothAdvertiser(AdvertiseData advertiseData, AdvertiseCallback advertiseCallback) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v(TAG, "Bluetooth Adapter Enabled");
        if (!mBluetoothAdapter.isEnabled()) return;
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeScanner != null) {
            Log.v(TAG, "Bluetooth Scanner Enabled");
            mBluetoothLeAdvertiser.startAdvertising(ADVERTISE_SETTINGS, advertiseData, advertiseCallback);
        } else {
            Toast.makeText(MainActivity.this,
                    "Seu dispositivo não suporta BLE Advertiser!",
                    Toast.LENGTH_LONG).show();
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("MainActivity", "Callback: Success");
            ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord == null) {
                Log.w(TAG, "Null ScanRecord for device " + result.getDevice().getAddress());
                return;
            } else {
                String uuid = null;
                final String mac = result.getDevice().getAddress();

                List<ParcelUuid> serviceUuids = scanRecord.getServiceUuids();
                if(serviceUuids != null){
                    if(!serviceUuids.isEmpty()){
                        uuid = scanRecord.getServiceUuids().get(0).getUuid().toString();
                    } else return;
                }
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed errorCode " + errorCode);
        }
    };

    private final AdvertiseCallback mStartAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.v("Advertiser Enabled", settingsInEffect.toString());
            mAdvertiserHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeAdvertiser.stopAdvertising(mStartAdvertiseCallback);
                }
            }, INTERVAL);
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
        }
    };

    private final AdvertiseCallback mStopAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.v("Advertiser Disabled", settingsInEffect.toString());
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on.
                        // However local clients should wait for STATE_ON before attempting to use the adapter.
                        Log.v(TAG, "Bluetooth Adapter Enabling");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        //Indicates the local Bluetooth adapter is on, and ready for use.
                        //enableBluetoothScanner();
                        break;

                    case BluetoothAdapter.STATE_OFF:
                        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
                        break;
                }

            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            //enableBluetoothScanner();
        } catch (SecurityException se) {
            Toast.makeText(MainActivity.this,
                    "Não é possível usar o aplicativo sem conceder permissões!",
                    Toast.LENGTH_LONG).show();
            askPermissions();
        } catch (Exception e) {
            e.printStackTrace();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if (resultCode == REQUEST_ENABLE_BT) enableBluetoothScanner();
    }

    public static String getGuidFromByteArray(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid.toString();
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
