/**
 * RGBW Control App - Main JavaScript
 * 
 * This script handles:
 * 1. Webcam access and device enumeration
 * 2. Color detection from webcam feed
 * 3. UI interactions (toggle, button)
 * 4. Communication with the backend
 */

// DOM Elements
const videoElement = document.getElementById('webcam');
const cameraSelect = document.getElementById('camera-select');
const colorBox = document.getElementById('color-box');
const redValue = document.getElementById('red-value');
const greenValue = document.getElementById('green-value');
const blueValue = document.getElementById('blue-value');
const autoModeToggle = document.getElementById('auto-mode');
const modeLabel = document.getElementById('mode-label');
const sendButton = document.getElementById('send-button');
const statusMessage = document.getElementById('status-message');
const loadingIndicator = document.querySelector('.loading-indicator');

// Device status elements
const discoveryStatus = document.getElementById('discovery-status');
const discoveryToggle = document.getElementById('discovery-toggle');
const bulbIp = document.getElementById('bulb-ip');
const bulbName = document.getElementById('bulb-name');

// App State
let currentColor = { red: 0, green: 0, blue: 0 };
let isAutoMode = false;
let autoUpdateInterval = null;
let deviceStatusInterval = null;
let colorThief = new ColorThief();
let stream = null;
let isDiscoveryRunning = false;

// Configuration
const AUTO_UPDATE_INTERVAL = 3000; // 3 seconds
const DEVICE_STATUS_INTERVAL = 5000; // 5 seconds
const API_ENDPOINT = '/color';
const DEVICE_API = {
    STATUS: '/api/devices/status',
    LIST: '/api/devices',
    START_DISCOVERY: '/api/devices/discovery/start',
    STOP_DISCOVERY: '/api/devices/discovery/stop'
};

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
});

/**
 * Initialize the application
 */
async function initializeApp() {
    try {
        // Set up event listeners
        setupEventListeners();

        // Start fetching device status
        fetchDeviceStatus();

        // Set up interval to periodically fetch device status
        deviceStatusInterval = setInterval(fetchDeviceStatus, DEVICE_STATUS_INTERVAL);

        // Check if getUserMedia is supported
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            throw new Error('Your browser does not support webcam access. Please try a modern browser like Chrome, Firefox, or Edge.');
        }

        // Enumerate available camera devices
        await enumerateDevices();

        // Start with the default camera
        await startCamera();

    } catch (error) {
        console.error('Error initializing app:', error);
        showStatus(`Error: ${error.message}`, 'error');
        handleWebcamError(error);
    }
}

/**
 * Fetch device status from the API
 */
async function fetchDeviceStatus() {
    try {
        const response = await fetch(DEVICE_API.STATUS);

        if (!response.ok) {
            throw new Error(`Failed to fetch device status: ${response.status}`);
        }

        const data = await response.json();

        // Update discovery status
        isDiscoveryRunning = data.discoveryRunning;
        updateDiscoveryStatus(isDiscoveryRunning);

        // Update current device info
        if (data.currentDevice) {
            bulbIp.textContent = data.currentDevice.ipAddress;
            bulbName.textContent = data.currentDevice.name;
        } else {
            bulbIp.textContent = 'Not connected';
            bulbName.textContent = 'Unknown';
        }

        console.log('Device status updated:', data);
    } catch (error) {
        console.error('Error fetching device status:', error);
        // Don't show error in UI to avoid distracting the user
    }
}

/**
 * Update the discovery status in the UI
 * @param {boolean} isRunning - Whether discovery is running
 */
function updateDiscoveryStatus(isRunning) {
    discoveryStatus.textContent = isRunning ? 'Running' : 'Stopped';
    discoveryStatus.className = isRunning ? 'active' : 'inactive';

    discoveryToggle.textContent = isRunning ? 'Stop' : 'Start';
    discoveryToggle.className = isRunning ? 'small-button stop' : 'small-button';
}

/**
 * Toggle device discovery
 */
