package ai.robocoders.control.shelly.controller;

import ai.robocoders.control.shelly.model.ShellyDevice;
import ai.robocoders.control.shelly.service.ShellyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Shelly device discovery and management.
 */
@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "*") // Allow requests from any origin for demo purposes
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    private final ShellyService shellyService;

    @Autowired
    public DeviceController(ShellyService shellyService) {
        this.shellyService = shellyService;
    }

    /**
     * Get the status of device discovery and the current device.
     *
     * @return ResponseEntity with discovery status and current device information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // Get discovery status
        boolean isDiscoveryRunning = shellyService.isDiscoveryRunning();
        response.put("discoveryRunning", isDiscoveryRunning);
        
        // Get current device
        ShellyDevice currentDevice = shellyService.getCurrentDevice();
        if (currentDevice != null) {
            Map<String, Object> deviceInfo = new HashMap<>();
            deviceInfo.put("name", currentDevice.getName());
            deviceInfo.put("ipAddress", currentDevice.getIpAddress());
            deviceInfo.put("type", currentDevice.getType());
            deviceInfo.put("available", currentDevice.isAvailable());
            response.put("currentDevice", deviceInfo);
        } else {
            response.put("currentDevice", null);
        }
        
        // Get discovered devices count
        List<ShellyDevice> devices = shellyService.getDiscoveredDevices();
        response.put("discoveredDevicesCount", devices.size());
        
        logger.info("Device status requested: discoveryRunning={}, currentDevice={}, deviceCount={}", 
                isDiscoveryRunning, 
                currentDevice != null ? currentDevice.getName() : "none", 
                devices.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get the list of all discovered devices.
     *
     * @return ResponseEntity with the list of discovered devices
     */
    @GetMapping
    public ResponseEntity<List<ShellyDevice>> getDevices() {
        List<ShellyDevice> devices = shellyService.getDiscoveredDevices();
        logger.info("Device list requested, returning {} devices", devices.size());
        return ResponseEntity.ok(devices);
    }

    /**
     * Start device discovery.
     *
     * @return ResponseEntity with success message
     */
    @PostMapping("/discovery/start")
    public ResponseEntity<Map<String, Object>> startDiscovery() {
        logger.info("Starting device discovery");
        shellyService.startDiscovery();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Device discovery started");
        response.put("discoveryRunning", shellyService.isDiscoveryRunning());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Stop device discovery.
     *
     * @return ResponseEntity with success message
     */
    @PostMapping("/discovery/stop")
    public ResponseEntity<Map<String, Object>> stopDiscovery() {
        logger.info("Stopping device discovery");
        shellyService.stopDiscovery();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Device discovery stopped");
        response.put("discoveryRunning", shellyService.isDiscoveryRunning());
        
        return ResponseEntity.ok(response);
    }
}