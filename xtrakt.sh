#!/bin/bash
DEBUG=1
SQLITE=sqlite3

AVATAR_TABLE=AVATAR
PHOTOS_TABLE=PHOTOS
AVATAR_DIR=avatars
PHOTOS_DIR=photos

function log() {
	if [ -n "$DEBUG" ]; then
		if [[ -v 1 ]]; then
			echo "Отсутствует сообщение для логгирования"
			exit 10
		else
			echo "LOG: $1"
		fi
	fi
}

function showUsage() {
	echo "***********************************************************************************************************************************"
	echo "* Для запуска используйте ${0} GLOBAL_OUTPUT_DIRECTORY DATABASE_FILE TABLE [PROFILE_ID] PICTURE_ID, где:"
	echo "* GLOBAL_OUTPUT_DIRECTORY - директория для фотографий с директориями avatars и photos"
	echo "* DATABASE_FILE - путь к файлу с базой данных,"
	echo "* TABLE - название таблицы - $AVATAR_TABLE или $PHOTOS_TABLE,"
	echo "* PROFILE_ID - id профиля пользователя из таблицы profile в базе данных, аргумент можно опустить,"
	echo "* PICTURE_ID - id фотографии или аватара в этой таблице или ALL чтобы получить все фотографии"
	echo "***********************************************************************************************************************************"
}

function checkRequirements() {
	if [ -x "$(command -v ${SQLITE})" ]; then
		echo "sqlite3......OK"
	else
		echo "sqlite3......NOT FOUND"
		exit 21
	fi
}

function initVar() {
	if [[ -v 1 ]]; then
		echo "Ошибка инициализации"
		exit 200
	fi
	if [[ -v 2 ]]; then
		echo "$3"
		showUsage
		exit 2
	else
		local var="$1"
    	eval $var="$2"
	fi
}

function initVars() {
	echo "Проверка количества аргументов"
	if [ "$#" -ne 4 ] && [ "$#" -ne 5 ]; then
		echo "Некорректное число аргументов"
		showUsage
		exit 1
	fi

	echo "Инициализация GLOBAL_OUTPUT_DIRECTORY"
	initVar GLOBAL_OUTPUT_DIRECTORY "$1" "Аргумент GLOBAL_OUTPUT_DIRECTORY не задан"
	echo "$GLOBAL_OUTPUT_DIRECTORY"

	echo "Инициализация DATABASE_FILE"
	initVar DATABASE_FILE "$2" "DATABASE_FILE не задан"

	echo "Инициализация TABLE"
	initVar TABLE "$3" "TABLE не задан"

	if [ "$TABLE" != "$AVATAR_TABLE" ] && [ "$TABLE" != "$PHOTOS_TABLE" ]; then
		echo "Аргумент TABLE задан неверно"
		showUsage
		exit 3
	fi

	if [ "$#" -eq 5 ]; then
		echo "Инициализация PROFILE_ID"
		initVar PROFILE_ID "$4" "PROFILE_ID не задан"
	fi

	if [ "$#" -eq 4 ]; then
		echo "Инициализация PICTURE_ID"
		initVar PICTURE_ID "$4" "PICTURE_ID не задан"
	fi

	if [ "$#" -eq 5 ]; then
		echo "Инициализация PICTURE_ID"
		initVar PICTURE_ID "$5" "PICTURE_ID не задан"
	fi

	echo "Получение списка PICTURE_IDS"
	if [ "$PICTURE_ID" == "ALL" ]; then
		if [ "$TABLE" == "$AVATAR_TABLE" ]; then
			if [[ -v PROFILE_ID ]]; then
				PICTURE_IDS=`"$SQLITE" "$DATABASE_FILE" "select a.id from avatar a where a.profile_id=$PROFILE_ID"`
			else
				PICTURE_IDS=`"$SQLITE" "$DATABASE_FILE" "select a.id from avatar a"`
			fi
		else
			if [[ -v PROFILE_ID ]]; then
				PICTURE_IDS=`"$SQLITE" "$DATABASE_FILE" "select p.id from photos p where p.profile_id=$PROFILE_ID"`
			else
				PICTURE_IDS=`"$SQLITE" "$DATABASE_FILE" "select p.id from photos p"`
			fi
		fi
	else
		if [[ -v PROFILE_ID ]]; then
			if [ "$TABLE" == "$AVATAR_TABLE" ]; then
				PICTURE_IDS=`"$SQLITE" "$DATABASE_FILE" "select a.id from avatar a where a.profile_id=$PROFILE_ID and p.id=$PICTURE_ID"`
			else
				echo $([ -z ${asdd+"err"} ])
				sql="select p.id from photos p where p.profile_id=${PROFILE_ID} and p.id=${PICTURE_ID}"
				echo "$sql"
				PICTURE_IDS=$("$SQLITE" "$DATABASE_FILE" "$sql")
			fi
		else
			PICTURE_IDS=$PICTURE_ID
		fi
	fi

	echo "Получение поддиректории"
	if [ "$TABLE" == "$AVATAR_TABLE" ]; then
		SUB_DIR="$AVATAR_DIR"
		FILE_PREFIX="ava"
	else
		SUB_DIR="$PHOTOS_DIR"
		FILE_PREFIX="pho"
	fi
}

