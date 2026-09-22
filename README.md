# FoodBridge 🥦🍞

**A community surplus-food-sharing Android app.**
OPSC6312 Portfolio of Evidence — Part 2 prototype.

Hope Malatjie (ST10444867) · Chantal Mafa (ST10444319) · Keabetswe Modiba (ST10438507) · Karabo Mojapelo (ST10436116)
The IIE Rosebank International College

> 📹 **Demo video:** `ADD YOUR UNLISTED YOUTUBE LINK HERE BEFORE SUBMITTING`

---

## Purpose

Every day, households, cafés and grocery stores end up with food that's still
good but won't be sold or eaten in time. FoodBridge connects the people who
have surplus food with the people and organisations who can use it, before it
goes to waste — combining ideas validated by three existing apps researched in
Part 1 (Too Good To Go, OLIO and Karma):

- A **free, trust-based, peer-to-peer** listing model (like OLIO), so ordinary
  households — not just businesses — can give food away.
- A **fixed pickup window per listing** (like Too Good To Go), which keeps
  collection logistics simple and reduces no-shows.
- **Showing the actual item and a photo** rather than a "surprise bag" (like
  Karma), so recipients know what they're collecting.
- A feature **none of the three had**: direct routing of bulk surplus to
  **NGO/food-bank partner accounts** (Gift of the Givers-style), so a large
  donation can go to an organisation rather than only individuals.

## Design considerations

FoodBridge follows the architecture decided in the Part 1 design document:

- **MVVM** throughout — Compose UI screens are pure functions of
  `StateFlow<UiState<T>>`, and all Firebase/Retrofit access lives in a
  `data/repository` layer behind interfaces, so ViewModels can be unit tested
  against fakes/mocks with no device or emulator.
- **Firebase as backend-as-a-service** — Authentication for sign-in, Cloud
  Firestore for users/listings/reservations, Cloud Storage for listing
  photos, and Cloud Messaging for push notifications.
