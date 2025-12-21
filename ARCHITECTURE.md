# Архитектура приложения

## Обзор

Color Palette Extractor построен на основе современной Android архитектуры с использованием паттерна MVVM (Model-View-ViewModel) и принципов Clean Architecture.

## Архитектурные слои

### 1. Presentation Layer (Слой представления)

#### UI Components (Compose)
- `ColorPaletteScreen.kt` - главный экран приложения
- `AnalyticsScreen.kt` - экран с метриками и аналитикой

#### ViewModels
- `ColorPaletteViewModel.kt` - управление состоянием UI и бизнес-логикой извлечения цветов

**Особенности:**
- Использование Jetpack Compose для декларативного UI
- StateFlow для reactive обновления UI
- Обработка lifecycle событий

### 2. Domain Layer (Доменный слой)

#### Models
- `AppState.kt` - sealed class для состояний приложения:
  - `Initial` - начальное состояние
  - `Loading` - загрузка
  - `Success` - успешное извлечение цветов
  - `Error` - ошибка

- `ColorInfo.kt` - модель данных цвета:
  - `color: Color` - цвет
  - `hexCode: String` - hex-код
  - `name: String` - название цвета
  - `population: Int` - популярность

#### Business Logic
- `ColorPaletteExtractor.kt` - извлечение цветовой палитры из изображений
- `AppMonitor.kt` - центральная система мониторинга

### 3. Data Layer (Слой данных)

#### Database (Room)

**AnalyticsDatabase:**
- Локальное хранилище всех аналитических данных
- Singleton паттерн для единственного экземпляра

**Entities:**
- `SessionEntity` - сессии пользователя
- `CrashEntity` - информация о крешах
- `ErrorEntity` - логирование ошибок
- `PerformanceMetricEntity` - метрики производительности
- `FeatureUsageEntity` - использование функций
- `FeedbackEntity` - отзывы пользователей

**DAO (Data Access Object):**
- `AnalyticsDao` - интерфейс для доступа к данным
- SQL запросы для CRUD операций
- Suspend функции для асинхронной работы

#### Utilities
- `Analytics.kt` - вспомогательные аналитические функции
- `AnalyticsStorage.kt` - управление хранением данных
- `PerformanceMonitor.kt` - мониторинг производительности

## Паттерны проектирования

### 1. MVVM (Model-View-ViewModel)

```
View (Compose UI) → ViewModel → Model (Data)
      ↑                              ↓
      └────────── StateFlow ←────────┘
```

**Преимущества:**
- Разделение ответственности
- Тестируемость
- Reactive обновления UI
- Управление lifecycle

### 2. Repository Pattern

ViewModel не работает напрямую с данными, используется промежуточный слой (Repository).

### 3. Singleton Pattern

- `AnalyticsDatabase` - единственный экземпляр базы данных
- `AppMonitor` - единственный экземпляр системы мониторинга

### 4. Observer Pattern

Использование Flow и StateFlow для наблюдения за изменениями данных.

### 5. Factory Pattern

Создание ViewModels через ViewModelProvider.

## Потоки данных

### Извлечение цветов из изображения

```
User selects image
      ↓
ColorPaletteScreen (UI)
      ↓
ColorPaletteViewModel.processImage()
      ↓
ColorPaletteExtractor.extractPalette()
      ↓
Android Palette API
      ↓
List<ColorInfo>
      ↓
StateFlow<AppState.Success>
      ↓
UI update (recomposition)
```

### Сбор аналитики

```
User action / App event
      ↓
AppMonitor.recordX()
      ↓
AnalyticsDao (Room)
      ↓
SQLite Database
      ↓
AppMonitor.getHealthReport()
      ↓
AnalyticsScreen (UI)
```

## Управление состоянием

### UI State
```kotlin
sealed class AppState {
    object Initial : AppState()
    object Loading : AppState()
    data class Success(val imageUri: Uri, val colors: List<ColorInfo>) : AppState()
    data class Error(val message: String) : AppState()
}
```

### Lifecycle управление

**MainActivity:**
- `onCreate()` - инициализация AppMonitor, запись startup time
- `onResume()` - начало сессии
- `onPause()` - завершение сессии
- `onDestroy()` - логирование

## Асинхронность

### Coroutines
- `viewModelScope` - для ViewModel операций
- `CoroutineScope(Dispatchers.IO)` - для фоновых операций
- `suspend` функции для последовательных операций

### Flow
- `StateFlow` - для UI состояния
- `Flow` - для потоков данных из базы

## Обработка ошибок

### Try-Catch блоки
```kotlin
try {
    // операция
} catch (e: Exception) {
    // логирование ошибки
    appMonitor?.recordError(...)
    // обновление UI
}
```

### Sealed Classes
Использование sealed class для type-safe обработки состояний.

## Dependency Injection

Простой DI через:
- Singleton объекты
- Factory функции
- Context передача

## Тестируемость

Архитектура позволяет легко тестировать:
- ViewModels (unit tests)
- Business logic (unit tests)
- Database (instrumented tests)
- UI (UI tests with Compose)

## Производительность

### Оптимизации:
- Ленивая загрузка изображений (Coil)
- Фоновая обработка (Coroutines)
- Кэширование результатов
- Efficient recomposition (Compose)

### Мониторинг:
- Время запуска приложения
- Время обработки изображения
- Частота ошибок
- Crash rate

## Расширяемость

Архитектура позволяет легко добавлять:
- Новые источники данных
- Новые экраны
- Новые метрики
- Новые функции

## Безопасность

- Локальное хранение данных (Room)
- Проверка разрешений (READ_MEDIA_IMAGES)
- Валидация входных данных
- Обработка исключений

## Лучшие практики

1. **Separation of Concerns** - каждый класс имеет одну ответственность
2. **Single Source of Truth** - StateFlow как единственный источник UI состояния
3. **Unidirectional Data Flow** - данные текут в одном направлении
4. **Immutability** - использование data class и val
5. **Reactive Programming** - Flow и StateFlow
6. **Lifecycle Awareness** - правильная работа с lifecycle
7. **Resource Management** - автоматическая очистка ресурсов

## Диаграммы

Подробные UML диаграммы см. в [DIAGRAMS.md](DIAGRAMS.md)

