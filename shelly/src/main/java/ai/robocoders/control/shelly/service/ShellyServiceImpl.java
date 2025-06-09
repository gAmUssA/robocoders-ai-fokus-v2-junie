package ai.robocoders.control.shelly.service;

import ai.robocoders.control.shelly.model.ColorData;
import ai.robocoders.control.shelly.model.ShellyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementation of the ShellyService interface for controlling a Shelly Duo GU10 RGBW bulb.
 */
@Service
public class ShellyServiceImpl implements ShellyService {

    private static final Logger logger = LoggerFactory.getLogger(ShellyServiceImpl.class);
    private static final int GAIN = 100; // Full brightness
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String SHELLY_SERVICE_TYPE = "_http._tcp.local.";

    @Value("${shelly.bulb.endpoint}")
    private String shellyEndpoint;

    // Fallback IP from properties if discovery fails
    @Value("${shelly.bulb.ip:}")
    private String fallbackIp;

    // Auto-start discovery on initialization
    @Value("${shelly.discovery.auto-start:true}")
    private boolean autoStartDiscovery;

    // Using a thread-safe list for discovered devices
    private final List<ShellyDevice> discoveredDevices = new CopyOnWriteArrayList<>();
    private ShellyDevice currentDevice;
    private JmDNS jmdns;
    private boolean discoveryRunning = false;

    private final RestClient restClient;

    public ShellyServiceImpl() {
        this.restClient = RestClient.builder()
                .build();
    }

    /**
     * Initialize the service after properties are set.
     * Auto-starts discovery if configured.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("Initializing ShellyService");

        // If we have a fallback IP, create a device for it
        if (fallbackIp != null && !fallbackIp.isEmpty()) {
            ShellyDevice fallbackDevice = new ShellyDevice("Shelly (Fallback)", fallbackIp, "Unknown");
            discoveredDevices.add(fallbackDevice);
            currentDevice = fallbackDevice;
            logger.info("Added fallback device: {}", fallbackDevice);
        }

        // Auto-start discovery if configured
        if (autoStartDiscovery) {
            logger.info("Auto-starting mDNS discovery");
            startDiscovery();
        }
    }

    @Override
    public void startDiscovery() {
        if (discoveryRunning) {
            logger.info("Discovery already running");
            return;
        }

        try {
            logger.info("Starting mDNS discovery for Shelly devices");

            // Find a suitable network interface (not localhost)
            InetAddress addr = findSuitableInterface();
            if (addr == null) {
                logger.warn("No suitable network interface found for mDNS discovery");
                return;
            }

            logger.info("Using network interface: {}", addr.getHostAddress());

            // Create JmDNS instance
            jmdns = JmDNS.create(addr);

            // Add a service listener
            jmdns.addServiceListener(SHELLY_SERVICE_TYPE, new ShellyServiceListener());

            discoveryRunning = true;
            logger.info("mDNS discovery started");

            // If we have a fallback IP, create a device for it
            if (fallbackIp != null && !fallbackIp.isEmpty()) {
                ShellyDevice fallbackDevice = new ShellyDevice("Shelly (Fallback)", fallbackIp, "Unknown");
                if (!discoveredDevices.contains(fallbackDevice)) {
                    discoveredDevices.add(fallbackDevice);
                    if (currentDevice == null) {
                        currentDevice = fallbackDevice;
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Error starting mDNS discovery: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stopDiscovery() {
        if (!discoveryRunning || jmdns == null) {
            return;
        }

        try {
            logger.info("Stopping mDNS discovery");
            jmdns.close();
            discoveryRunning = false;
            logger.info("mDNS discovery stopped");
        } catch (IOException e) {
            logger.error("Error stopping mDNS discovery: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<ShellyDevice> getDiscoveredDevices() {
        return Collections.unmodifiableList(discoveredDevices);
    }

    @Override
    public ShellyDevice getCurrentDevice() {
        return currentDevice;
    }

    @Override
    public boolean isDiscoveryRunning() {
        return discoveryRunning;
    }

    @Override
    public void setColor(ColorData colorData) throws Exception {
        logger.info("Setting color: R={}, G={}, B={}", 
                colorData.getRed(), colorData.getGreen(), colorData.getBlue());

        // Validate color values
        validateColorValues(colorData);

        // Check if we have a current device
        if (currentDevice == null) {
            throw new Exception("No Shelly device available. Please start discovery or check your network connection.");
        }

        // Calculate white value as min(R,G,B)
        int white = colorData.calculateWhite();
        logger.info("Calculated white value: {}", white);

        // Construct the URL with query parameters
        String url = String.format("http://%s%s?turn=on&red=%d&green=%d&blue=%d&white=%d&gain=%d",
                currentDevice.getIpAddress(),
                shellyEndpoint,
                colorData.getRed(),
                colorData.getGreen(),
                colorData.getBlue(),
                white,
                GAIN);

        logger.info("Sending request to Shelly bulb at IP: {}", currentDevice.getIpAddress());
        logger.debug("Full request URL: {}", url);

        try {
            long startTime = System.currentTimeMillis();

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            long endTime = System.currentTimeMillis();
            logger.info("Shelly bulb response received in {} ms", (endTime - startTime));
            logger.debug("Response from Shelly: {}", response);
        } catch (Exception e) {
            logger.error("Error communicating with Shelly bulb at {}: {}", currentDevice.getIpAddress(), e.getMessage());
            logger.debug("Detailed error:", e);
            throw new Exception("Failed to communicate with Shelly bulb: " + e.getMessage(), e);
        }
    }

    /**
     * Validates that the RGB values are within the valid range (0-255).
     *
     * @param colorData The color data to validate
     * @throws IllegalArgumentException if any value is outside the valid range
     */
    private void validateColorValues(ColorData colorData) {
        if (colorData.getRed() < 0 || colorData.getRed() > 255) {
            throw new IllegalArgumentException("Red value must be between 0 and 255");
        }
        if (colorData.getGreen() < 0 || colorData.getGreen() > 255) {
            throw new IllegalArgumentException("Green value must be between 0 and 255");
        }
        if (colorData.getBlue() < 0 || colorData.getBlue() > 255) {
            throw new IllegalArgumentException("Blue value must be between 0 and 255");
        }
    }

