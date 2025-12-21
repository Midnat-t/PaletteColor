# UML Диаграммы

## Диаграмма классов

### PlantUML код

```plantuml
@startuml Color_Palette_Architecture

' Styling
skinparam classAttributeIconSize 0
skinparam backgroundColor #FEFEFE
skinparam class {
    BackgroundColor<<UI>> LightBlue
    BackgroundColor<<ViewModel>> LightGreen
    BackgroundColor<<Data>> LightYellow
    BackgroundColor<<Database>> Wheat
    BackgroundColor<<Util>> Pink
}

' === PRESENTATION LAYER ===
package "Presentation Layer" {
    class MainActivity <<UI>> {
        - appMonitor: AppMonitor
        - startTime: Long
        + onCreate()
        + onResume()
        + onPause()
        + onDestroy()
    }
    
    class ColorPaletteScreen <<UI>> {
        + ColorPaletteScreen(viewModel)
        - MainScreenContent()
        - InitialScreen()
        - LoadingScreen()
        - SuccessScreen()
        - ErrorScreen()
        - FeedbackDialog()
    }
    
    class AnalyticsScreen <<UI>> {
        + AnalyticsScreen(appMonitor, onBack)
        - AnalyticsContent()
        - HeaderCard()
        - MetricCard()
        - StatisticsCard()
        - ClearDataDialog()
    }
    
    class ColorPaletteViewModel <<ViewModel>> {
        - _uiState: MutableStateFlow<AppState>
        + uiState: StateFlow<AppState>
        - extractor: ColorPaletteExtractor
        - appMonitor: AppMonitor
        + processImage(context, imageUri)
        + resetState()
        + getMetrics(): Map<String, Any>
    }
}

' === DOMAIN LAYER ===
package "Domain Layer" {
    class AppState <<sealed>> {
        + Initial
        + Loading
        + Success(imageUri, colors)
        + Error(message)
    }
    
    class ColorInfo {
        + color: Color
        + hexCode: String
        + name: String
        + population: Int
    }
    
    class HealthReport {
        + crashRate: Double
        + errorRate: Double
        + avgSessionLength: Double
        + avgAppStartTime: Double
        + retentionRate7Days: Double
        + csi: Double
        + nps: Double
        + totalSessions: Int
        + totalCrashes: Int
        + totalErrors: Int
        + totalFeedback: Int
    }
}

' === BUSINESS LOGIC ===
package "Business Logic" {
    class AppMonitor <<Util>> {
        - database: AnalyticsDatabase
        - dao: AnalyticsDao
        - currentSessionId: Long?
        + startSession()
        + endSession()
        + recordCrash(throwable)
        + recordError(type, message, context, severity)
        + recordPerformance(type, duration, success)
        + trackFeatureUsage(featureName)
        + recordFeedback(rating, comment)
        + getHealthReport(): HealthReport
        + getCSI(): Double
        + getNPS(): Double
        + clearAllData()
        + cleanupOldData(daysToKeep)
    }
    
    class ColorPaletteExtractor <<Util>> {
        + extractPalette(context, imageUri): List<ColorInfo>
        - loadBitmap(context, uri): Bitmap
        - getPalette(bitmap): Palette
        - extractColors(palette): List<ColorInfo>
    }
    
    enum PerformanceMetricType {
        APP_START
        IMAGE_PROCESSING
        SCREEN_LOAD
        COLOR_EXTRACTION
        DATABASE_QUERY
    }
    
    enum ErrorSeverity {
        LOW
        MEDIUM
        HIGH
        CRITICAL
    }
}

' === DATA LAYER ===
package "Data Layer" {
    class AnalyticsDatabase <<Database>> {
        {static} - INSTANCE: AnalyticsDatabase
        + analyticsDao(): AnalyticsDao
        {static} + getDatabase(context): AnalyticsDatabase
    }
    
    interface AnalyticsDao <<Database>> {
        ' Session methods
        + insertSession(session): Long
        + updateSession(session)
        + getRecentSessions(limit): Flow<List<SessionEntity>>
        + getTotalSessionCount(): Int
        + getAverageSessionDuration(): Double?
        
        ' Crash methods
        + insertCrash(crash)
        + getAllCrashes(): Flow<List<CrashEntity>>
        + getTotalCrashCount(): Int
        + getCrashCountSince(since): Int
        
        ' Error methods
        + insertError(error)
        + getRecentErrors(limit): Flow<List<ErrorEntity>>
        + getTotalErrorCount(): Int
        + getErrorCountBySeverity(severity): Int
        
        ' Performance methods
        + insertPerformanceMetric(metric)
        + getMetricsByType(type, limit): Flow<List<PerformanceMetricEntity>>
        + getAverageDuration(type): Double?
        
        ' Feature usage methods
        + insertFeatureUsage(usage)
        + getFeatureUsageCounts(): List<FeatureUsageCount>
        
        ' Feedback methods
        + insertFeedback(feedback)
        + getAllFeedback(): Flow<List<FeedbackEntity>>
        + getAverageRating(): Double?
        + getPositiveFeedbackCount(): Int
        + getNegativeFeedbackCount(): Int
        
        ' Cleanup methods
        + clearAllSessions()
        + clearAllCrashes()
        + clearAllErrors()
        + clearAllMetrics()
        + clearAllUsage()
        + clearAllFeedback()
    }
    
    class SessionEntity <<Database>> {
        + id: Long
        + startTime: Long
        + endTime: Long?
        + duration: Long?
        + appVersion: String
        + androidVersion: Int
        + deviceModel: String
    }
    
    class CrashEntity <<Database>> {
        + id: Long
        + timestamp: Long
        + exceptionType: String
        + exceptionMessage: String
        + stackTrace: String
        + appVersion: String
        + androidVersion: Int
        + deviceModel: String
    }
    
    class ErrorEntity <<Database>> {
        + id: Long
        + timestamp: Long
        + errorType: String
        + errorMessage: String
        + context: String
        + severity: String
    }
    
    class PerformanceMetricEntity <<Database>> {
        + id: Long
        + timestamp: Long
        + metricType: String
        + duration: Long
        + success: Boolean
        + additionalData: String?
    }
    
    class FeatureUsageEntity <<Database>> {
        + id: Long
        + timestamp: Long
        + featureName: String
        + sessionId: Long?
    }
    
    class FeedbackEntity <<Database>> {
        + id: Long
        + timestamp: Long
        + rating: Int
        + comment: String
        + appVersion: String
        + contextInfo: String?
    }
}

' === RELATIONSHIPS ===

' Presentation relationships
MainActivity --> ColorPaletteScreen : "создает"
MainActivity --> AppMonitor : "использует"
ColorPaletteScreen --> ColorPaletteViewModel : "наблюдает"
ColorPaletteScreen --> AnalyticsScreen : "навигирует"
AnalyticsScreen --> AppMonitor : "использует"

' ViewModel relationships
ColorPaletteViewModel --> AppState : "управляет"
ColorPaletteViewModel --> ColorInfo : "создает"
ColorPaletteViewModel --> ColorPaletteExtractor : "использует"
ColorPaletteViewModel --> AppMonitor : "использует"

' Business Logic relationships
AppMonitor --> AnalyticsDatabase : "использует"
AppMonitor --> AnalyticsDao : "использует"
AppMonitor --> HealthReport : "создает"
AppMonitor --> PerformanceMetricType : "использует"
AppMonitor --> ErrorSeverity : "использует"
ColorPaletteExtractor --> ColorInfo : "создает"

' Data relationships
AnalyticsDatabase --> AnalyticsDao : "предоставляет"
AnalyticsDao --> SessionEntity : "управляет"
AnalyticsDao --> CrashEntity : "управляет"
AnalyticsDao --> ErrorEntity : "управляет"
AnalyticsDao --> PerformanceMetricEntity : "управляет"
AnalyticsDao --> FeatureUsageEntity : "управляет"
AnalyticsDao --> FeedbackEntity : "управляет"

@enduml
```