async function toggleDiscovery() {
    try {
        const endpoint = isDiscoveryRunning ? DEVICE_API.STOP_DISCOVERY : DEVICE_API.START_DISCOVERY;

        const response = await fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to ${isDiscoveryRunning ? 'stop' : 'start'} discovery: ${response.status}`);
        }

        const data = await response.json();

        // Update discovery status
        isDiscoveryRunning = data.discoveryRunning;
        updateDiscoveryStatus(isDiscoveryRunning);

        showStatus(`Discovery ${isDiscoveryRunning ? 'started' : 'stopped'} successfully`, 'success');

        // Immediately fetch device status to get updated information
        fetchDeviceStatus();
    } catch (error) {
        console.error('Error toggling discovery:', error);
        showStatus(`Error: ${error.message}`, 'error');
    }
}

/**
 * Handle webcam access errors with graceful degradation
 * @param {Error} error - The error that occurred
 */
function handleWebcamError(error) {
    // Hide loading indicator
    loadingIndicator.style.display = 'none';

    // Show error message in video container
    const videoContainer = document.querySelector('.video-container');
    videoContainer.innerHTML = `
        <div class="error-message">
            <h3>Camera Access Error</h3>
            <p>${error.message}</p>
            <p>You can still use the app in manual mode by selecting colors below:</p>
            <div class="manual-color-inputs">
                <label>
                    Red: <input type="range" min="0" max="255" value="128" id="manual-red" />
                    <span id="manual-red-value">128</span>
                </label>
                <label>
                    Green: <input type="range" min="0" max="255" value="128" id="manual-green" />
                    <span id="manual-green-value">128</span>
                </label>
                <label>
                    Blue: <input type="range" min="0" max="255" value="128" id="manual-blue" />
                    <span id="manual-blue-value">128</span>
                </label>
            </div>
        </div>
    `;

    // Set up manual color selection
    setupManualColorControls();
}

/**
 * Set up event listeners for manual color controls
 */
function setupManualColorControls() {
    const redSlider = document.getElementById('manual-red');
    const greenSlider = document.getElementById('manual-green');
    const blueSlider = document.getElementById('manual-blue');

    const redValueDisplay = document.getElementById('manual-red-value');
    const greenValueDisplay = document.getElementById('manual-green-value');
    const blueValueDisplay = document.getElementById('manual-blue-value');

    if (redSlider && greenSlider && blueSlider) {
        // Update color when sliders change
        const updateColor = () => {
            currentColor = {
                red: parseInt(redSlider.value),
                green: parseInt(greenSlider.value),
                blue: parseInt(blueSlider.value)
            };

            // Update displays
            redValueDisplay.textContent = currentColor.red;
            greenValueDisplay.textContent = currentColor.green;
            blueValueDisplay.textContent = currentColor.blue;

            // Update color preview
            updateColorDisplay();
        };

        // Set initial color
        updateColor();

        // Add event listeners
        redSlider.addEventListener('input', updateColor);
        greenSlider.addEventListener('input', updateColor);
        blueSlider.addEventListener('input', updateColor);

        // Force manual mode
        if (autoModeToggle.checked) {
            autoModeToggle.checked = false;
            modeLabel.textContent = 'Manual Mode';
            sendButton.disabled = false;
            stopAutoUpdate();
        }
    }
}

/**
 * Set up event listeners for UI elements
 */
function setupEventListeners() {
    // Camera selection change
    cameraSelect.addEventListener('change', async () => {
        if (stream) {
            stopCamera();
        }
        await startCamera();
    });

    // Auto mode toggle
    autoModeToggle.addEventListener('change', () => {
        isAutoMode = autoModeToggle.checked;
        modeLabel.textContent = isAutoMode ? 'Auto Mode' : 'Manual Mode';
        sendButton.disabled = isAutoMode;

        if (isAutoMode) {
            startAutoUpdate();
        } else {
            stopAutoUpdate();
        }
    });

    // Send button click
    sendButton.addEventListener('click', () => {
        sendColorToBackend();
    });

    // Video element loaded metadata
    videoElement.addEventListener('loadedmetadata', () => {
        loadingIndicator.style.display = 'none';
    });

    // Discovery toggle button
    if (discoveryToggle) {
        discoveryToggle.addEventListener('click', () => {
            toggleDiscovery();
        });
    }
}

/**
 * Enumerate available camera devices and populate the dropdown
 */
async function enumerateDevices() {
    try {
        // Request temporary access to get device info
        const tempStream = await navigator.mediaDevices.getUserMedia({ video: true });
        tempStream.getTracks().forEach(track => track.stop());

        const devices = await navigator.mediaDevices.enumerateDevices();
        const videoDevices = devices.filter(device => device.kind === 'videoinput');

        // Clear the dropdown
        cameraSelect.innerHTML = '';

        // Add each video device to the dropdown
        videoDevices.forEach(device => {
            const option = document.createElement('option');
            option.value = device.deviceId;
            option.text = device.label || `Camera ${cameraSelect.length + 1}`;
            cameraSelect.appendChild(option);
        });

        if (videoDevices.length === 0) {
            showStatus('No camera devices found', 'error');
        }
    } catch (error) {
        console.error('Error enumerating devices:', error);
        showStatus('Could not access camera devices', 'error');
    }
}

/**
 * Start the camera with the selected device
 */
async function startCamera() {
    try {
        loadingIndicator.style.display = 'flex';

        const constraints = {
            video: {
                deviceId: cameraSelect.value ? { exact: cameraSelect.value } : undefined
            }
        };

        stream = await navigator.mediaDevices.getUserMedia(constraints);
        videoElement.srcObject = stream;

        // Start color detection when video is playing
        videoElement.onplaying = () => {
            // Start color detection after a short delay to ensure video is ready
            setTimeout(() => {
                detectColor();
            }, 500);
        };

    } catch (error) {
        console.error('Error starting camera:', error);
        showStatus(`Camera error: ${error.message}`, 'error');
        loadingIndicator.style.display = 'none';
    }
}

/**
 * Stop the camera stream
 */
function stopCamera() {
    if (stream) {
        stream.getTracks().forEach(track => track.stop());
        stream = null;
        videoElement.srcObject = null;
    }
}

/**
 * Detect the dominant color from the video feed
 */
function detectColor() {
    if (!videoElement.paused && !videoElement.ended) {
        try {
            // Create a canvas to capture the current video frame
            const canvas = document.createElement('canvas');
            const context = canvas.getContext('2d');

            // Set canvas dimensions to match video
            canvas.width = videoElement.videoWidth;
            canvas.height = videoElement.videoHeight;

            // Draw the current video frame to the canvas
            context.drawImage(videoElement, 0, 0, canvas.width, canvas.height);

            // Extract dominant color using Color Thief from the canvas
            // This is more reliable than extracting directly from the video element
            const dominantColor = colorThief.getColor(canvas);

            if (dominantColor && dominantColor.length === 3) {
                // Update current color
                currentColor = {
                    red: dominantColor[0],
                    green: dominantColor[1],
                    blue: dominantColor[2]
                };

                // Update UI
                updateColorDisplay();

                // If in auto mode, ensure we're sending updates
                if (isAutoMode && !autoUpdateInterval) {
                    startAutoUpdate();
                }

                // Log the detected color for debugging
                console.log('Detected color:', dominantColor);
            }

            // Schedule next detection - use setTimeout instead of requestAnimationFrame
            // for more consistent timing and to avoid overwhelming the browser
            setTimeout(detectColor, 100);
        } catch (error) {
            console.error('Error detecting color:', error);
            // Retry after a short delay
            setTimeout(detectColor, 500);
        }
    } else {
        // Retry if video is not playing
        setTimeout(detectColor, 500);
    }
}

/**
 * Update the color display with current color values
 */
function updateColorDisplay() {
    // Update color box background
    colorBox.style.backgroundColor = `rgb(${currentColor.red}, ${currentColor.green}, ${currentColor.blue})`;

    // Update RGB text values
    redValue.textContent = currentColor.red;
    greenValue.textContent = currentColor.green;
    blueValue.textContent = currentColor.blue;
}

/**
 * Start automatic color updates to the backend
 */
function startAutoUpdate() {
    if (autoUpdateInterval) {
        clearInterval(autoUpdateInterval);
    }

    // Send immediately
    sendColorToBackend();

    // Then set up interval
    autoUpdateInterval = setInterval(() => {
        // Log the current color being sent (for debugging)
        console.log('Auto-sending color:', currentColor);
        sendColorToBackend();
    }, AUTO_UPDATE_INTERVAL);

    console.log('Auto update started with interval:', AUTO_UPDATE_INTERVAL);
}

/**
 * Stop automatic color updates
 */
function stopAutoUpdate() {
    if (autoUpdateInterval) {
        clearInterval(autoUpdateInterval);
        autoUpdateInterval = null;
    }
}

/**
 * Send the current color to the backend
 */
async function sendColorToBackend() {
    // Validate that we have a valid color to send
    if (!currentColor || typeof currentColor.red !== 'number' || 
        typeof currentColor.green !== 'number' || 
        typeof currentColor.blue !== 'number') {
        console.error('Invalid color data:', currentColor);
        return;
    }

    // Check if we have a connected bulb
    if (bulbIp.textContent === 'Not connected') {
        showStatus('No bulb connected. Please start discovery or check your network.', 'error');
        return;
    }

    // Ensure values are integers within valid range
    const colorToSend = {
        red: Math.max(0, Math.min(255, Math.round(currentColor.red))),
        green: Math.max(0, Math.min(255, Math.round(currentColor.green))),
        blue: Math.max(0, Math.min(255, Math.round(currentColor.blue)))
    };

    console.log('Sending color to backend:', colorToSend);

    try {
        const response = await fetch(API_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(colorToSend)
        });

        const data = await response.json();

        if (response.ok) {
            showStatus(`Color sent to ${bulbName.textContent} (${bulbIp.textContent})!`, 'success');
            console.log('Color sent successfully:', colorToSend);
        } else {
            showStatus(`Error: ${data.message}`, 'error');
            console.error('Server error:', data.message);
        }
    } catch (error) {
        console.error('Error sending color:', error);
        showStatus('Failed to communicate with the server', 'error');
    }
}

/**
 * Show a status message
 * @param {string} message - The message to display
 * @param {string} type - The type of message ('success' or 'error')
 */
function showStatus(message, type = 'success') {
    statusMessage.textContent = message;
    statusMessage.className = 'status';
    statusMessage.classList.add(type);

    // Clear the message after 5 seconds
    setTimeout(() => {
        statusMessage.textContent = '';
        statusMessage.className = 'status';
    }, 5000);
}
