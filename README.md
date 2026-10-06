# Hello World (Android)

A minimal Android app that displays "Hello, World!".

## Requirements

- **Android Studio** (Ladybug or newer recommended) or **JDK 17** + Android SDK
- **Android SDK** with API 24+ (min), 34 (compile/target)

## Run the app

### Option 1: Android Studio

1. Open Android Studio → **File → Open** and select this folder.
2. Wait for Gradle sync to finish.
3. Connect a device or start an emulator (**Tools → Device Manager**).
4. Click **Run** (green triangle) or press `Shift+F10`.

### Option 2: Command line

If you have the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) in the project:

```bash
# Build debug APK
./gradlew assembleDebug

# Install and run on connected device
./gradlew installDebug
```

If the wrapper is missing, run `gradle wrapper` from a machine with Gradle installed, or open the project in Android Studio once to generate it.

## Project structure

```
helloworld/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/helloworld/MainActivity.kt
│       └── res/
│           ├── drawable/     # app icon
│           ├── layout/      # activity_main.xml
│           └── values/      # strings, themes, colors
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 前鏡頭姿態骨架（MediaPipe Pose）

從「超慢跑」頁點擊中間相機圖示會開啟前鏡頭，並疊加 **MediaPipe 姿態骨架**（關節點與連線）。

### 姿態模型檔（必要）

請將 MediaPipe Pose 模型檔放入 `app/src/main/assets/`：

1. 下載 **pose_landmarker_lite.task**（約 3MB）：  
   https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task  
2. 將下載的檔案重新命名為 `pose_landmarker_lite.task`（若尚未是此名稱）。  
3. 放到專案目錄：`app/src/main/assets/pose_landmarker_lite.task`（若沒有 `assets` 資料夾請先建立）。

若未放置此檔，開啟前鏡頭時會顯示「缺少姿態模型檔」提示，骨架不會出現。

## Tech

- **Kotlin** 1.9
- **Android Gradle Plugin** 8.2
- **minSdk** 24, **targetSdk** 34
- **UI**: one `MainActivity` with a centered "Hello, World!" text (Material theme).
