package ai.robocoders.control.shelly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Shelly RGBW Control App.
 * This Spring Boot application controls a Shelly Duo GU10 RGBW smart bulb
 * based on color data received from a webcam.
 */
@SpringBootApplication
public class ShellyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShellyApplication.class, args);
    }
}