    /**
     * Finds a suitable network interface for mDNS discovery.
     * Avoids localhost interfaces.
     *
     * @return A suitable InetAddress or null if none found
     */
    private InetAddress findSuitableInterface() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // Skip loopback, inactive, or virtual interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    // Skip loopback addresses and IPv6 addresses
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') == -1) {
                        logger.info("Found suitable interface: {} - {}", networkInterface.getDisplayName(), address.getHostAddress());
                        return address;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error finding network interfaces: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * Service listener for Shelly devices.
     */
    private class ShellyServiceListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            logger.info("Service added: {}", event.getName());
            // Request service info - this will trigger serviceResolved
            jmdns.requestServiceInfo(event.getType(), event.getName());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            logger.info("Service removed: {}", event.getName());

            // Find and remove the device from our list
            String name = event.getName();
            discoveredDevices.removeIf(device -> device.getName().equals(name));

            // If the current device was removed, select another one if available
            if (currentDevice != null && currentDevice.getName().equals(name)) {
                currentDevice = discoveredDevices.isEmpty() ? null : discoveredDevices.get(0);
            }
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            ServiceInfo info = event.getInfo();
            String name = event.getName();

            // Check if this is a Shelly device
            if (name.toLowerCase().contains("shelly")) {
                InetAddress[] addresses = info.getInetAddresses();
                if (addresses.length > 0) {
                    String ipAddress = addresses[0].getHostAddress();
                    String type = info.getPropertyString("md") != null ? 
                            info.getPropertyString("md") : "Unknown";

                    logger.info("Discovered Shelly device: {} at {}", name, ipAddress);

                    // Create a new device and add it to our list
                    ShellyDevice device = new ShellyDevice(name, ipAddress, type);

                    // Check if we already have this device
                    boolean exists = false;
                    for (ShellyDevice existingDevice : discoveredDevices) {
                        if (existingDevice.getIpAddress().equals(ipAddress)) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        discoveredDevices.add(device);
                        logger.info("Added new Shelly device: {}", device);

                        // If this is our first device, make it the current one
                        if (currentDevice == null) {
                            currentDevice = device;
                            logger.info("Set current device to: {}", device);
                        }
                    }
                }
            }
        }
    }
}
