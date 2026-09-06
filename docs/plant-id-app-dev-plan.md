# Plant ID Android App — Development Plan

A step-by-step plan for building a personal-use plant identification app using the
Pl@ntNet API, structured for AI-assisted development: small, independently
verifiable steps, each with a clear "definition of done" and test coverage.

---

## 0. Tech Stack (current best practice, 2026)

| Concern | Choice | Why |
|---|---|---|
| Language | Kotlin | Standard for new Android apps |
| UI | Jetpack Compose | Declarative, less boilerplate, easy to test |
| Architecture | MVVM + light Clean Architecture (data / domain / ui layers) | Testable, AI-friendly (small, single-purpose files) |
| DI | Hilt | Standard, reduces manual wiring, easy to fake in tests |
| Async | Kotlin Coroutines + Flow | Standard for state and async work |
| Networking | Retrofit + OkHttp + kotlinx.serialization (or Moshi) | Mature, well-documented, easy to mock |
| Camera | CameraX | Modern camera API, works across devices |
| Local storage | Room (for identification history) | Simple, testable persistence |
| Testing | JUnit4/5, MockK, Turbine (Flow testing), Compose UI Test, Robolectric (optional) | Full pyramid: unit → UI → instrumented |
| Build | Gradle version catalogs (`libs.versions.toml`) | Current recommended dependency management |

---

## 1. Guiding Principles for the Build Process

1. **Vertical slices, not layers.** Each step should produce something you can run
   or test, not just "add the network layer" in isolation.
2. **Fake before real.** Build against a fake/mock Pl@ntNet client first, wire in
   the real API last. This decouples UI work from API quota/network issues.
3. **One class of behavior per commit.** Small diffs are easier for both you and
   an AI assistant to verify.
4. **Tests written alongside, not after.** Ask for a test with every non-trivial
   step; treat "no test" as a code smell to flag, not accept silently.
5. **Secrets never in source.** API key goes in `local.properties` /
   `BuildConfig`, never committed.

---

## 2. Step-by-Step Plan

Each step below is sized to be a single focused AI-assisted session (roughly
15–45 min of work), with a explicit verification method.

### Phase A — Project Skeleton

**Step A1: Create project scaffold**
- New Android Studio project, Compose template, min SDK 26+, package name set.
- Add Hilt, Retrofit, Room, coroutines, testing libs to `libs.versions.toml`.
- Configure Edge-to-Edge support (WindowCompat.setDecorFitsSystemWindows).
- ✅ Verify: project builds and runs, shows default "Hello" screen with content drawing behind system bars.

**Step A2: App architecture skeleton**
- Create empty packages: `data`, `domain`, `ui`, `di`.
- Add `Application` class with `@HiltAndroidApp`.
- ✅ Verify: app still builds and launches with Hilt initialized (no crash).

**Step A3: CI-friendly test setup**
- Add a trivial unit test (e.g. `1 + 1 == 2`) and a trivial Compose UI test
  (renders a text node) to confirm both test runners work.
- ✅ Verify: `./gradlew test` and `./gradlew connectedAndroidTest` (or
  Robolectric equivalent) both pass.

### Phase B — Domain Model & Fake Data Layer

**Step B1: Define domain model**
- `PlantIdentificationResult` (species name, scientific name, common names,
  confidence score, family, thumbnail URL).
- `PlantRepository` interface with `suspend fun identify(images: List<ImageInput>, organs: List<Organ>): Result<List<PlantIdentificationResult>>`.
- ✅ Verify: unit test constructs the model and checks basic properties
  (data class equality, etc.) — trivial but confirms compile correctness.

**Step B2: Fake repository implementation**
- `FakePlantRepository` returns canned results (success case + error case)
  after a small artificial delay.
- ✅ Verify: unit test asserts fake returns expected fixture data.

**Step B3: ViewModel using the fake repository**
- `PlantIdViewModel` exposing a `StateFlow<PlantIdUiState>` with states:
  `Idle`, `Loading`, `Success(results)`, `Error(message)`.
- Inject `PlantRepository` via Hilt (bound to fake for now).
- ✅ Verify: unit test using Turbine drives the ViewModel and asserts state
  transitions `Idle → Loading → Success`.

### Phase C — UI Against the Fake Layer

**Step C1: Basic Compose screen**
- Screen with: image placeholder, "Pick Image" button (no camera yet, just a
  stub button), "Identify" button, result list.
- Wire to `PlantIdViewModel`; button click triggers a hardcoded fake image.
- ✅ Verify: Compose UI test — click button, assert loading indicator then
  result text appears.

**Step C2: Error and empty states**
- Add UI for error state (message + retry button) and empty state.
- ✅ Verify: Compose UI test forces `FakePlantRepository` to return an error
  and asserts the error UI renders.

**Step C3: Image picker integration (real)**
- Replace stub button with Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- ✅ Verify: manual smoke test (photo picker opens, selected image renders in
  the UI) — this step is inherently harder to automate, note it as a manual
  checkpoint.

### Phase D — Real Pl@ntNet API Integration

**Step D1: API key & config**
- Add `PLANTNET_API_KEY` to `local.properties`, expose via `BuildConfig`.
- Add a `.gitignore` check test/script confirming `local.properties` is
  ignored.
- ✅ Verify: `BuildConfig.PLANTNET_API_KEY` is non-empty at runtime (debug log
  or simple assertion in a debug-only screen).

