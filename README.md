Requirements:<br/>
jre - для запуска<br/>
geckodriver или chromedriver - для запуска "управляемого браузера"<br/>
maven - для сборки проекта<br/>
заполненная таблица "profile" базы данных (sqlite3 для создания, заполнения таблиц)<br/>
<br/>
Info:<br/>
После каждого запуска для экономии места можно удалять папку cache--directory (задается в src/main/resources/selenium.properties), если не задано cache--clear-cache-files.<br/>
Команды для сборки и запуска находятся в run.sh<br/>
Можно посмотреть сгенерированный javadoc<br/>
Имеется возможность запускать веб-драйвер в фоновом режиме<br/>
Информация добавляется в базу данных при следующем запуске программы при условии наличия изменений в данных или в аватаре<br/>
Имеется возможность использования прокси, но ее использование крайне нежелательно т.к. могут возникнуть ошибки веб-драйвера, связанные с недостаточным временем поиска элемента на странице, также в некоторых случаях подключиться к прокси невозможно<br/>
**********************************************************
Получение базовой информации с профилей в Instagram и сохранение изменений в профиле в базу данных:<br />
<ol>
<li>фото профиля;</li>
<li>количество публикаций;</li>
<li>количество подписчиков;</li>
<li>количество подписок.</li>
</ol>
<br/>

Вся информация сохраняется в базе данных, таблицы которой можно создать, выполнив (для sqlite) (pte - person track enabled):<br/>
CREATE TABLE "profile" (<br/>
	"id"		INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"person"	TEXT NOT NULL<br/>
	"pte"	INTEGER NOT NULL DEFAULT 0<br/>
);<br/>
<br/>
CREATE TABLE "avatar" (<br/>
	"id"	        INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	INTEGER NOT NULL,<br/>
	"type"	        TEXT NOT NULL,<br/>
	"data"	        BLOB NOT NULL,<br/>
	"firstseen"	    TEXT NOT NULL<br/>
);<br/>
<br/>
CREATE TABLE "info" (<br/>
	"id"	                INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	        INTEGER NOT NULL,<br/>
	"publications_count"	INTEGER NOT NULL,<br/>
	"subscribers_count"	    INTEGER NOT NULL,<br/>
	"subscriptions_count"	INTEGER NOT NULL,<br/>
	"modified_at"	        TEXT NOT NULL<br/>
);
**********************************************************
сборка проекта<br/>
mvn clean compile assembly:single<br/>
запуск<br/>
java -jar target/Inet-1.0-jar-with-dependencies.jar<br/>
или<br/>
java -jar target/Inet-1.0-jar-with-dependencies.jar <путь к database.properties> <путь к selenium.properties><br/>
или<br/>
java -jar target/Inet-1.0-jar-with-dependencies.jar <лог-файл> <путь к database.properties> <путь к selenium.properties><br/>