## Диаграмма последовательности - Извлечение цветов

### PlantUML код

```plantuml
@startuml Color_Extraction_Sequence

actor Пользователь
participant ColorPaletteScreen
participant ColorPaletteViewModel
participant ColorPaletteExtractor
participant "Android Palette API" as Palette
participant AppMonitor
participant AnalyticsDao
database Room

Пользователь -> ColorPaletteScreen : Выбирает изображение
activate ColorPaletteScreen

ColorPaletteScreen -> ColorPaletteViewModel : processImage(context, imageUri)
activate ColorPaletteViewModel

ColorPaletteViewModel -> ColorPaletteViewModel : _uiState = Loading
ColorPaletteViewModel -> ColorPaletteScreen : StateFlow обновляется
ColorPaletteScreen -> ColorPaletteScreen : Показывает LoadingScreen

ColorPaletteViewModel -> AppMonitor : trackFeatureUsage("color_extraction")
activate AppMonitor
AppMonitor -> AnalyticsDao : insertFeatureUsage(usage)
activate AnalyticsDao
AnalyticsDao -> Room : INSERT INTO feature_usage
deactivate AnalyticsDao
deactivate AppMonitor

ColorPaletteViewModel -> ColorPaletteExtractor : extractPalette(context, imageUri)
activate ColorPaletteExtractor

ColorPaletteExtractor -> ColorPaletteExtractor : loadBitmap(context, uri)
ColorPaletteExtractor -> ColorPaletteExtractor : getPalette(bitmap)
ColorPaletteExtractor -> Palette : Palette.from(bitmap).generate()
activate Palette
Palette --> ColorPaletteExtractor : palette
deactivate Palette

ColorPaletteExtractor -> ColorPaletteExtractor : extractColors(palette)
ColorPaletteExtractor --> ColorPaletteViewModel : List<ColorInfo>
deactivate ColorPaletteExtractor

ColorPaletteViewModel -> AppMonitor : recordPerformance(IMAGE_PROCESSING, duration, true)
activate AppMonitor
AppMonitor -> AnalyticsDao : insertPerformanceMetric(metric)
activate AnalyticsDao
AnalyticsDao -> Room : INSERT INTO performance_metrics
deactivate AnalyticsDao
deactivate AppMonitor

ColorPaletteViewModel -> ColorPaletteViewModel : _uiState = Success(uri, colors)
ColorPaletteViewModel -> ColorPaletteScreen : StateFlow обновляется
deactivate ColorPaletteViewModel

ColorPaletteScreen -> ColorPaletteScreen : Показывает SuccessScreen с цветами
deactivate ColorPaletteScreen

ColorPaletteScreen -> Пользователь : Отображает извлеченные цвета

@enduml
```

