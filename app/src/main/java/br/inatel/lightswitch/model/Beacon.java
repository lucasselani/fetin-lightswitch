package br.inatel.lightswitch.model;

/**
 * Created by Lucas on 24/04/2016.
 */
public class Beacon {
    private String mac;
    private String devicename;
    private String rssi;
    private String distance;

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }

    public String getDeviceName() { return devicename; }
    public void setDeviceName(String devicename) { this.devicename = devicename; }

    public String getDistance() {
        return distance;
    }
    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getRssi() { return rssi; }
    public void setRssi(String rssi) { this.rssi = rssi; }

    public double calculateAccuracy(double txPower, double rssi) {
        if (rssi == 0) return -1.0;
        return Math.pow(10, ((rssi-txPower)/-42.119));
    }
}
