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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;

import br.inatel.lightswitch.R;
import br.inatel.lightswitch.adapter.BeaconListAdapter;
import br.inatel.lightswitch.model.Beacon;

import static br.inatel.lightswitch.util.AdvertiserConfigurations.ADVERTISE_SETTINGS;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataLampOff;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataLampOn;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSensOff;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSensOn;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSimOff;
import static br.inatel.lightswitch.util.AdvertiserConfigurations.getAdvertiseDataSimOn;
import static br.inatel.lightswitch.util.ScanConfigurations.SCAN_FILTERS;
import static br.inatel.lightswitch.util.ScanConfigurations.SCAN_SETTINGS;

public class MainActivity extends AppCompatActivity implements BeaconListAdapter.OnCoumpoundButtonClicked  {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getName();
    private static final String UUID_BEACON = "88c4649c-9875-4b8f-b2e6-5d06ae55f38c";
    private static final int INTERVAL = 500;
    private static final int LE_CALLBACK_TIMEOUT_MILLIS = 2000;
    private static final int SCAN_REFRESH_INTERVAL = 1500;
    private static final int REFRESH_DIALOG_TIMEOUT = 6000;
    private ArrayList<Beacon> beacons = null;
    private BeaconListAdapter beaconListAdapter;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler mAdvertiserHandler;
    private Handler mDialogHandler;
    private ProgressDialog mAdvertiserDialog;
    private ProgressDialog mScannerDialog;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_main);
        swipeRefreshLayout.setOnRefreshListener(onRefreshListenerListener);

        beacons = new ArrayList<>();
        beaconListAdapter = new BeaconListAdapter(beacons, this);
        beaconListAdapter.setOnCoumpoundButtonClicked(this);
        listView = (ListView) findViewById(R.id.list);
        listView.setItemsCanFocus(true);
        listView.setAdapter(beaconListAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshAppList();
            }
        });
    }

    SwipeRefreshLayout.OnRefreshListener onRefreshListenerListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refreshAppList();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onSwitchCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mDialogHandler = new Handler();
        mAdvertiserDialog = ProgressDialog.show(MainActivity.this,
                "", "Enviando comando.\nPor favor aguarde...", true, true);

        mDialogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdvertiserDialog.dismiss();
            }
        },INTERVAL+LE_CALLBACK_TIMEOUT_MILLIS);

        View relativeLayout = (View) buttonView.getParent();
        View rootLayout = (View) relativeLayout.getParent();
        ListView listView = (ListView) rootLayout.getParent();
        int position = listView.getPositionForView(rootLayout);

        byte[] id = ((Beacon) beaconListAdapter.getItem(position)).getId();

        switch (buttonView.getId()){
            case R.id.lampSwitch:
                if(isChecked){
                    enableBluetoothAdvertiser(getAdvertiseDataLampOn(id));

                } else{
                    enableBluetoothAdvertiser(getAdvertiseDataLampOff(id));
                }
                break;
            case R.id.simSwitch:
                if(!isChecked){
                    enableBluetoothAdvertiser(getAdvertiseDataSimOn(id));
                }else{
                    enableBluetoothAdvertiser(getAdvertiseDataSimOff(id));
                }
                break;
            case R.id.sensSwitch:
                if(!isChecked){
                    enableBluetoothAdvertiser(getAdvertiseDataSensOn(id));
                }else{
                    enableBluetoothAdvertiser(getAdvertiseDataSensOff(id));
                }
                break;
            default:
                break;
        }
    }

    public void refreshAppList(){
            if(mBluetoothAdapter.isEnabled()){
                mScannerDialog = ProgressDialog.show(MainActivity.this,
                        "", "Atualizando a página.\nPor favor aguarde...", true, true);
                mBluetoothLeScanner.stopScan(mScanCallback);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0; i<beacons.size(); i++){
                            beacons.remove(i);
                        }
                        beaconListAdapter.notifyDataSetInvalidated();
                        mBluetoothLeScanner.startScan(SCAN_FILTERS,SCAN_SETTINGS,mScanCallback);
                    }
                }, SCAN_REFRESH_INTERVAL);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mScannerDialog != null){
                        if(mScannerDialog.isShowing()){
                            mScannerDialog.dismiss();
                            Snackbar.make(findViewById(R.id.coordinatorLayout),
                                    "A atualização falhou!\nPor favor realize-a novamente.",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }, REFRESH_DIALOG_TIMEOUT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        askEnableBluetooth();
        askPermissions();
        enableBluetoothScanner();
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
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    "Seu dispositivo não suporta Bluetooth!",
                    Snackbar.LENGTH_LONG).show();
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
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    "Seu dispositivo não suporta scanner de Bluetooth Low Energy!",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void enableBluetoothAdvertiser(AdvertiseData advertiseData) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.v(TAG, "Bluetooth Adapter Enabled");
        if (!mBluetoothAdapter.isEnabled()) return;

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser != null) {
            Log.v(TAG, "Bluetooth Advertiser Enabled");
            mBluetoothLeAdvertiser.startAdvertising(ADVERTISE_SETTINGS, advertiseData, mAdvertiseCallback);
            mAdvertiserHandler = new Handler();
            mAdvertiserHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
                }
            }, INTERVAL);

        } else {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    "Seu dispositivo não suporta enviar dados em Bluetooth Low Energy!",
                    Snackbar.LENGTH_LONG).show();
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
                if(mScannerDialog != null) if(mScannerDialog.isShowing()) mScannerDialog.dismiss();
                String name = result.getDevice().getName();
                String mac = result.getDevice().getAddress();
                Beacon beacon = new Beacon();
                double rssi = result.getRssi();

                if(name == null || name.isEmpty()) name = "N/A";
                if(!name.contains("Tug")) return;
                if(mac == null || mac.isEmpty()) mac = "N/A";

                try{
                    String[] args1 = mac.split(":");
                    beacon.setId(hexStringToByteArray(args1[0]));

                    String[] args2 = name.split(" ");
                    beacon.setFullname(name);
                    beacon.setDeviceName(args2[0]);
                    beacon.setPower(args2[1] + "/" + args2[2]);
                    beacon.setMac(mac);
                    beacon.setRssi(String.valueOf(rssi));

                    Log.v("Beacon: ", beacon.getFullname());
                } catch (Exception e){
                    e.printStackTrace();
                }

                int pos = 0;
                for(int i=0; i<beacons.size(); i++){
                    if(beacons.get(i).getMac().equals(mac)){
                        beacons.remove(i);
                        pos = i;
                        beaconListAdapter.notifyDataSetInvalidated();
                        break;
                    }
                }
                beacons.add(pos, beacon);
                beaconListAdapter.notifyDataSetChanged();
            }

            mBluetoothLeScanner.stopScan(this);
            mBluetoothLeScanner.startScan(SCAN_FILTERS, SCAN_SETTINGS, this);
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed errorCode " + errorCode);
        }
    };

    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v("Advertiser Enabled", settingsInEffect.toString());
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            if(errorCode == 3) mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
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
                        enableBluetoothScanner();
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
            enableBluetoothScanner();
        } catch (SecurityException se) {
            Snackbar.make(findViewById(R.id.coordinatorLayout),
                    "Não é possível usar o aplicativo sem conceder permissões!",
                    Snackbar.LENGTH_LONG).show();
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
        if (resultCode == REQUEST_ENABLE_BT) enableBluetoothScanner();
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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
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
            new AlertDialog.Builder(this)
                    .setMessage(R.string.about)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
