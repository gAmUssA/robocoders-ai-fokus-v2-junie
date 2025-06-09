# Actionable Improvement Tasks

This document contains a prioritized checklist of tasks for implementing the RGBW Control App. Tasks are organized by area and should be completed in the order presented.

## Project Setup

1. [x] Initialize Spring Boot 3.5 project with Java 21 configuration
2. [x] Configure Maven build with Maven Wrapper
3. [x] Create basic project structure (backend and frontend directories)
4. [x] Set up application.properties with configurable bulb IP

## Backend Implementation

5. [x] Create REST controller with POST /color endpoint
6. [x] Implement color data validation (0-255 range for RGB values)
7. [x] Create Shelly integration service with RestClient
8. [x] Implement white value calculation as min(R,G,B)
9. [x] Add error handling for network issues and timeouts
10. [x] Configure appropriate response status codes and messages

## Frontend Implementation

11. [x] Create HTML structure with video element and controls
12. [x] Implement webcam access using getUserMedia API
13. [x] Add camera device enumeration and selection dropdown
14. [x] Integrate Color Thief for dominant color extraction
15. [x] Create color preview box with RGB value display
16. [x] Implement Manual/Auto toggle switch
17. [x] Add "Send to Bulb" button (enabled only in manual mode)
18. [x] Create full-screen layout optimized for demo presentation
19. [x] Style UI with clean, modern CSS
20. [x] Implement color transmission logic (3-second interval in Auto mode)

## Integration and Testing

21. [x] Connect frontend to backend API
22. [ ] Test webcam access across different browsers
23. [ ] Verify color detection accuracy
24. [ ] Test toggle functionality between manual and automatic modes
25. [ ] Verify error handling with network disconnection scenarios
26. [ ] Test end-to-end functionality with actual Shelly bulb
27. [ ] Optimize application for demo flow
28. [x] Create simple documentation for running the application

## Performance and Usability Improvements

29. [ ] Optimize color extraction for smoother performance
30. [x] Add visual feedback for successful/failed bulb communication
31. [x] Implement graceful degradation for browsers without webcam support
32. [x] Add basic logging for troubleshooting during demo
33. [x] Ensure responsive design works on presenter's device

## Optional Enhancements (If Time Permits)

34. [ ] Add color history feature to recall previous colors
35. [ ] Implement simple color effects (pulse, fade, etc.)
36. [ ] Add bulb connection status indicator
37. [ ] Create simple onboarding/help overlay for first-time users
38. [ ] Add keyboard shortcuts for common actions
