# RGBW Control App

A web application that uses your webcam to detect dominant color and control a Shelly Duo GU10 RGBW bulb over the local network.

## Features

- Webcam access and camera device selection
- Real-time color detection from webcam feed
- Manual or automatic color updates to the bulb
- Clean, modern UI optimized for demos

## Prerequisites

- Java 21 or higher
- Maven (or use the included Maven Wrapper)
- A modern web browser with webcam access (Chrome, Firefox, Edge)
- A Shelly Duo GU10 RGBW bulb connected to your local network

## Configuration

Before running the application, update the Shelly bulb IP address in the `application.properties` file:

```properties
# src/main/resources/application.properties
shelly.bulb.ip=192.168.1.100  # Replace with your bulb's IP address
```

## Running the Application

### Using Maven Wrapper

```bash
# On Unix/Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
```

### Using Maven

```bash
mvn spring-boot:run
```

## Accessing the Application

Once the application is running, open your web browser and navigate to:

```
http://localhost:8080
```

## Usage Instructions

1. Allow camera access when prompted by your browser
2. Select your preferred camera from the dropdown if you have multiple cameras
3. The app will display the dominant color detected from your camera feed
4. Toggle between Manual and Auto mode:
   - In Manual mode: Click "Send to Bulb" to update the bulb color
   - In Auto mode: The bulb color updates automatically every 3 seconds

## Troubleshooting

- **Camera not working**: Ensure your browser has permission to access your camera
- **Cannot connect to bulb**: Verify the bulb's IP address in the configuration and ensure it's powered on and connected to the same network
- **Color detection issues**: Ensure adequate lighting for better color detection

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.5
- **Frontend**: HTML5, CSS3, JavaScript
- **Color Detection**: Color Thief library
- **Communication**: REST API