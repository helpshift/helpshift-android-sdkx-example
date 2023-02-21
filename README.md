# Helpshift SDK X Android Example App

Sample Android project demonstrating the integration of Helpshift SDK X

## Requirements

* See Helpshift SDK X requirements [here](https://developers.helpshift.com/sdkx_android/getting-started/)

## Import project

1. Clone the repositiory
2. Open `helpshift-sdkx-example` in Android Studio or you can directly checkout via version control option in Android Studio

## Building the project

Please follow these steps to build the app:
* Enter your install credentials in `helpshift-sdkx-example/installCreds.gradle` file. To get your Helpshift app credentials please check [here](https://developers.helpshift.com/sdkx_android/getting-started/#start-using).
* FCM push notification is already integrated in the example app but we have provided a dummy `google-services.json` file. 
     * You can configure FCM by providing your own `google-services.json` file at `helpshift-sdkx-example/app/google-services.json`.
     * You can then provide the FCM API Key in Helpshift Dashboard as mentioned [here](https://developers.helpshift.com/sdkx_android/notifications/#push-via-helpshift)
* Build the project in Android Studio and Run on your device.


## Example feature implementations

### Initializing Helpshift SDK via `install`

* Refer to `MainApplication.java` class, `onCreate()` method.
* Notice that we have initialized the SDK as soon as the app is launched.

### User Management

* Refer to the following package for User related integration and example code: [User Management](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/user/LoginActivity.java)
* Developer Documentation: [User](https://developers.helpshift.com/sdkx_android/users/)

### SDK Configurations

* Refer to the following package for SDK configurations: [Configurations](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/config)

## Resources
* Documentation: [https://developers.helpshift.com/sdkx_android/getting-started/](https://developers.helpshift.com/sdkx_android/getting-started/)
* Release Notes: [https://developers.helpshift.com/sdkx_android/release-notes/](https://developers.helpshift.com/sdkx_android/release-notes/)

## License

```
Copyright 2021, Helpshift, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
