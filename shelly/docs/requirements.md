# Vibecoding Demo: RGBW Control App Requirements

This specification outlines a practical and impressive application for a 1-hour Vibecoding conference demo using agentic AI IDEs. The app controls a Shelly Duo GU10 RGBW smart bulb based on real-time color extraction from a webcam feed.

> ✅ This project **intentionally prioritizes simplicity** for a live demo. All complex infrastructure (cloud, databases, Docker, etc.) is excluded by design. mDNS-based discovery and camera selection are lightweight usability enhancements.

---

## Project Goal

Build a **web app that uses your webcam to detect dominant color and control a Shelly Duo GU10 RGBW bulb** over the local network.

* Dropdown for selecting the active webcam device
* Toggle between **manual or automatic color updates**
* Entire app runs locally with no cloud dependency

---

## 🔧 Backend (Spring Boot 3.5, Java 21)

**Include:**

* Java 21 + Spring Boot 3.5 app
* Build with Maven + Maven Wrapper
* REST endpoint: `POST /color` accepts RGB data from frontend
* Use `RestClient` to call `http://<bulb-ip>/light/0`
* Basic error handling (try/catch, timeouts)
* IP of the bulb is provider via application properties

**Skip:**

* Circuit breakers
* Connection pooling configuration
* WebSocket
* Health checks / Actuator / Prometheus
* Redis, PostgreSQL, Docker, MQTT

---

## Frontend (HTML + JS)

**Include:**

* `getUserMedia` for webcam access
* `enumerateDevices()` for selecting camera input
* Simple color extraction (e.g. Color Thief)
* Show color preview box
* Toggle switch: Manual / Auto
* Button: “Send to Bulb” (only enabled in manual mode)
* If Auto mode is enabled, send color every 3 seconds
* **UI optimized for large screen demos:**
- full-screen layout with large, clearly visible components (preview box, toggle, and buttons)
- clean, modern, slick UI

**Skip:**

* WebSockets
* K-means, OffscreenCanvas, Web Workers
* HSL, Kelvin, or complex color math
* Camera constraint configuration

---

## 💡 Shelly Integration

* Use Shelly REST API at `/light/0`
* Supported query parameters:

    * `turn=on`
    * `red`, `green`, `blue`, `white` (0..255)
    * `gain=100`
* Example URL format:

  ```
  http://192.168.1.42/light/0?turn=on&red=120&green=80&blue=200&white=40&gain=100
  ```
* Calculate white as `min(R,G,B)` to reduce color saturation
* Skip `/white/0`, `/color/0`, and advanced features

---

## Live Demo Flow

1. User opens the web app → webcam activates
2. Bulb is discovered automatically
3. User selects a camera device from dropdown
4. Color is shown live on screen
5. User toggles between auto and manual mode
6. Bulb updates based on detected color