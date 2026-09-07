# 🌿 Plant Identifier

A modern, personal-use Android application for identifying plant species from photos, powered by the [Pl@ntNet API](https://my.plantnet.org/).

![App Icon](app/src/main/res/drawable/ic_app_icon_foreground.xml)

## ✨ Features

*   **Snap or Pick:** Take a photo using the custom in-app camera (CameraX) or select an image from your device gallery.
*   **Organ Selection:** Improve identification accuracy by tagging your photo as a Leaf, Flower, Fruit, Bark, Habit, or Other.
*   **Real-time Identification:** Instant species matching with confidence scores and common names.
*   **History Library:** Automatically saves every successful identification to a local database for offline browsing.
*   **Intelligent Optimization:** Automatic image resizing and compression to ensure fast uploads and low data usage.
*   **Material 3 UI:** A clean, modern interface following the latest Android design guidelines.
*   **Offline Awareness:** Graceful handling of network connectivity issues.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM + Clean Architecture principles
*   **Dependency Injection:** Hilt
*   **Networking:** Retrofit 3 + OkHttp 5 + Kotlinx Serialization
*   **Database:** Room
*   **Camera:** CameraX
*   **Image Loading:** Coil
*   **Testing:** JUnit, MockK, Turbine (Flow testing), Compose UI Test, Robolectric

## 🚀 Getting Started

### 1. Prerequisites
*   Android Studio Ladybug (or newer)
*   A Pl@ntNet API Key (Get one at [my.plantnet.org](https://my.plantnet.org/))

### 2. Setup
1.  Clone this repository.
2.  Open the project in Android Studio.
3.  Add your API key to `local.properties`:
    ```properties
    PLANTNET_API_KEY=your_actual_api_key_here
    ```
4.  Sync Gradle and run the app!

## 🏗 Architecture

The app follows **Modern Android Development (MAD)** practices:
*   **UI Layer:** Jetpack Compose with state-aware ViewModels.
*   **Domain Layer:** Clean interfaces and data models defining the core business logic.
*   **Data Layer:** Repositories managing the flow between the Retrofit network service and the Room local database.

## 📄 Attribution

The image-based plant species identification service used, is based on the Pl@ntNet recognition API, regularly updated and accessible through the site [https://my.plantnet.org/](https://my.plantnet.org/).

## ⚖️ License

This project is for personal use. See Pl@ntNet's terms of use for API limitations and requirements.
