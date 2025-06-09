package ai.robocoders.control.shelly.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Controller to handle redirects for frontend resources.
 */
@Controller
public class RedirectController {

    /**
     * Redirects root path to index.html.
     *
     * @return Redirect to index.html
     */
    @GetMapping("/")
    public String redirectToIndex() {
        return "redirect:/index.html";
    }

    /**
     * Serves app.js content directly instead of redirecting.
     * This is needed for the test script which expects a 200 response.
     *
     * @return The JavaScript file content
     * @throws IOException if the file cannot be read
     */
    @GetMapping(value = "/app.js", produces = "application/javascript")
    @ResponseBody
    public ResponseEntity<String> serveAppJs() throws IOException {
        Resource resource = new ClassPathResource("static/js/app.js");
        String content = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        return ResponseEntity.ok(content);
    }
}