## Диаграмма последовательности - Сбор аналитики сессий

### PlantUML код

```plantuml
@startuml Session_Analytics_Sequence

actor Пользователь
participant MainActivity
participant AppMonitor
participant AnalyticsDao
database "Room Database" as Room

== Запуск приложения ==

Пользователь -> MainActivity : Открывает приложение
activate MainActivity

MainActivity -> MainActivity : onCreate()
MainActivity -> AppMonitor : getInstance(context)
activate AppMonitor
AppMonitor --> MainActivity : appMonitor
deactivate AppMonitor

MainActivity -> MainActivity : onResume()
MainActivity -> AppMonitor : startSession()
activate AppMonitor

AppMonitor -> AppMonitor : Проверка: currentSessionId != null?
note right: Защита от дубликатов

AppMonitor -> AppMonitor : sessionStartTime = currentTimeMillis()
AppMonitor -> AnalyticsDao : insertSession(SessionEntity)
activate AnalyticsDao
AnalyticsDao -> Room : INSERT INTO sessions
Room --> AnalyticsDao : session id
AnalyticsDao --> AppMonitor : sessionId
deactivate AnalyticsDao

AppMonitor -> AppMonitor : currentSessionId = sessionId
deactivate AppMonitor

== Использование приложения ==

Пользователь -> MainActivity : Использует приложение

== Сворачивание/закрытие ==

Пользователь -> MainActivity : Сворачивает приложение
MainActivity -> MainActivity : onPause()
MainActivity -> AppMonitor : endSession()
activate AppMonitor

AppMonitor -> AppMonitor : Проверка: currentSessionId == null?
note right: Проверка активной сессии

AppMonitor -> AppMonitor : endTime = currentTimeMillis()
AppMonitor -> AppMonitor : duration = endTime - sessionStartTime

AppMonitor -> AnalyticsDao : getSessionsSince(sessionStartTime)
activate AnalyticsDao
AnalyticsDao -> Room : SELECT FROM sessions WHERE startTime >= ?
Room --> AnalyticsDao : List<SessionEntity>
AnalyticsDao --> AppMonitor : sessions
deactivate AnalyticsDao

AppMonitor -> AnalyticsDao : updateSession(session.copy(endTime, duration))
activate AnalyticsDao
AnalyticsDao -> Room : UPDATE sessions SET endTime=?, duration=? WHERE id=?
deactivate AnalyticsDao

AppMonitor -> AppMonitor : currentSessionId = null
deactivate AppMonitor

deactivate MainActivity

== Просмотр аналитики ==

Пользователь -> AnalyticsScreen : Открывает Analytics
activate AnalyticsScreen

AnalyticsScreen -> AppMonitor : getHealthReport()
activate AppMonitor

AppMonitor -> AnalyticsDao : getAverageSessionDuration()
activate AnalyticsDao
AnalyticsDao -> Room : SELECT AVG(duration) FROM sessions WHERE duration IS NOT NULL
Room --> AnalyticsDao : avgDuration
AnalyticsDao --> AppMonitor : avgDuration
deactivate AnalyticsDao

AppMonitor -> AnalyticsDao : getTotalSessionCount()
activate AnalyticsDao
AnalyticsDao -> Room : SELECT COUNT(*) FROM sessions
Room --> AnalyticsDao : count
AnalyticsDao --> AppMonitor : count
deactivate AnalyticsDao

AppMonitor -> AppMonitor : Расчет других метрик
AppMonitor --> AnalyticsScreen : HealthReport
deactivate AppMonitor

AnalyticsScreen -> Пользователь : Отображает метрики
deactivate AnalyticsScreen

@enduml
```

