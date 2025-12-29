# UML Диаграммы (Mermaid формат)

## Диаграмма классов (упрощенная)

```mermaid
classDiagram
    %% Presentation Layer
    class MainActivity {
        -AppMonitor appMonitor
        -Long startTime
        +onCreate()
        +onResume()
        +onPause()
        +onDestroy()
    }
    
    class ColorPaletteScreen {
        +ColorPaletteScreen(viewModel)
        -MainScreenContent()
        -SuccessScreen()
        -ErrorScreen()
    }
    
    class AnalyticsScreen {
        +AnalyticsScreen(appMonitor)
        -AnalyticsContent()
        -MetricCard()
    }
    
    class ColorPaletteViewModel {
        -MutableStateFlow~AppState~ _uiState
        +StateFlow~AppState~ uiState
        -ColorPaletteExtractor extractor
        +processImage(context, imageUri)
        +resetState()
    }
    
    %% Domain
    class AppState {
        <<sealed>>
        +Initial
        +Loading
        +Success
        +Error
    }
    
    class ColorInfo {
        +Color color
        +String hexCode
        +String name
        +Int population
    }
    
    %% Business Logic
    class AppMonitor {
        -AnalyticsDatabase database
        -AnalyticsDao dao
        +startSession()
        +endSession()
        +recordCrash(throwable)
        +recordError()
        +getHealthReport()
        +clearAllData()
    }
    
    class ColorPaletteExtractor {
        +extractPalette(context, uri)
        -loadBitmap()
        -getPalette()
        -extractColors()
    }
    
    %% Data Layer
    class AnalyticsDatabase {
        -AnalyticsDatabase INSTANCE
        +analyticsDao()
        +getDatabase(context)
    }
    
    class AnalyticsDao {
        <<interface>>
        +insertSession(session)
        +updateSession(session)
        +insertCrash(crash)
        +insertError(error)
        +getHealthReport()
    }
    
    class SessionEntity {
        +Long id
        +Long startTime
        +Long endTime
        +Long duration
        +String appVersion
    }
    
    class FeedbackEntity {
        +Long id
        +Long timestamp
        +Int rating
        +String comment
    }
    
    %% Relationships
    MainActivity --> ColorPaletteScreen
    MainActivity --> AppMonitor
    ColorPaletteScreen --> ColorPaletteViewModel
    ColorPaletteScreen --> AnalyticsScreen
    ColorPaletteViewModel --> AppState
    ColorPaletteViewModel --> ColorInfo
    ColorPaletteViewModel --> ColorPaletteExtractor
    ColorPaletteViewModel --> AppMonitor
    AnalyticsScreen --> AppMonitor
    AppMonitor --> AnalyticsDatabase
    AppMonitor --> AnalyticsDao
    AnalyticsDatabase --> AnalyticsDao
    AnalyticsDao --> SessionEntity
    AnalyticsDao --> FeedbackEntity
```

## Диаграмма последовательности - Извлечение цветов

```mermaid
sequenceDiagram
    actor Пользователь
    participant Screen as ColorPaletteScreen
    participant VM as ColorPaletteViewModel
    participant Extractor as ColorPaletteExtractor
    participant Palette as Android Palette API
    participant Monitor as AppMonitor
    participant DB as Room Database
    
    Пользователь->>Screen: Выбирает изображение
    activate Screen
    
    Screen->>VM: processImage(context, imageUri)
    activate VM
    
    VM->>VM: _uiState = Loading
    VM->>Screen: StateFlow обновляется
    Screen->>Screen: Показывает LoadingScreen
    
    VM->>Monitor: trackFeatureUsage("color_extraction")
    activate Monitor
    Monitor->>DB: INSERT INTO feature_usage
    deactivate Monitor
    
    VM->>Extractor: extractPalette(context, imageUri)
    activate Extractor
    
    Extractor->>Extractor: loadBitmap(context, uri)
    Extractor->>Palette: Palette.from(bitmap).generate()
    activate Palette
    Palette-->>Extractor: palette
    deactivate Palette
    
    Extractor->>Extractor: extractColors(palette)
    Extractor-->>VM: List<ColorInfo>
    deactivate Extractor
    
    VM->>Monitor: recordPerformance(IMAGE_PROCESSING, duration)
    activate Monitor
    Monitor->>DB: INSERT INTO performance_metrics
    deactivate Monitor
    
    VM->>VM: _uiState = Success(uri, colors)
    VM->>Screen: StateFlow обновляется
    deactivate VM
    
    Screen->>Screen: Показывает SuccessScreen
    Screen->>Пользователь: Отображает цвета
    deactivate Screen
```

