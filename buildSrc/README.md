# Convention Plugins

## kotlin-base

**Причина появления**

Устранить дублирование конфигураций во всех gradle модулях проекта.

**Функционал**

- Применяет Kotlin JVM компилятор (`org.jetbrains.kotlin.jvm`)
- Устанавливает JVM toolchain 17 для компиляции и запуска
- Добавляет тестовые зависимости: `junit` и `kotlinx-coroutines-test`

**Подключение**

```kotlin
plugins {
    alias(libs.plugins.kotlinBase)
}
```

**Когда стоит расширять**

- Нужна общая настройка компилятора для всех модулей (например, `-Xexplicit-api=strict`, `allWarningsAsErrors`)
- Появляется новая тестовая зависимость, которую должны получать все модули
- Меняется целевая версия JVM — достаточно поправить `jvmToolchain` в одном месте
