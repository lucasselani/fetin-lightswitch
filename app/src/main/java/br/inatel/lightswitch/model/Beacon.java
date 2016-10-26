package br.inatel.lightswitch.model;

/**
 * Created by Lucas on 24/04/2016.
 */
public class Beacon {
    private String fullname;
    private String mac;
    private String devicename;
    private String current;
    private String voltage;
    private String rssi;
    private String power;
    private byte[] id;

    public byte[] getId() { return id; }
    public void setId(byte[] id) { this.id = id; }

    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getMac() { return mac; }
    public void setMac(String mac) { this.mac = mac; }

    public String getDeviceName() { return devicename; }
    public void setDeviceName(String devicename) { this.devicename = devicename; }

    public String getCurrent() { return current; }
    public void setCurrent(String current) { this.current = current; }

    public String getVoltage() { return voltage; }
    public void setVoltage(String voltage) { this.voltage = voltage; }

    public String getRssi() { return rssi; }
    public void setRssi(String rssi) { this.rssi = rssi; }
}
