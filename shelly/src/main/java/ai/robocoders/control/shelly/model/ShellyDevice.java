package ai.robocoders.control.shelly.model;

/**
 * Model class representing a discovered Shelly device.
 */
public class ShellyDevice {
    private String name;
    private String ipAddress;
    private String type;
    private boolean available;
    private long discoveryTime;

    public ShellyDevice() {
        this.discoveryTime = System.currentTimeMillis();
    }

    public ShellyDevice(String name, String ipAddress, String type) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.type = type;
        this.available = true;
        this.discoveryTime = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public long getDiscoveryTime() {
        return discoveryTime;
    }

    public void setDiscoveryTime(long discoveryTime) {
        this.discoveryTime = discoveryTime;
    }

    @Override
    public String toString() {
        return "ShellyDevice{" +
                "name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", type='" + type + '\'' +
                ", available=" + available +
                '}';
    }
}