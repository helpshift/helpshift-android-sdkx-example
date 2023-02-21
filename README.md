# Helpshift SDK X Android Example App

Sample Android project demonstrating the integration of Helpshift SDK X

## Requirements

* See Helpshift SDK X requirements [here](https://developers.helpshift.com/sdkx_android/getting-started/)

## Import project

1. Clone the repositiory
2. Open `helpshift-sdkx-example` in Android Studio or you can directly checkout via version control option in Android Studio

## Building the project

Please follow these steps to build the app:
* Update your Helpshift App credentials in `helpshift-sdkx-example/installCreds.gradle` file. To get your Helpshift app credentials please check [here](https://developers.helpshift.com/sdkx_android/getting-started/#start-using).
* FCM push notification is already integrated in the example app but we have provided a dummy `google-services.json` file. 
     * You can configure FCM by providing your own `google-services.json` file at `helpshift-sdkx-example/app/google-services.json`.
     * You can then provide the FCM API Key in Helpshift Dashboard as mentioned [here](https://developers.helpshift.com/sdkx_android/notifications/#push-via-helpshift)
* Build the project in Android Studio and Run on your device.


## Example feature implementations

### Initializing Helpshift SDK via `install`

* Refer to `MainApplication.java` class, `onCreate()` method.
* Notice that we have initialized the SDK as soon as the app is launched.


NOTE: `Helpshift.install()` must be called before invoking any other api in the Helpshift SDK. 


### User Management

* Refer to the following package for User related integration and example code: [User Management](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/user/LoginActivity.java)
* Developer Documentation: [User](https://developers.helpshift.com/sdkx_android/users/)

### SDK Configurations

* Refer to the following package for SDK configurations: [Configurations](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/config)
* It contains custom example for CIF, please modify according to your needs.
* Many other configurations are picked from the example app UI.
* You can check the configurations taken at runtime in this example code: [Configuration from UI](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/MainActivity.java#L260)
* Developer Documentation: [Configurations](https://developers.helpshift.com/sdkx_android/sdk-configuration/)

### Showing Conversation/FAQ screens, Breadcrumbs, Logs, setting Language etc

* For example code of various other features please refer to code examples in [MainAcitvity](helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/MainActivity.java) 
* The code is easy to interpret since each button on UI has been linked with a feature.
* For example if you need example code for showing Conversation Screen, start refering from [Conversation onClick](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/MainActivity.java#L283)
* Developer Documentation: [Helpshift APIs](https://developers.helpshift.com/sdkx_android/support-tools/)

### Handling push notifications from Helpshift

* To handle push notifications from Helpshift, refer the following code example: [Helpshift Push Notification](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/firebase/MyFirebaseMessagingService.java)
* Notice that we have checked "origin" as "helpshift" before calling `handlePush` with the SDK.
* NOTE: In case the app is killed in background, the system will first invoke `MainApplication.onCreate()` and only then delegate control to `MyFirebaseMessagingService`. Now since `MainApplication.onCreate()` is called first, we ensure that `Helpshift.install()` is called before calling `Helpshift.handlePush()` api.
* Developer Documentation: [Notifications](https://developers.helpshift.com/sdkx_android/notifications/)

### Handling Proactive Outbound Notifications

* To show Proactive Outbound notification on device when receiving push notifications check the code sample here: [Outbound Notification](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/firebase/MyFirebaseMessagingService.java)
* Handling click of this notification: [Handle Proactive Outbound Notification Click](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/proactive/ProactiveNotificationActivity.java)
* Handling Proactive Outbound links as deep links: [Proactive Outbound as Deeplink](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/proactive/ProactiveDeepLinkActivity.java)
* NOTE: In case the app is killed in background, the system will first invoke `MainApplication.onCreate()` and only then delegate control to `ProactiveNotificationActivity` or `ProactiveDeepLinkActivity`. Now since `MainApplication.onCreate()` is called first, we ensure that `Helpshift.install()` is called before calling `Helpshift.handleProactiveLink()` api.

* Developer Documentation: [Proactive Outbound](https://developers.helpshift.com/sdkx_android/outbound-support/)

### Handling Deeplinks

* Example code to handle deeplinks: [Deeplinks example](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/deeplink/DeepLinkActivity.java)
* Developer Documentation: [Deep Linking](https://developers.helpshift.com/sdkx_android/deep-linking/)

### Event Delegates
 
* Example code to check delegate callbacks from Helpshift SDK: [Event Listener Example](/helpshift-sdkx-example/app/src/main/java/com/helpshift/liteyagami/MainActivity.java#L110)

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
