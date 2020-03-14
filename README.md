Requirements:<br/>
jre - для запуска<br/>
geckodriver или chromedriver - для запуска "управляемого браузера"<br/>
maven - для сборки<br/>
созданная база данных и заполненная таблица "profile"<br/>
<br/>
Build (находясь в корне проекта):<br/>
Создание и заполнение базы данных:<br/>
Создание каталога для базы данных - mkdir -p /path/to/db/file<br/>
Создание таблицы avatar:<br/>
sqlite3 /path/to/db/file/file.db 'CREATE TABLE "avatar"("id" INTEGER PRIMARY KEY AUTOINCREMENT,"profile_id" INTEGER NOT NULL,"type" TEXT NOT NULL,"data" BLOB NOT NULL,"firstseen" TEXT NOT NULL);' '.exit'<br/>
Создание таблицы info:<br/>
sqlite3 /path/to/db/file/file.db 'CREATE TABLE "info"("id" INTEGER PRIMARY KEY AUTOINCREMENT,"profile_id" INTEGER NOT NULL,"publications_count" INTEGER NOT NULL,"subscribers_count" INTEGER NOT NULL,"subscriptions_count" INTEGER NOT NULL,"modified_at" TEXT NOT NULL);' '.exit'<br/>
Создание таблицы photos:<br/>
sqlite3 /path/to/db/file/file.db 'CREATE TABLE "photos"("id" INTEGER PRIMARY KEY AUTOINCREMENT,"profile_id" INTEGER NOT NULL,"type" TEXT NOT NULL DEFAULT 'jpeg',"photo" BLOB NOT NULL,"added_at" TEXT NOT NULL);' '.exit'<br/>
Создание таблицы photos_temp:<br/>
sqlite3 /path/to/db/file/file.db 'CREATE TABLE "photos_temp"("id" INTEGER PRIMARY KEY AUTOINCREMENT,"profile_id" INTEGER NOT NULL,"type" TEXT NOT NULL DEFAULT 'jpeg',"photo" BLOB NOT NULL,"added_at" TEXT NOT NULL);' '.exit'<br/>
Создание таблицы profile:<br/>
sqlite3 /path/to/db/file/file.db 'CREATE TABLE "profile"("id" INTEGER PRIMARY KEY AUTOINCREMENT,"person" TEXT NOT NULL,"amp" INTEGER NOT NULL DEFAULT 0,"pte" INTEGER NOT NULL DEFAULT 0);' '.exit'<br/>
<br/>
Далее нужно заполнить таблицу profile необходимыми данными любым удобным способом, при этом:<br/>
id - уникальный идентификатор строки, autoincrement, заполняется автоматически либо указывается явно;<br/>
person - профиль пользователя (если ссылка выглядит так - https://instagram.com/someuser, то в person занести someuser)<br/>
amp - add more photos - добавлять фотографии пользователя из основного блока - блока публикаций, значение 1 - собирать (по умолчанию в настройках database.properties в запросе используется 1 для отметки сбора)<br/>
pte - нужно ли собирать базовую (публикации, подписки, подписчики, аватар) информацию о пользователе, значение 1 - собирать (по умолчанию в настройках database.properties в запросе используется 1 для отметки сбора)<br/>
<br/>
Собрать Support и установить в локальный репозиторий maven:<br/>
cd Support && mvn clean install > build.log && cd ..<br/>
Собрать Core, упаковать в архив со всеми зависимостями:<br/>
cd Core && mvn clean compile assembly:single > build.log && cd ..<br/>
<br/>
Затем необходимо настроить конфигурации логгера, базы данных и веб-драйвера оптимальными значениями.<br/>
<br/>
Run (находясь в корне проекта):<br/>
java -jar Core/target/Core-1.0-jar-with-dependencies.jar config.d<br/>
<br/>
Info:<br/>
Можно посмотреть сгенерированный javadoc<br/>
Имеется возможность запускать веб-драйвер в фоновом режиме<br/>
Информация добавляется в базу данных при следующем запуске программы при условии наличия изменений в данных или в аватаре, аналогично для фотографий<br/>
Имеется возможность использования прокси, но ее использование крайне нежелательно т.к. могут возникнуть ошибки веб-драйвера, связанные с недостаточным временем поиска элемента на странице, также в некоторых случаях подключиться к прокси невозможно<br/>
Присутствует bash-скрипт извлечения BLOB из базы данных (xtrakt.sh)<br/>
<br/>
**********************************************************
Получение базовой информации с профилей в Instagram и сохранение изменений в профиле в базу данных:<br />
<ol>
<li>аватар профиля;</li>
<li>количество публикаций;</li>
<li>количество подписчиков;</li>
<li>количество подписок;</li>
<li>фотографии профиля.</li>
</ol>
<br/>

Вся информация сохраняется в базе данных, таблицы которой можно создать, выполнив (для sqlite) (pte - person track enabled):<br/>
CREATE TABLE "avatar" (<br/>
	"id"		INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	INTEGER NOT NULL,<br/>
	"type"		TEXT NOT NULL,<br/>
	"data"		BLOB NOT NULL,<br/>
	"firstseen"	TEXT NOT NULL<br/>
);<br/>
CREATE TABLE "info" (<br/>
	"id"			INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"		INTEGER NOT NULL,<br/>
	"publications_count"	INTEGER NOT NULL,<br/>
	"subscribers_count"	INTEGER NOT NULL,<br/>
	"subscriptions_count"	INTEGER NOT NULL,<br/>
	"modified_at"		TEXT NOT NULL<br/>
);<br/>
CREATE TABLE "photos" (<br/>
	"id"		INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	INTEGER NOT NULL,<br/>
	"type"		TEXT NOT NULL DEFAULT 'jpeg',<br/>
	"photo"		BLOB NOT NULL,<br/>
	"added_at"	TEXT NOT NULL<br/>
);<br/>
CREATE TABLE "photos_temp" (<br/>
	"id"		INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"profile_id"	INTEGER NOT NULL,<br/>
	"type"		TEXT DEFAULT 'jpeg',<br/>
	"photo"		BLOB NOT NULL,<br/>
	"added_at"	TEXT NOT NULL<br/>
);<br/>
CREATE TABLE "profile" (<br/>
	"id"		INTEGER PRIMARY KEY AUTOINCREMENT,<br/>
	"person"	TEXT NOT NULL,<br/>
	"amp"		INTEGER NOT NULL DEFAULT 0,<br/>
	"pte"		INTEGER NOT NULL DEFAULT 0<br/>
);