## Диаграмма последовательности - Управление сессиями

```mermaid
sequenceDiagram
    actor Пользователь
    participant Activity as MainActivity
    participant Monitor as AppMonitor
    participant DAO as AnalyticsDao
    participant DB as Room Database
    
    %% Запуск приложения
    Note over Пользователь,DB: Запуск приложения
    
    Пользователь->>Activity: Открывает приложение
    activate Activity
    
    Activity->>Activity: onCreate()
    Activity->>Monitor: getInstance(context)
    
    Activity->>Activity: onResume()
    Activity->>Monitor: startSession()
    activate Monitor
    
    Monitor->>Monitor: Проверка дубликатов
    Note right of Monitor: currentSessionId == null?
    
    Monitor->>Monitor: sessionStartTime = now()
    Monitor->>DAO: insertSession(SessionEntity)
    activate DAO
    DAO->>DB: INSERT INTO sessions
    DB-->>DAO: session id
    DAO-->>Monitor: sessionId
    deactivate DAO
    
    Monitor->>Monitor: currentSessionId = sessionId
    deactivate Monitor
    
    %% Использование
    Note over Пользователь,DB: Использование приложения
    Пользователь->>Activity: Работает с приложением
    
    %% Закрытие
    Note over Пользователь,DB: Сворачивание приложения
    
    Пользователь->>Activity: Сворачивает
    Activity->>Activity: onPause()
    Activity->>Monitor: endSession()
    activate Monitor
    
    Monitor->>Monitor: Проверка активной сессии
    Note right of Monitor: currentSessionId != null?
    
    Monitor->>Monitor: duration = now() - startTime
    
    Monitor->>DAO: getSessionsSince(startTime)
    activate DAO
    DAO->>DB: SELECT FROM sessions
    DB-->>DAO: List<SessionEntity>
    DAO-->>Monitor: sessions
    deactivate DAO
    
    Monitor->>DAO: updateSession(endTime, duration)
    activate DAO
    DAO->>DB: UPDATE sessions
    deactivate DAO
    
    Monitor->>Monitor: currentSessionId = null
    deactivate Monitor
    
    deactivate Activity
```

## Диаграмма потока данных

```mermaid
flowchart TD
    Start([Пользователь выбирает изображение]) --> CheckPermission{Разрешения<br/>предоставлены?}
    
    CheckPermission -->|Нет| RequestPermission[Запросить разрешения]
    RequestPermission --> CheckPermission
    
    CheckPermission -->|Да| SetLoading[Установить состояние Loading]
    SetLoading --> TrackFeature[Записать использование функции]
    TrackFeature --> LoadImage[Загрузить изображение из URI]
    
    LoadImage --> ConvertBitmap[Конвертировать в Bitmap]
    ConvertBitmap --> GeneratePalette[Генерация Palette<br/>Android Palette API]
    
    GeneratePalette --> ExtractColors[Извлечь цвета из Palette]
    ExtractColors --> CreateColorInfo[Создать List ColorInfo]
    
    CreateColorInfo --> RecordPerformance[Записать метрику производительности]
    RecordPerformance --> CheckResult{Цвета<br/>найдены?}
    
    CheckResult -->|Да| SetSuccess[Установить состояние Success]
    SetSuccess --> UpdateUI[Обновить UI через StateFlow]
    UpdateUI --> DisplayColors[Отобразить цвета пользователю]
    DisplayColors --> End([Готово])
    
    CheckResult -->|Нет| RecordError[Записать ошибку]
    RecordError --> SetError[Установить состояние Error]
    SetError --> ShowError[Показать сообщение об ошибке]
    ShowError --> End
    
    LoadImage -->|Ошибка| RecordError
    GeneratePalette -->|Ошибка| RecordError
```

## Архитектура MVVM

