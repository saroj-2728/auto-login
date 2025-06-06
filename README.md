# Pulchowk Login

A lightweight Android application that automatically logs in to Pulchowk Campus WiFi captive portal in the background.

## Features
- Automatically detects captive portal WiFi networks and logs in without user interaction
- Connects automatically in the background once configured
- Supports multiple credentials with primary/secondary priority
- Falls back to alternative credentials if primary login fails
- Starts automatically on device boot
- Low resource usage

## Installation

To install the application, follow these steps:

1. Download the APK from the releases section
2. Install the application on your Android device
3. Open the app and enter your login credentials
4. The service will start automatically in the background

Alternatively, build from source:

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/saroj-2728/auto-login.git
   ```
2. Open the project in Android Studio
3. Build and run the application on your Android device

## Usage

1. Open the app and add your login credentials
2. The app starts a background service that monitors network connections
3. Whenever your device connects to a WiFi network requiring login, the app automatically attempts to log in
4. Upon successful login, a Toast message will indicate the login status
5. If login fails, the app will try alternative credentials if configured
6. The app automatically starts when you reboot your device

## Troubleshooting

If automatic login isn't working:
1. Ensure the app has required permissions (WiFi, Network, Background)
2. Disable battery optimization for the app
3. Check that credentials are entered correctly
4. Restart the phone if service doesn't start

## Credits

This project is an enhanced and refactored version of the original work [Pulchowk Login](https://github.com/sujanbimali9/pulchowk_login) by [Sujan Bimali](https://github.com/sujanbimali9). Huge credit and appreciation to them for creating the foundation that made this implementation possible. This project builds upon the original concept and core functionality designed by them.

## Enhancements Over Original Version
This fork enhances the original project with several key improvements:

- **Multiple Credential Management**: Added support for storing and using multiple login credentials
- **Priority-based Login System**: Set primary credentials with automatic fallback to secondary credentials
- **Smart Captive Portal Detection**: Only attempts login when actually needed (no wasted login attempts)
- **Improved User Interface**: Redesigned for better user experience and easier credential management
- **Enhanced Background Service**: More efficient service that uses less battery and system resources