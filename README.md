# Floating Clock

A lightweight native Android floating clock built with Java.

## Features

- Starts the floating clock when the launcher icon is tapped.
- Displays live time in 24-hour format with seconds.
- Tap the clock to switch to a compact circular bubble.
- Tap the bubble to expand the clock again.
- Drag the clock or bubble to reposition it.
- Remembers the last position and display mode.
- Uses a foreground service and partial wakelock to help keep the clock active.

## Build

Open this repository as an Android Gradle project in Android Studio or CodeAssist ADE and build the `app` module.

- Package: `com.azuki.floatingclock`
- Minimum Android: 6.0 (API 23)
- Target SDK: 28

## First launch

Android requires the user to grant **Display over other apps** permission. The app opens that system permission page once; after permission is granted, it starts the floating clock and closes its activity.

The persistent notification indicates that the foreground service is running. Stop the service from Android Settings if you want to dismiss the floating clock completely.
