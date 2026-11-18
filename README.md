# Telecom+

Личный кабинет Интертелекома (Android) + интеграция IT Phone (Ringotel).

Проект восстановлен из релизной сборки `0.2.24-alpha26.5` (jadx + ручная правка), без внешних зависимостей.

## Структура
- `app/src/main/java/ua/telecomplus/app` — портал (скрапинг `services.intertelecom.ua`), WebView-мост авторизации, провижининг Ringotel, UI.
- `defpackage/` — R8-лямбды, на которые ссылается `MainActivity` (будут вычищены при рефакторинге).

## Сборка
GitHub Actions (`.github/workflows/build.yml`) собирает debug-APK и выкладывает его артефактом `telecom-plus-debug`.

## Известное
- Телефония (звонки) пока не работает: нет медиамодуля. Планируется SIP/RTP + PCMA/PCMU/Opus.
- Debug-подпись отличается от прежней релизной: поверх старой версии не встанет, нужно удалить старую.
