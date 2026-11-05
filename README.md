# FoodBridge 🥦🍞

**A community surplus-food-sharing Android app.**
OPSC6312 Portfolio of Evidence (POE) — Part 3 Final Submission.

Hope Malatjie (ST10444867) · Chantal Mafa (ST10444319) · Keabetswe Modiba (ST10438507) · Karabo Mojapelo (ST10436116)
The IIE Rosebank International College

> 📹 **Demo Video Presentation:** `ADD YOUR UNLISTED YOUTUBE LINK HERE BEFORE SUBMITTING`

---

## Overview & Lecturer Feedback Compliance

In full compliance with the lecturer's feedback, **Jetpack Compose has been completely removed** from this assessment submission. The application is developed strictly using **Kotlin and native XML layouts**, incorporating Android Activities, Fragments, `ViewBinding`, `ConstraintLayout`, `RecyclerView`, Material Components, and the AndroidX Navigation Component (`nav_graph.xml`).

---

## Core Features & POE Requirements

FoodBridge connects surplus food from donors (restaurants, grocery stores, households) with community recipients and shelters before it goes to waste.

- **User Registration & Login (FR-1)**: Secured authentication flow supporting Email/Password with password encryption and **Single Sign-On (SSO)** via Google Sign-In.
- **Surplus Food Feed (FR-3)**: Browse food listings in a responsive XML `RecyclerView` with real-time category filtering, search, and swipe-to-refresh capabilities.
- **Nearby Food Map (FR-3)**: Google Maps SDK integration (`MapFragment` + `MapView`) displaying interactive markers for nearby surplus food listings.
- **Post Surplus Food (FR-2)**: Donors upload food details, quantities, pickup windows, location coordinates, and photos via Coil image loader.
- **Listing Details & Reservation (FR-4)**: View complete listing info, donor badges, countdown pickup windows, and reserve listings atomically.
- **In-App Messaging (FR-5)**: Real-time chat between recipient and donor scoped to individual food reservations.
- **Impact Profile & Settings (FR-6)**: Track meals saved, CO2 avoided (kg), and access app settings.
- **Offline Mode with Room Synchronization (PoE Only)**: Local SQLite/Room database (`FoodBridgeDatabase`, `ListingDao`, `ReservationDao`) caches listings locally when offline and synchronizes pending actions when connection is restored.
- **Multi-Language Support (PoE Only)**: Full localization for three languages including two South African languages:
  - **English** (Default: `res/values/strings.xml`)
  - **isiZulu** (`res/values-zu/strings.xml`)
  - **Afrikaans** (`res/values-af/strings.xml`)
  - Includes dynamic in-app language switching via `LocaleHelper`.
- **Push Notifications (FCM)**: Real-time notification updates integrated with `FoodBridgeMessagingService`.

---

## Architecture & Project Structure

FoodBridge uses a clean Model-View-ViewModel (MVVM) architecture with ViewBinding:

```
app/
├── data/
│   ├── local/          # Room DB: FoodBridgeDatabase, ListingDao, ReservationDao, Local entities
│   ├── model/          # Domain models: User, FoodListing, Reservation, ChatMessage, ImpactStats
│   ├── remote/         # Retrofit FoodBridgeApi + DTOs + NetworkModule
│   └── repository/     # Firebase, Room DB, & REST-backed repositories behind interfaces
├── ui/
│   ├── auth/           # LoginFragment, RegisterFragment, AuthViewModel (SSO & Auth)
│   ├── home/           # HomeFeedFragment, ListingAdapter, HomeViewModel
│   ├── map/            # MapFragment (Google Maps SDK)
│   ├── post/           # PostListingFragment, PostListingViewModel
│   ├── listing/        # ListingDetailFragment, ListingDetailViewModel
│   ├── chat/           # ChatFragment, MessageAdapter, ChatViewModel
│   └── impact/         # ImpactProfileFragment, ImpactViewModel (Settings & Language Switcher)
├── util/               # LocaleHelper, LocationHelper, UiState wrapper, FCM service
└── res/
    ├── layout/         # Native XML layouts for activities, fragments, and recycler items
    ├── menu/           # bottom_nav_menu.xml
    ├── navigation/     # nav_graph.xml (Navigation Component)
    ├── values/         # English strings, colors, themes
    ├── values-zu/      # isiZulu strings
    └── values-af/      # Afrikaans strings
```

---

## Getting Started

1. **Clone Repository:** Clone the repository and open in Android Studio (Koala or newer).
2. **Firebase Setup:** Download your project's `google-services.json` and place it in `app/google-services.json`. Ensure Authentication (Email/Password & Google), Firestore, Storage, and FCM are enabled.
3. **Google Maps API Key:** Add your API key to `maps_api_key` in `app/src/main/res/values/strings.xml`.
4. **Build & Run:** Select the `:app` configuration and run on an Android device or emulator (minSdk 26).

---

## Automated Testing & GitHub Actions (CI/CD)

Automated unit testing covers the ViewModel, Repository, and Room DB layers.

- **Run Unit Tests Locally:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **GitHub Actions Pipeline (`.github/workflows/build.yml`):**
  - Triggers automatically on every push or pull request to `main`, `master`, or `develop`.
  - Sets up JDK 17, runs unit tests (`./gradlew test`), compiles the debug APK (`./gradlew assembleDebug`), and uploads the APK artifact.

---

## AI Tool Usage Write-up (Max 500 words)

Generative AI (Claude by Anthropic and Google Gemini in Android Studio) was utilized as an engineering assistant during the development and migration phase of FoodBridge, adhering to the assessment guidelines:

1. **Architecture & Refactoring to XML Layouts:** AI tools assisted in converting legacy Compose UI code into standard Kotlin `Fragment` classes paired with native Android XML layouts (`ConstraintLayout`, `RecyclerView`, `MaterialComponents`).
2. **Offline Room Database & Sync Engine:** AI assisted in drafting Room Entities (`LocalListingEntity`, `LocalReservationEntity`), DAOs (`ListingDao`, `ReservationDao`), and `FoodBridgeDatabase` to establish offline caching and background synchronization in `FoodBridgeListingRepository`.
3. **Multi-Language Support (Localization):** AI assisted in generating accurate string translations for South African languages (**isiZulu** and **Afrikaans**) in `res/values-zu/strings.xml` and `res/values-af/strings.xml`, as well as creating the `LocaleHelper` utility for dynamic context switching.
4. **CI/CD Pipeline Setup:** AI assisted in configuring the GitHub Actions workflow script (`.github/workflows/build.yml`) to automate unit testing and build APK artifacts on GitHub.

All AI-generated code was thoroughly reviewed, tested, debugged, and integrated by the development team to ensure compliance with project requirements.
