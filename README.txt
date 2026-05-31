Патч для вкладки статистики и уведомлений.

Как применить:
1) Распакуйте архив поверх корня проекта так, чтобы файлы из папки C заменили файлы в вашем проекте C.
2) В Android Studio выполните Sync Project with Gradle Files.
3) Запустите Clean Project и Rebuild Project.

Что изменено:
- MainActivity.java: убран несуществующий ReminderScheduler, добавлена регистрация захода и планировщик уведомлений.
- ProgressFragment.java: вместо заглушки теперь полноценная вкладка статистики.
- StatisticsDao.java и StatisticsRepository.java: статистика хранится в Room.
- TheoryTopicActivity.java: прочитанная теория засчитывается при достижении 95%+ чтения.
- PracticeTaskActivity.java: история попыток и успешные решения задач записываются в статистику.
- TestPassingActivity.java и TestResultActivity.java: результаты тестов записываются в статистику, повторное прохождение открывает тот же тест.
- AndroidManifest.xml: удалена ссылка на отсутствующую StatisticsActivity, оставлен receiver уведомлений.
- drawer_menu.xml: убран дублирующий пункт nav_statistics.
