Requirements:<br/>
jre - для запуска<br/>
geckodriver или chromedriver - для запуска "управляемого браузера"<br/>
gradle - для сборки проекта<br/>
заполненная таблица "profile" базы данных (sqlite3 для создания, заполнения таблиц)<br/>
<br/>
Info:<br/>
После каждого запуска для экономии места можно удалять папку cache-directory (задается в src/main/resources/selenium.properties), если не задано clear-cache-files.<br/>
Можно посмотреть сгенерированный javadoc<br/>
Имеется возможность запускать веб-драйвер в фоновом режиме<br/>
**********************************************************
Получение фотографий (некоторого их количества) с профилей в Instagram и сохранение их в базу данных при необходимости:<br/>
Можно использовать ту же базу данных что и в Inet, но при этом необходимо сделать следующие изменения:<br/>
Таблица profile немного отличается от используемой в Inet программе (добавлен атрибут amp - add more photo):
CREATE TABLE "profile" (<br/>
	"id"	    INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"person"	TEXT NOT NULL,<br/>
	"amp"	    INTEGER NOT NULL DEFAULT 0,<br/>
	"pte"	    INTEGER NOT NULL DEFAULT 0<br/>
);<br/>
<br/>
Также используются две одинаковых таблицы с разными именами - одна для хранения фотографий пользователя, вторая - для временного хранения фотографий:<br/>
CREATE TABLE "photos" (<br/>
	"id"	        INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	INTEGER NOT NULL,<br/>
	"type"	        TEXT NOT NULL,<br/>
	"photo"	        BLOB NOT NULL,<br/>
	"added_at"	    TEXT NOT NULL<br/>
);<br/>
<br/>
CREATE TABLE "photos_temp" (<br/>
	"id"	        INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	INTEGER NOT NULL,<br/>
	"type"	        TEXT NOT NULL,<br/>
	"photo"	        BLOB NOT NULL,<br/>
	"added_at"	    TEXT NOT NULL<br/>
);<br/>
**********************************************************
запуск<br/>
java -jar Inet/MPCAddinion/build/libs/MPCAddition-1.0.jar -jar <директория с файлами конфигураций (config.d)><br/>