```mermaid
graph TB
    subgraph "Presentation Layer"
        View[ColorPaletteScreen<br/>Jetpack Compose]
    end
    
    subgraph "ViewModel Layer"
        ViewModel[ColorPaletteViewModel<br/>StateFlow AppState]
    end
    
    subgraph "Domain Layer"
        Model[AppState<br/>ColorInfo]
    end
    
    subgraph "Business Logic"
        Extractor[ColorPaletteExtractor]
        Monitor[AppMonitor]
    end
    
    subgraph "Data Layer"
        DB[(Room Database<br/>SQLite)]
    end
    
    View -->|User Actions| ViewModel
    ViewModel -->|StateFlow| View
    
    ViewModel --> Model
    ViewModel --> Extractor
    ViewModel --> Monitor
    
    Monitor --> DB
    
    style View fill:#E3F2FD
    style ViewModel fill:#C8E6C9
    style Model fill:#FFF9C4
    style DB fill:#FFE0B2
```

## Структура базы данных

```mermaid
erDiagram
    SESSIONS ||--o{ FEATURE_USAGE : "has"
    SESSIONS {
        long id PK
        long startTime
        long endTime
        long duration
        string appVersion
        int androidVersion
        string deviceModel
    }
    
    CRASHES {
        long id PK
        long timestamp
        string exceptionType
        string exceptionMessage
        string stackTrace
        string appVersion
        int androidVersion
        string deviceModel
    }
    
    ERRORS {
        long id PK
        long timestamp
        string errorType
        string errorMessage
        string context
        string severity
    }
    
    PERFORMANCE_METRICS {
        long id PK
        long timestamp
        string metricType
        long duration
        boolean success
        string additionalData
    }
    
    FEATURE_USAGE {
        long id PK
        long timestamp
        string featureName
        long sessionId FK
    }
    
    FEEDBACK {
        long id PK
        long timestamp
        int rating
        string comment
        string appVersion
        string contextInfo
    }
```

## Жизненный цикл сессии

```mermaid
stateDiagram-v2
    [*] --> NoSession: Приложение закрыто
    
    NoSession --> ActiveSession: onResume()<br/>startSession()
    
    state ActiveSession {
        [*] --> Recording
        Recording --> Recording: Пользователь работает<br/>Записываются метрики
    }
    
    ActiveSession --> SessionEnding: onPause()<br/>endSession()
    
    state SessionEnding {
        [*] --> CalculateDuration
        CalculateDuration --> UpdateDatabase
        UpdateDatabase --> ClearSessionId
    }
    
    SessionEnding --> NoSession: Сессия завершена
    
    NoSession --> ActiveSession: Повторное открытие<br/>Новая сессия
```

## Компонентная архитектура

```mermaid
graph TB
    subgraph "UI Layer"
        MainActivity
        ColorPaletteScreen
        AnalyticsScreen
    end
    
    subgraph "ViewModel Layer"
        ColorPaletteViewModel
    end
    
    subgraph "Business Logic Layer"
        AppMonitor
        ColorPaletteExtractor
    end
    
    subgraph "Data Layer"
        AnalyticsDatabase
        AnalyticsDao
    end
    
    subgraph "Persistence"
        RoomDB[(SQLite Database)]
    end
    
    subgraph "Android Framework"
        PaletteAPI[Palette API]
        Coil[Image Loader]
        Compose[Jetpack Compose]
    end
    
    MainActivity --> ColorPaletteScreen
    MainActivity --> AppMonitor
    ColorPaletteScreen --> ColorPaletteViewModel
    ColorPaletteScreen --> AnalyticsScreen
    ColorPaletteScreen --> Compose
    
    AnalyticsScreen --> AppMonitor
    
    ColorPaletteViewModel --> ColorPaletteExtractor
    ColorPaletteViewModel --> AppMonitor
    
    ColorPaletteExtractor --> PaletteAPI
    ColorPaletteExtractor --> Coil
    
    AppMonitor --> AnalyticsDatabase
    AppMonitor --> AnalyticsDao
    
    AnalyticsDatabase --> AnalyticsDao
    AnalyticsDao --> RoomDB
    

```

## Просмотр диаграмм

Эти диаграммы в формате Mermaid автоматически отображаются в:
- GitHub
- GitLab
- VS Code с расширением Mermaid
- Многих других Markdown редакторах

Для PlantUML диаграмм см. файл `DIAGRAMS.md`

