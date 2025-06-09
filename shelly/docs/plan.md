# Implementation Plan for RGBW Control App

## 1. Project Overview

### Project Goal
The primary goal is to build a web application that:
- Uses a webcam to detect dominant colors in real-time
- Controls a Shelly Duo GU10 RGBW smart bulb over a local network
- Provides a simple, intuitive interface for a live demo environment

### Key Principles
- **Simplicity First**: The project intentionally prioritizes simplicity for a live demo
- **Local Operation**: The entire application runs locally with no cloud dependencies
- **Demo-Ready UI**: Interface optimized for large screen presentations with clear, visible components

## 2. Backend Implementation

### Technology Stack
- Java 21
- Spring Boot 3.5
- Maven + Maven Wrapper for build management

### Core Components

#### Configuration
- Application properties file to store:
  - Shelly bulb IP address
  - Server port configuration
  - Logging settings

#### REST API
- Endpoint: `POST /color`
  - Accepts RGB data from frontend
  - Validates input values (0-255 range)
  - Returns appropriate status codes and error messages

#### Shelly Integration Service
- Uses Spring's `RestClient` to communicate with the bulb
- Calls `http://<bulb-ip>/light/0` with appropriate parameters
- Implements error handling with try/catch blocks and timeouts
- Calculates white value as `min(R,G,B)` to reduce color saturation

#### Error Handling
- Implement basic error handling for network issues
- Provide meaningful error responses to the frontend
- Set appropriate timeouts for bulb communication

### Implementation Rationale
- Spring Boot provides a lightweight, production-ready framework
- `RestClient` (new in Spring 6) offers a modern, concise HTTP client API
- Basic error handling is sufficient for demo purposes without overcomplicating the system

## 3. Frontend Implementation

### Technology Stack
- HTML5
- JavaScript (vanilla)
- CSS for styling

### Core Components

#### Camera Interface
- Use `getUserMedia` API for webcam access
- Implement `enumerateDevices()` to populate camera selection dropdown
- Create video preview element to display camera feed

#### Color Detection
- Implement simple color extraction using Color Thief library
- Display detected dominant color in a preview box
- Calculate RGB values for transmission to backend

#### User Interface
- Create full-screen layout optimized for demos with:
  - Large video preview
  - Prominent color preview box
  - Camera selection dropdown
  - Manual/Auto toggle switch
  - "Send to Bulb" button (enabled only in manual mode)
- Implement clean, modern UI with clear visual feedback

#### Color Transmission Logic
- In Auto mode: Send color to backend every 3 seconds
- In Manual mode: Send color only when "Send to Bulb" button is clicked
- Implement basic error handling for API calls

### Implementation Rationale
- Vanilla JavaScript keeps the frontend simple and dependency-free
- Color Thief provides efficient color extraction without complex algorithms
- The toggle between manual and automatic updates gives demo flexibility

## 4. Shelly Integration

### API Usage
- Use the Shelly REST API at `/light/0`
- Implement the following query parameters:
  - `turn=on` to ensure the bulb is active
  - `red`, `green`, `blue` values (0-255) from color detection
  - `white` calculated as min(R,G,B)
  - `gain=100` for full brightness

### Implementation Approach
- Backend will handle all communication with the Shelly device
- Format URL as: `http://<bulb-ip>/light/0?turn=on&red=X&green=Y&blue=Z&white=W&gain=100`
- Implement proper error handling for network issues or device unavailability

### Implementation Rationale
- Direct REST API calls provide the simplest integration method
- Calculating white as min(R,G,B) reduces color saturation for better visual results
- Keeping the integration in the backend isolates the frontend from device-specific details

## 5. Testing Strategy

### Manual Testing
- Test webcam access and color detection across different browsers
- Verify color accuracy between preview and bulb output
- Test toggle functionality between manual and automatic modes
- Verify error handling with network disconnection scenarios

### Implementation Rationale
- For a demo application, manual testing is sufficient
- Focus testing on the user flow that will be demonstrated

## 6. Implementation Timeline

### Phase 1: Project Setup
- Initialize Spring Boot project with Maven
- Configure application properties
- Create basic project structure

### Phase 2: Backend Development
- Implement REST endpoint for color data
- Create Shelly integration service
- Implement error handling

### Phase 3: Frontend Development
- Create HTML structure and CSS styling
- Implement webcam access and device selection
- Add color detection functionality
- Build UI components (toggle, button, previews)

### Phase 4: Integration and Testing
- Connect frontend to backend
- Test end-to-end functionality
- Optimize for demo flow
- Final adjustments and bug fixes

### Implementation Rationale
- Phased approach allows for incremental testing
- Backend and frontend can be developed in parallel after initial setup
- Final phase focuses on integration and demo preparation