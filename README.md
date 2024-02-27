# Pulchowk Login

**Pulchowk Login** is an Android application thatlogging in to Pulchowk WiFi using a username and password in the background reducing the hassle of logging it manually everytime

## Features
- auto login in background when network changes.
- Secure login using HTTPS.
- Toast messages for displaying login status.

## Note
- continuous switching of network might reach your login limit.

## Installation

To install the application, follow these steps:

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/sujanbimali9/PulchowkLogin.git
   ```

2. Open the project in Android Studio.

3. Build and run the application on your Android device.

4. login with your credentials for the first time then hide the app from laucher.

## Usage


1. whenever the network of device switches, app login the user if background.
2. Upon successful login, a Toast message will indicate the login status.
3. If the login fails, an appropriate error message will be displayed.

## Configuration

- Modify the icon displayed on the Quick Settings Tile by replacing the `icon.png` file in the `res/drawable` directory.
- Modify the title displayed on the Quick Settings Tile by replacing the `android:label="login"` in the `AndroidManifest.xml` inside the service tag.

## Dependencies

- AndroidX: Android Jetpack components.
- Kotlin Coroutines: For asynchronous programming.
- Gson: For JSON serialization and deserialization (if required).
- Other dependencies may be added as needed.

## Contributing

Contributions are welcome! If you find any bugs or have suggestions for improvements, please open an issue or create a pull request.
