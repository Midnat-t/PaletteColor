https://disk.yandex.ru/i/4jxOO4rxFnOvTQ - видео для просмотра работы приложения.
# Color Palette Extractor

Приложение для Android, которое извлекает цветовую палитру из изображений и предоставляет hex-коды доминирующих цветов.

## Описание

Color Palette Extractor позволяет пользователям выбирать изображения из галереи и автоматически извлекать из них доминирующие цвета. Приложение использует библиотеку Android Palette API для анализа изображений и предоставляет удобный интерфейс для просмотра и копирования цветовых кодов.

## Основные возможности

- Извлечение доминирующих цветов из изображений
- Отображение hex-кодов цветов
- Копирование цветов в буфер обмена
- Просмотр популярности каждого цвета
- Система сбора отзывов пользователей
- Встроенная аналитика и мониторинг производительности
- Dashboard с метриками качества приложения

## Технологический стек

- **Язык**: Kotlin
- **UI Framework**: Jetpack Compose
- **Архитектура**: MVVM (Model-View-ViewModel)
- **База данных**: Room Database
- **Async операции**: Kotlin Coroutines, Flow
- **Image loading**: Coil
- **Color extraction**: AndroidX Palette
- **Минимальная версия Android**: API 24 (Android 7.0)

## Структура проекта

```
app/src/main/java/com/example/colorpalette/
├── MainActivity.kt                    # Главная Activity
├── data/                              # Модели данных
│   ├── AppState.kt
│   └── ColorInfo.kt
├── database/                          # Room база данных
│   ├── AnalyticsDatabase.kt
│   ├── dao/
│   │   └── AnalyticsDao.kt
│   └── entities/
│       ├── SessionEntity.kt
│       ├── CrashEntity.kt
│       ├── ErrorEntity.kt
│       ├── PerformanceMetricEntity.kt
│       ├── FeatureUsageEntity.kt
│       └── FeedbackEntity.kt
├── monitoring/                        # Система мониторинга
│   └── AppMonitor.kt
├── ui/                                # UI компоненты
│   ├── components/
│   │   ├── ColorPaletteScreen.kt
│   │   └── AnalyticsScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── utils/                             # Утилиты
│   ├── Analytics.kt
│   ├── AnalyticsStorage.kt
│   ├── ColorPaletteExtractor.kt
│   └── PerformanceMonitor.kt
└── viewmodel/                         # ViewModels
    └── ColorPaletteViewModel.kt
```

## Установка и запуск

1. Клонируйте репозиторий
2. Откройте проект в Android Studio
3. Синхронизируйте Gradle файлы
4. Запустите приложение на эмуляторе или физическом устройстве

## Требования к системе

- Android Studio Arctic Fox или выше
- JDK 11 или выше
- Android SDK API 24 или выше
- Gradle 8.9.1

## Использование

1. Запустите приложение
2. Нажмите кнопку "Select Image"
3. Выберите изображение из галереи
4. Просмотрите извлеченные цвета
5. Нажмите на цвет для копирования hex-кода

## Аналитика

Приложение включает встроенную систему аналитики, которая отслеживает:

- Сессии пользователей
- Производительность приложения
- Ошибки и крэши
- Использование функций
- Отзывы пользователей

Для просмотра метрик нажмите кнопку "Analytics" в главном экране.

## Документация

- [Архитектура приложения](ARCHITECTURE.md)
- [Система аналитики](ANALYTICS.md)
- [UML диаграммы](DIAGRAMS.md)

## Лицензия

Этот проект создан в образовательных целях.

## Авторы

Разработано как демонстрация современных практик разработки Android приложений.

