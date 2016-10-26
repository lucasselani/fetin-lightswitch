package br.inatel.lightswitch.adapter;

/**
 * Created by lucasselani on 25/10/2016.
 */
//https://developer.android.com/training/swipe/add-swipe-interface.html#AddRefreshAction
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import br.inatel.lightswitch.R;
import br.inatel.lightswitch.model.Beacon;

/**
 * Created by Lucas on 16/05/2016.
 */
public class BeaconListAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener{
    private ArrayList<Beacon> beaconList;
    private Context context;
    private LayoutInflater layoutInflater;
    private OnCoumpoundButtonClicked mCallback;

    public BeaconListAdapter(ArrayList<Beacon> beaconList, Context context){
        this.beaconList = beaconList;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return beaconList.size();
    }

    @Override
    public Object getItem(int position) {
        return beaconList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        BeaconViewHolder viewHolder = null;

        if(view == null){
            view = layoutInflater.inflate(R.layout.beacon_item, parent, false);
            viewHolder = new BeaconViewHolder();

            viewHolder.name = (TextView) view.findViewById(R.id.beacon_name);
            viewHolder.mac = (TextView) view.findViewById(R.id.beacon_mac);
            viewHolder.power = (TextView) view.findViewById(R.id.beacon_eletric);
            viewHolder.rssi = (TextView) view.findViewById(R.id.beacon_rssi);
            viewHolder.lampSwitch = (SwitchCompat) view.findViewById(R.id.lampSwitch);
            viewHolder.simSwitch = (SwitchCompat) view.findViewById(R.id.simSwitch);
            viewHolder.sensSwitch = (SwitchCompat) view.findViewById(R.id.sensSwitch);

            view.setTag(viewHolder);
        }
        else{
            viewHolder = (BeaconViewHolder) view.getTag();
        }

        Beacon beacon = beaconList.get(position);
        viewHolder.name.setText(beacon.getDeviceName());
        viewHolder.mac.setText(beacon.getMac());
        //viewHolder.power.setText(beacon.getVoltage()+"/"+beacon.getCurrent());
        viewHolder.power.setText(beacon.getPower());
        viewHolder.rssi.setText(beacon.getRssi()+" dBm");
        viewHolder.lampSwitch.setOnCheckedChangeListener(this);
        viewHolder.simSwitch.setOnCheckedChangeListener(this);
        viewHolder.sensSwitch.setOnCheckedChangeListener(this);
        return view;
    }

    private static class BeaconViewHolder {
        private TextView name;
        private TextView mac;
        private TextView power;
        private TextView rssi;
        private CompoundButton lampSwitch;
        private CompoundButton simSwitch;
        private CompoundButton sensSwitch;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCallback.onSwitchCheckedChanged(buttonView, isChecked);
    }

    public void setOnCoumpoundButtonClicked(OnCoumpoundButtonClicked mCallback) {
        this.mCallback = mCallback;
    }

    public interface OnCoumpoundButtonClicked {
        void onSwitchCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }
}