- **A thin custom REST API in front of Firestore** (`FoodBridgeApi`, called
  via Retrofit) — the Android client never writes to Firestore directly for
  actions with business rules attached (creating a listing, reserving a
  listing). That keeps "a listing can only be reserved once" enforced
  server-side, matching the Part 1 API design. Reads that benefit from
  instant UI updates (the home feed, a listing's live status) use a direct
  Firestore snapshot listener instead, since that's what makes a reservation
  show as "reserved" to every other browsing user immediately.
- **Real-time-first UI** — `callbackFlow` wrappers around Firestore listeners
  feed straight into `StateFlow`, so the feed and listing detail screens
  update live with no manual refresh.

### Feature → requirement map

| Screen | Requirement(s) | Notes |
|---|---|---|
| Login / Register | FR-1 | Firebase Auth (email/password) + Firestore profile doc |
| Home Feed | FR-3 | Real-time Firestore listener, category filter chips |
| Map View | FR-3, SDK requirement | Google Maps Compose SDK, listing markers |
| Post Surplus Food | FR-2 | Coil-loaded photo picker, Cloud Storage upload, REST create |
| Listing Detail | FR-4 | Live status via Firestore, Reserve via REST (atomic) |
| In-app Chat | FR-5 | Firestore-backed, scoped to one reservation |
| My Impact / Profile | FR-6 | Reads meals-saved / CO2e / badge level from REST |
| Push notifications | FR-7 | `FoodBridgeMessagingService` (FCM) |

### External library & SDK (Part 2 requirements)

- **External library — [Coil](https://coil-kt.github.io/coil/)**: async image
  loading for listing photos on the Home Feed, Listing Detail and Post
  screens, so the UI never blocks on a network image fetch.
- **SDK — Google Maps SDK for Android** (`maps-compose` +
  `play-services-location`): the Map View screen plots nearby listings around
  the user's current location, matching Figure 5 of the Part 1 wireframes.

## Architecture

```
app/
├── data/
│   ├── model/         # Domain models: User, FoodListing, Reservation, ChatMessage, ImpactStats
│   ├── remote/         # Retrofit FoodBridgeApi + DTOs + NetworkModule
│   └── repository/     # Firebase + REST-backed repositories behind interfaces
├── ui/
│   ├── auth/           # Login / Register screens + AuthViewModel
│   ├── home/            # Home Feed screen + HomeViewModel
│   ├── map/              # Map View screen (Google Maps SDK)
│   ├── post/              # Post Surplus Food screen + PostListingViewModel
│   ├── listing/            # Listing Detail screen + ListingDetailViewModel
│   ├── chat/                # In-app chat screen + ChatViewModel
│   ├── impact/                # My Impact / Profile screen + ImpactViewModel
│   ├── navigation/             # NavHost + bottom tab bar
│   └── theme/                   # Compose theme
└── util/                # UiState wrapper, FCM service, location helper
```

## Getting started

1. **Clone the repo** and open it in Android Studio (Koala or newer).
2. **Firebase project:** create a Firebase project, add an Android app with
   package name `com.foodbridge.app`, download the real `google-services.json`
   and replace the placeholder file at `app/google-services.json`. Enable
   **Authentication (Email/Password)**, **Firestore**, **Storage** and
   **Cloud Messaging** in the Firebase console.
3. **Backend REST API:** deploy the Cloud Functions described in
   `data/remote/FoodBridgeApi.kt` (`GET /listings`, `POST /listings`,
   `POST /listings/{id}/reserve`, `GET /users/{id}/impact`) to Firebase Cloud
   Functions, then update `API_BASE_URL` in `app/build.gradle.kts` to your
   deployed URL — or point it at the Firebase emulator
   (`http://10.0.2.2:5001/<project-id>/us-central1/api/`) while developing.
4. **Google Maps key:** get a key from the Google Cloud Console and paste it
   into `maps_api_key` in `app/src/main/res/values/strings.xml`.
5. **Run** on an emulator or device (minSdk 26).

## Testing

Unit tests live under `app/src/test` and cover the ViewModel layer against
mocked repositories (Mockito + Turbine + Truth), plus one repository-level
test of the DTO→domain mapping:

```
./gradlew testDebugUnitTest
```

Covered: registration/login success & failure (FR-1), category filtering
(FR-3), reserve success & server-side rejection (FR-4), post-listing form
validation and the happy path (FR-2), and impact-stats loading (FR-6).

## GitHub & GitHub Actions

- **Version control:** the team works on feature branches and merges into
  `main` via pull request, with small, frequent commits rather than one
  large end-of-sprint commit.
- **CI (`.github/workflows/android-build.yml`):** on every push and pull
  request against `main`, GitHub Actions sets up JDK 17, runs
  `./gradlew testDebugUnitTest` so a broken unit test fails the build before
  it can be merged, then runs `./gradlew assembleDebug` and uploads the
  resulting debug APK as a build artifact — so anyone on the team (or a
  marker) can download a working build without setting up Android Studio.

## Data model (Firestore)

```
users/{userId}          — name, email, role, mealsSaved, co2Avoided, badgeLevel
listings/{listingId}    — title, category, quantity, pickupWindow*, status, lat/lng, photoUrl, donorId
reservations/{id}       — listingId, donorId, recipientId, status
  └ messages/{messageId} — senderId, text, sentAt
```

## AI tool usage (max 500 words)

Claude (Anthropic) was used as a development assistant while building this
Part 2 prototype, working from the Part 1 design document (the app overview,
detailed FR/NFR table, wireframes, API contract, UML class diagram and data
dictionary) that the team had already produced. Its use fell into three
areas:

1. **Project scaffolding.** Claude generated the initial Android Studio
   project structure (Gradle config, MVVM package layout, manifest,
   resources) and the Kotlin source for the data layer (models, Retrofit API
   interface, Firebase-backed repositories) directly from the FR-1 to FR-7
   table and the "Key endpoints" section of the design doc, so the generated
   endpoints and data classes match what was specified in Part 1 rather than
   inventing a new design.
2. **UI implementation.** Each Jetpack Compose screen (Login/Register, Home
   Feed, Map View, Post Surplus Food, Listing Detail, Chat, My Impact) was
   generated to match its corresponding wireframe figure and caption from the
   design document, wired to a ViewModel exposing a `StateFlow<UiState<T>>`.
3. **Unit tests and CI.** Claude wrote the ViewModel unit tests (Mockito +
   Turbine + Truth) exercising the success/failure paths for each FR, and the
   `.github/workflows/android-build.yml` GitHub Actions workflow that runs
   those tests and builds a debug APK on every push.

Debugging errors, editing generated code, filling in the team's own final
copy/branding decisions, deploying the actual Cloud Functions backend, and
recording/narrating the demo video are the team's own work, done after
reviewing and adjusting the generated scaffold.

## Known limitations of this prototype

- The Cloud Functions backend referenced by `FoodBridgeApi` is not deployed
  as part of this repository — the team deploys it separately per the
  "Getting started" steps above.
- The pickup-window picker on the Post screen defaults to "now + 2 hours" as
  a placeholder; swap in a real date/time picker before final submission.
- `google-services.json` in this repo is a placeholder — replace it with the
  team's real Firebase config before building.
