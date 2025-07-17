# React Native Bitchat Example

This is an example application for the `react-native-bitchat` module, built using Expo to demonstrate its integration and functionality. This example leverages Expo's advantages, such as easier setup and cross-platform development, and is designed to work with the parent `react-native-bitchat` module.

## Getting Started

Follow these instructions to set up and run the example application on your local machine.

### Prerequisites

- Node.js (recommended version: 18.x or 20.x, check `.nvmrc` in the root directory)
- Yarn (version 3.6.1, as per the root `package.json`)
- Expo CLI (install globally with <code>npm install -g expo-cli</code> or use npx)
- Ruby 3.4.5 (installed on macOS)
- JDK 17 (configured for Android development)
- Xcode 15.1+ (for iOS development)
- Android SDK (configured with <code>sdk.dir</code> in `local.properties`)

### Installation

1. **Create the Example Project**:
   - In the root folder of the `react-native-bitchat` module, run the following command to create a new Expo project:
     <code>sudo npx create-expo-app BitchatExample --template blank@sdk-49</code>
   - Note: The SDK version (e.g., 49) should be compatible with React Native 0.72.10 (as per the root `package.json`). Adjust the version (e.g., `--template blank@sdk-50`) if needed based on compatibility.

2. **Rename and Adjust Permissions**:
   - Rename the created `BitchatExample` folder to `example`:
     <code>mv BitchatExample example</code>
   - Grant full permissions to the root folder and all subfolders/files to avoid permission issues:
     <code>sudo chmod -R 777 /path/to/react-native-bitchat</code>
   - Note: Use the actual path to your `react-native-bitchat` root folder.

3. **Configure `package.json`**:
   - Open `example/package.json` and update the `"name"` field to:
     <code>"name": "react-native-bitchat-example"</code>
   - If a `package-lock.json` exists, update the `"name"` field there to match.
   - Add the local dependency to the parent module under `"dependencies"`:
     <code>"react-native-bitchat": "file:.."</code>

4. **Install Dependencies**:
   - Navigate to the `example` folder and install dependencies:
     <code>cd example && yarn install</code>

### Running the Application

#### iOS Setup

1. **Run the iOS Build**:
   - From the `example` folder, execute:
     <code>sudo npx expo run:ios</code>
   - When prompted with "What would you like your iOS bundle identifier to be?", enter a value like <code>com.bitchat.example</code>.

2. **Handle Potential Errors**:
   - If an error occurs (e.g., permission denied), grant full permissions to the `example/ios` folder:
     <code>sudo chmod -R 777 example/ios</code>
   - Retry the command: <code>sudo npx expo run:ios</code>
   - Ensure Xcode 15.1+ is installed and selected (use <code>sudo xcode-select -s /Applications/Xcode.app/Contents/Developer</code> if needed).

#### Android Setup

1. **Run the Android Build**:
   - From the `example` folder, execute:
     <code>sudo npx expo run:android</code>
   - When prompted with "What would you like your Android package name to be?", enter a value like <code>com.bitchat.example</code>.

2. **Handle Potential Errors**:
   - If an error occurs (e.g., Android SDK not found), create a `local.properties` file in `example/android/` with:
     <code>sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk</code>
   - Replace `YOUR_USERNAME` with your actual macOS username.
   - Ensure JDK 17 is installed and configured (verify with <code>java -version</code>).

### Usage

- After successful setup, the Expo app will launch in the iOS Simulator or Android Emulator.
- Test the integration of `react-native-bitchat` by importing and using it in `example/App.js` (e.g., <code>import Bitchat from 'react-native-bitchat';</code>).
- Refer to the root `README.md` for module-specific usage examples.

### Troubleshooting

- **Permission Issues**: Re-run <code>sudo chmod -R 777</code> on affected directories if commands fail due to access restrictions.
- **Dependency Conflicts**: Ensure the Expo SDK version matches React Native 0.72.10. Update `package.json` or reinstall if needed.
- **Build Failures**: Check Expo CLI output for detailed error messages and adjust configurations (e.g., Xcode version, Android SDK path) accordingly.

### Contributing

Contributions to this example are welcome! Please fork the repository, make changes, and submit pull requests. Ensure the example remains compatible with the parent module.

### License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/nguyentruonglong/react-native-bitchat/blob/main/LICENSE) file in the root directory for details.
