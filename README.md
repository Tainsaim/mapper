# Key Mapper — Android 15+

Приложение для маппинга клавиш физической клавиатуры на нажатия в любом месте экрана.

## Быстрый старт (без ПК)

### Вариант 1: GitHub Actions
1. Создай репозиторий на github.com и загрузи все файлы
2. Перейди в Actions → Build Debug APK → Run workflow
3. После завершения скачай `KeyMapper-debug.apk` из Artifacts прямо с телефона

### Вариант 2: Termux (сборка на телефоне)
```bash
# Установи Termux с https://f-droid.org
pkg update && pkg upgrade -y
pkg install -y openjdk-17

# Скачай и распакуй SDK
mkdir -p ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
mv cmdline-tools latest

export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

sdkmanager --licenses
sdkmanager "platforms;android-35" "build-tools;35.0.0"

# Сборка
cd ~/KeyMapper
./gradlew assembleDebug

# APK будет здесь:
# app/build/outputs/apk/debug/app-debug.apk
```

## Использование приложения

1. Установи APK
2. Открой приложение
3. Нажми **"Разрешить отображение поверх приложений"** → разреши
4. Нажми **"Включить сервис"** → найди "Key Mapper Service" → включи
5. Вернись в приложение
6. Нажми **"+ Добавить маппинг"**
7. Нажми нужную клавишу на клавиатуре
8. Тапни по синему оверлею в нужном месте экрана
9. Маппинг работает во всех приложениях!

## Особенности

- `return true` в сервисе — клавиша поглощается (символ не печатается)
- Координаты в пикселях привязаны к разрешению экрана
- Сервис работает в фоне даже когда приложение свёрнуто
