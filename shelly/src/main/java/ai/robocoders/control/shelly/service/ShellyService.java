package ai.robocoders.control.shelly.service;

import ai.robocoders.control.shelly.model.ColorData;
import ai.robocoders.control.shelly.model.ShellyDevice;

import java.util.List;

/**
 * Service interface for interacting with the Shelly bulb.
 */
public interface ShellyService {

    /**
     * Sets the color of the Shelly bulb.
     *
     * @param colorData The RGB color data to set
     * @throws Exception if there's an error communicating with the bulb
     */
    void setColor(ColorData colorData) throws Exception;

    /**
     * Starts the mDNS discovery service to find Shelly devices on the network.
     */
    void startDiscovery();

    /**
     * Stops the mDNS discovery service.
     */
    void stopDiscovery();

    /**
     * Gets the list of discovered Shelly devices.
     *
     * @return List of discovered Shelly devices
     */
    List<ShellyDevice> getDiscoveredDevices();

    /**
     * Gets the currently selected Shelly device.
     *
     * @return The currently selected Shelly device, or null if none is selected
     */
    ShellyDevice getCurrentDevice();

    /**
     * Checks if the discovery service is currently running.
     *
     * @return true if discovery is running, false otherwise
     */
    boolean isDiscoveryRunning();
}