function showVars() {
	log "START showVars $*"
	echo "Будут использованы следующие переменные:"
	echo "GLOBAL_OUTPUT_DIRECTORY=${GLOBAL_OUTPUT_DIRECTORY}"
	echo "DATABASE_FILE=${DATABASE_FILE}"
	echo "TABLE=${TABLE}"
	echo "PROFILE_ID=${PROFILE_ID}"
	echo "PICTURE_ID=${PICTURE_ID}"
	log "STOP showVars $*"
}

function getSQLQuery() {
	if [[ -v 1 ]]; then
		echo "Не задан аругмент для getSQLQuery"
		exit 30
	fi
	if [ "$TABLE" == "AVATAR" ]; then
		echo "select quote(a.data) from avatar a where a.id=$1"
	elif [ "$TABLE" == "PHOTOS" ]; then
		echo "select quote(p.photo) from photos p where p.id=$1"
	else
		echo "Не найдена таблица $TABLE"
		showUsage
		exit 31
	fi
}

function getSQLQueryExtension() {
	if [[ -v 1 ]]; then
		echo "Не задан аругмент для getSQLQueryExtension"
		exit 30
	fi
	if [ "$TABLE" == "AVATAR" ]; then
		echo "select a.type from avatar a where a.id=$1"
	elif [ "$TABLE" == "PHOTOS" ]; then
		echo "select p.type from photos p where p.id=$1"
	else
		echo "Не найдена таблица $TABLE"
		showUsage
		exit 31
	fi
}

function getSQLQueryTime() {
	if [[ -v 1 ]]; then
		echo "Не задан аругмент для getSQLQueryExtension"
		exit 30
	fi
	if [ "$TABLE" == "AVATAR" ]; then
		echo "select a.firstseen from avatar a where a.id=$1"
	elif [ "$TABLE" == "PHOTOS" ]; then
		echo "select p.added_at from photos p where p.id=$1"
	else
		echo "Не найдена таблица $TABLE"
		showUsage
		exit 31
	fi
}

function getSQLQueryProfile() {
	if [[ -v 1 ]]; then
		echo "Не задан аругмент для getSQLQueryExtension"
		exit 30
	fi
	if [ "$TABLE" == "AVATAR" ]; then
		echo "select p.person from avatar a, profile p where a.id=$1 and a.profile_id=p.id"
	elif [ "$TABLE" == "PHOTOS" ]; then
		echo "select p.person from photos ph, profile p where ph.id=$1 and ph.profile_id=p.id"
	else
		echo "Не найдена таблица $TABLE"
		showUsage
		exit 31
	fi
}

echo "Запуск скрипта $*"
if [ "$#" -eq 0 ]; then
	showUsage
	exit 0
fi

checkRequirements
initVars $*
showVars

mkdir -p "${GLOBAL_OUTPUT_DIRECTORY}"

for id in $PICTURE_IDS
do
	echo "Получение PICTURE_ID=$id из таблицы $TABLE"
	profile=$("$SQLITE" "$DATABASE_FILE" "$(getSQLQueryProfile "$id")")
	echo "Фотография принадлежит $profile"
	mkdir -p "${GLOBAL_OUTPUT_DIRECTORY}/${profile}/${SUB_DIR}"

	extension=$("$SQLITE" "$DATABASE_FILE" "$(getSQLQueryExtension "$id")")
	echo "Расширение фотографии $extension"

	timemark=$(echo -e "$("$SQLITE" "$DATABASE_FILE" "$(getSQLQueryTime "$id")")" | tr -d '[:space:]:-')

	sql=$(getSQLQuery "$id")
	echo "Выполняется запрос $sql"
	echo "Файл будет сохранен как ${GLOBAL_OUTPUT_DIRECTORY}/${profile}/${SUB_DIR}/${FILE_PRFIX}${id}.${timemark}.${extension}"

	code=$("$SQLITE" "$DATABASE_FILE" "$sql" | cut -d "'" -f2 | xxd -r -p > "${GLOBAL_OUTPUT_DIRECTORY}/${profile}/${SUB_DIR}/${FILE_PREFIX}${id}.${timemark}.${extension}")
	echo "Запрос завершился ($code), ${GLOBAL_OUTPUT_DIRECTORY}/${SUB_DIR}/${profile}/${id}.${extension}"
done
echo "Завершение скрипта $*"