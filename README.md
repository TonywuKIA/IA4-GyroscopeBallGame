# Assignment 4 – Gyroscope-Controlled Ball Game

## Overview

This project implements a simple tilt-controlled ball game using Android and Jetpack Compose.
The player controls a ball by tilting the device and navigates through obstacles to reach the goal area.

---

## Features

* Motion-based control using device sensors (accelerometer)
* Ball movement based on device tilt
* Obstacles forming a simple maze
* Collision detection to prevent passing through obstacles
* Goal area (green region)
* Optional win message and reset button

---

## Implementation Details

* Built using **Kotlin** and **Jetpack Compose**
* UI is rendered using the **Canvas API**
* Device motion is captured using the **SensorManager**
* Accelerometer is used for reliable tilt control (especially in emulator)

---

## How to Run

1. Open the project in Android Studio
2. Run the app on:

   * A physical Android device (recommended), or
   * An emulator (use Virtual Sensors to simulate tilt)
3. Tilt the device to move the ball toward the goal

---

## Notes

* Emulator testing requires manually adjusting tilt via:

  * Extended Controls → Virtual Sensors → Device Pose
* A physical device provides a smoother experience

---

## Screenshots
<img width="421" height="834" alt="屏幕截图 2026-03-29 205011" src="https://github.com/user-attachments/assets/21f65e69-7733-4693-89ac-e955bdbd4d1e" />
<img width="515" height="1041" alt="屏幕截图 2026-03-29 212126" src="https://github.com/user-attachments/assets/55c486a3-f43f-40f4-973f-ebb3c8fff831" />