## Диаграмма компонентов

### PlantUML код

```plantuml
@startuml Component_Diagram

package "Android Device" {
    
    package "Application Layer" {
        [MainActivity] as MA
        [ColorPaletteScreen] as CPS
        [AnalyticsScreen] as AS
    }
    
    package "ViewModel Layer" {
        [ColorPaletteViewModel] as CPVM
    }
    
    package "Business Logic Layer" {
        [AppMonitor] as AM
        [ColorPaletteExtractor] as CPE
    }
    
    package "Data Layer" {
        [AnalyticsDatabase] as ADB
        [AnalyticsDao] as ADAO
    }
    
    package "Persistence" {
        database "SQLite\nRoom Database" as DB
    }
    
    package "Android Framework" {
        [Palette API] as PAL
        [Coil Image Loader] as COIL
        [Jetpack Compose] as COMPOSE
    }
}

' Relationships
MA --> CPS : создает UI
MA --> AM : управляет сессиями
CPS --> CPVM : наблюдает StateFlow
CPS --> AS : навигация
CPS --> COMPOSE : использует
AS --> AM : получает метрики

CPVM --> CPE : извлекает цвета
CPVM --> AM : записывает метрики

AM --> ADB : получает DAO
AM --> ADAO : выполняет запросы

CPE --> PAL : анализ цветов
CPE --> COIL : загрузка изображений

ADB --> ADAO : предоставляет
ADAO --> DB : SQL запросы

note right of AM
  Центральный компонент
  для сбора аналитики
end note

note bottom of DB
  Локальное хранилище
  всех метрик
end note

@enduml
```

## Диаграмма развертывания

### PlantUML код

```plantuml
@startuml Deployment_Diagram

node "Android Device" {
    
    component "Application APK" {
        artifact "ColorPalette.apk"
    }
    
    node "Android Runtime (ART)" {
        
        component "Application Process" {
            [MainActivity]
            [ColorPaletteScreen]
            [AnalyticsScreen]
            [ViewModels]
            [Business Logic]
        }
        
        database "Local Storage" {
            [SQLite Database]
            [SharedPreferences]
        }
        
        component "Android Framework" {
            [Jetpack Compose]
            [Room]
            [Coroutines]
            [Palette API]
        }
    }
    
    component "Hardware" {
        [CPU]
        [Memory]
        [Storage]
        [Display]
    }
}

[Application Process] --> [Android Framework] : использует
[Android Framework] --> [Hardware] : использует
[Business Logic] --> [Local Storage] : читает/пишет
[SQLite Database] --> [Storage] : хранится на

note right of [Local Storage]
  Все данные хранятся локально
  Нет внешних серверов
end note

@enduml
```

## Использование диаграмм

### Для PlantUML

1. Установите PlantUML: https://plantuml.com/download
2. Или используйте онлайн редактор: https://www.plantuml.com/plantuml/uml/
3. Скопируйте код диаграммы
4. Вставьте в редактор
5. Экспортируйте в PNG/SVG

### Для Mermaid (альтернатива)

GitHub и многие Markdown редакторы поддерживают Mermaid диаграммы напрямую.

