package ai.robocoders.control.shelly.controller;

import ai.robocoders.control.shelly.model.ColorData;
import ai.robocoders.control.shelly.service.ShellyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling color data and controlling the Shelly bulb.
 */
@RestController
@CrossOrigin(origins = "*") // Allow requests from any origin for demo purposes
public class ColorController {

    private static final Logger logger = LoggerFactory.getLogger(ColorController.class);

    private final ShellyService shellyService;

    @Autowired
    public ColorController(ShellyService shellyService) {
        this.shellyService = shellyService;
    }

    /**
     * Endpoint to receive color data from the frontend and send it to the Shelly bulb.
     *
     * @param colorData The RGB color data from the frontend
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/color")
    public ResponseEntity<Map<String, Object>> setColor(@RequestBody ColorData colorData) {
        logger.info("Received color data: {}", colorData);

        Map<String, Object> response = new HashMap<>();

        try {
            shellyService.setColor(colorData);

            logger.info("Color set successfully: R={}, G={}, B={}", 
                    colorData.getRed(), colorData.getGreen(), colorData.getBlue());

            response.put("success", true);
            response.put("status", "success");
            response.put("message", "Color set successfully");
            response.put("data", colorData);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid color data: {}", e.getMessage());

            response.put("success", false);
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error setting color: {}", e.getMessage(), e);

            response.put("success", false);
            response.put("status", "error");
            response.put("message", "Error setting color: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Exception handler for connection exceptions.
     *
     * @param e The connection exception
     * @return ResponseEntity with error details
     */
    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<Map<String, Object>> handleConnectionException(ConnectException e) {
        logger.error("Connection error: {}", e.getMessage(), e);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", "error");
        response.put("message", "Could not connect to the Shelly bulb. Please check if it's powered on and connected to the network.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
