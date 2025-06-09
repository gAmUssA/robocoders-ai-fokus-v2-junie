package ai.robocoders.control.shelly.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for health check endpoint.
 */
@RestController
public class HealthController {

    /**
     * Health check endpoint that confirms the service is running.
     *
     * @return A message indicating the service is running
     */
    @GetMapping("/health")
    public String healthCheck() {
        return "RGBW Control Service is running";
    }
}