**Step D2: Retrofit service definition**
- Define `PlantNetApiService` interface matching `POST /v2/identify`
  (multipart images + organ list + api-key query param).
- Define DTOs matching the Pl@ntNet response shape (species, score, images).
- ✅ Verify: unit test with **MockWebServer** — feed a canned JSON response,
  assert Retrofit parses it into the expected DTO.

**Step D3: Real repository implementation**
- `PlantNetRepository` implements `PlantRepository`, converts DTOs → domain
  model, maps HTTP/network errors to domain `Result.failure`.
- Implement image optimization utility (resizing/compression) to stay within
  API limits and improve upload speed.
- ✅ Verify: unit tests with MockWebServer for: success case, 4xx (bad
  request/quota), 5xx, and network timeout — each mapped to the right
  `Result`/UI state.

**Step D4: Wire real repository via Hilt**
- Hilt module binds `PlantRepository` to `PlantNetRepository` in release/
  debug builds, keeps `FakePlantRepository` available for a test build
  variant or DI test module.
- ✅ Verify: run the app for real, take/pick a photo of a plant, confirm a
  real identification result appears end-to-end. This is your first true
  integration smoke test.

### Phase E — Camera Capture

**Step E0: Camera permissions handling**
- Implement a Compose-friendly permission request flow using
  `rememberLauncherForActivityResult`.
- ✅ Verify: manual test — app requests camera permission; handles "denied"
  and "permanently denied" states gracefully.

**Step E1: CameraX preview screen**
- Add a capture screen with CameraX preview and a shutter button.
- ✅ Verify: manual check — preview renders, shutter button captures a file
  to app-private storage.

**Step E2: Feed captured photo into identification flow**
- Captured image path flows into the same ViewModel/repository path as the
  picker image (reuse Step C3/D4 plumbing).
- ✅ Verify: manual end-to-end test — take a photo of a real plant, get a
  result.

**Step E3: Organ selection UI**
- Let the user tag the photo as flower/leaf/fruit/bark/habit (Pl@ntNet's
  `organs` parameter improves accuracy).
- ✅ Verify: unit test that selected organ is passed through to the repository
  call; manual test that accuracy improves/changes with organ tagging.

### Phase F — Persistence & History

**Step F1: Room schema**
- `IdentificationEntity` (timestamp, image path, top result, confidence).
- ✅ Verify: Room in-memory DB unit test — insert, query, assert round-trip.

**Step F2: Save results on successful identification**
- ViewModel/repository writes to Room after a successful API call.
- ✅ Verify: unit test with fake DAO confirms save is triggered exactly once
  on `Success`, not on `Error`.

**Step F3: History screen**
- List past identifications, tap to see details again.
- ✅ Verify: Compose UI test with seeded fake DB data renders expected rows.

### Phase G — Polish & Robustness

**Step G1: Offline/error handling polish**
- No-network detection, friendly error copy, retry logic.
- ✅ Verify: unit tests for the "no connectivity" branch; manual test with
  airplane mode.

**Step G2: Rate limiting / API quota awareness**
- Pl@ntNet free tier has a daily request cap — surface remaining
  quota/errors gracefully rather than crashing.
- ✅ Verify: unit test simulates a 429 response and asserts correct UI
  messaging.

**Step G3: App icon, naming, minimal settings screen**
- Since it's personal use: maybe just an "About" screen with API attribution
  (Pl@ntNet requires attribution per their terms — check current terms
  before shipping even for personal use).
- ✅ Verify: manual visual check.

**Step G4: Location context (optional)**
- Integrate location services to send GPS coordinates with the API request
  to improve identification accuracy based on regional flora.
- ✅ Verify: manual test — confirm coordinates are passed in the API request
  when location permission is granted.

---

## 3. Test Coverage Strategy Summary

| Layer | Test type | Tooling |
|---|---|---|
| Domain models, mappers | Unit | JUnit + MockK |
| Repository (API layer) | Unit, with fake server | JUnit + MockWebServer |
| ViewModel | Unit, state-flow assertions | JUnit + Turbine + MockK |
| Room DAO | Instrumented or Robolectric | Room in-memory DB |
| Compose screens | UI test | Compose Test + fake ViewModel/repository |
| End-to-end | Manual smoke test | Real device, real API key |

Rule of thumb: **anything with logic branches gets a unit test; anything with
layout/interaction gets a Compose test; only true cross-system behavior
(camera hardware, real network) stays manual.**

---

## 4. Suggested Working Rhythm with an AI Assistant

For each step above:
1. State the step explicitly ("Let's do Step D3: real repository
   implementation") so the assistant scopes its change to that step only.
2. Ask for the implementation **and** its test in the same request.
3. Run the test yourself before moving on — don't let steps stack unverified.
4. If a step feels too big to review in one sitting, split it further (e.g.
   Step D3 could split into "success case" and "error mapping" separately).

---

## 5. Notes on Pl@ntNet API Specifics

- Account + private API key required: generate at `my.plantnet.org` under
  Settings → API key.
- Endpoint: `POST /v2/identify/{project}` — supports 1–5 images per request,
  each optionally tagged with an `organ` (flower, leaf, fruit, bark, habit,
  other).
- Check current rate limits and attribution requirements in your account
  dashboard before relying on them long-term — free-tier terms can change.
