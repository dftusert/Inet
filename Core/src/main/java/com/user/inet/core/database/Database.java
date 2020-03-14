package com.user.inet.core.database;

import com.user.inet.conf.Conf;
import com.user.inet.core.database.entity.Photos;
import com.user.inet.core.database.entity.Profile;
import com.user.inet.core.database.infocontainer.BaseInfo;
import com.user.support.Log;
import com.user.support.Utils;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.ArrayList;

/**
 * Класс работы с базой данных
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Database implements AutoCloseable {
    /**
     * connection соединение с базой данных
     * conf параметры, информация о таблицах базы данных
     */
    Connection connection;
    Conf conf;

    /**
     * Конструктор, в нем устанавливается соединение с базой данных
     * @param conf параметры, информация о таблицах базы данных (конфигурация)
     * @throws Error не найдено значение ключа в конфигурации
     * @throws SQLException ошибка соединения, регистрации драйвера
     */
    public Database(Conf conf) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "Database(config:" + conf.toString() + ')');
        this.conf = conf;
        DriverManager.registerDriver(new JDBC());

        connection = DriverManager.getConnection("jdbc:sqlite:" + conf.get("file"));
    }

    /**
     * Получение профилей из таблицы Profile, отмеченных как необходимые для загрузки информации
     * @return список всех профилей
     * @throws Error если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public ArrayList<Profile> getProfilesInfoCollect() throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.getProfilesInfoCollect()");
        String profilesQuery =  conf.get("get-profiles-load-info");
        String idColumn = conf.get("get-profiles--id");
        String personColumn = conf.get("get-profiles--person");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + profilesQuery + ", idColumn " + idColumn + ", personColumn " + personColumn);

        Log.log(Log.levels.INFO, "Получение профилей из таблицы profiles");
        ArrayList<Profile> profiles = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(profilesQuery)) {

            Log.log(Log.levels.DEBUG, "Получено профилей из базы данных: " + result.getFetchSize());
            while (result.next())
                profiles.add(new Profile(result.getInt(idColumn), result.getString(personColumn)));

        }
        return profiles;
    }

    /**
     * Получение профилей из таблицы Profile, отмеченных как необходимые для загрузки дополнительных фотографий
     * @return список всех профилей
     * @throws Error если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public ArrayList<Profile> getProfilesPhotosCollect() throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.getProfilesPhotosCollect()");
        String profilesQuery =  conf.get("get-profiles-load-photos");
        String idColumn = conf.get("get-profiles--id");
        String personColumn = conf.get("get-profiles--person");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + profilesQuery + ", idColumn " + idColumn + ", personColumn " + personColumn);

        Log.log(Log.levels.INFO, "Получение профилей из таблицы profiles");
        ArrayList<Profile> profiles = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(profilesQuery)) {

            Log.log(Log.levels.DEBUG, "Получено профилей из базы данных: " + result.getFetchSize());
            while (result.next())
                profiles.add(new Profile(result.getInt(idColumn), result.getString(personColumn)));
        }
        return profiles;
    }

    /**
     * Проверка на отсутствие сохраненных в базе аватаров для конкретного пользователя
     * @param profile профиль пользователя
     * @return true - аватаров пользователя в базе не найдено, false - найден хотя бы один аватар
     * @throws Error ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean noAvatarForProfile(Profile profile) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.noAvatarForProfile(profile:" + profile.toString() + ")");

        int count = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(conf.get("avatar-count-for-profile"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) count = result.getInt("id");
                if (count == -1) throw new Error("Ошибка выполнения запроса: " + pstmt.toString());
            }
        }

        return count == 0;
    }

    /**
     * Проверка на отсутствие информации о пользователе в базе данных
     * @param profile профиль пользователя
     * @return true - информации о пользователе в базе не найдено, false - найдена хотя бы одна запись
     * @throws Error ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean noInfoForProfile(Profile profile) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.noInfoForProfile(profile:" + profile.toString() + ")");

        int count = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(conf.get("info-count-for-profile"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) count = result.getInt("id");
                if (count == -1) throw new Error("Ошибка выполнения запроса: " + pstmt.toString());
            }
        }

        return count == 0;
    }

    /**
     * Получение последней базовой информации по профилю
     * @param profile профиль пользователя
     * @return последняя базовая информация по пользователю из базы данных
     * @throws Error ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public BaseInfo getLatestProfileInfo(Profile profile) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.getLatestProfileInfo(profile:" + profile.toString() + ")");

        int publicationsCount = -1, subscribersCount = -1, subscriptionsCount= -1;
        byte[] avatar = null;
        String avatarType = null;

        try (PreparedStatement pstmt = connection.prepareStatement(conf.get("info-latest-profile-info"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    publicationsCount = result.getInt("publications_count");
                    subscribersCount = result.getInt("subscribers_count");
                    subscriptionsCount = result.getInt("subscriptions_count");
                }
            }
        }

        try (PreparedStatement pstmt = connection.prepareStatement(conf.get("avatar-latest-profile-avatar"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    avatar = result.getBytes("data");
                    avatarType = result.getString("type");
                }
            }
        }

        return new BaseInfo(profile, publicationsCount, subscribersCount, subscriptionsCount, avatarType, avatar);
    }

    /**
     * Вставка данных в таблицу базовой информации о пользователе
     * @param baseInfo полученная информация о пользователе
     * @return true - вставка прошла успешно, false - встава закончилась ошибкой
     * @throws Error ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean insertIntoInfo(BaseInfo baseInfo) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.insertIntoInfo(baseInfo:" + baseInfo.toString() + ")");

        try (PreparedStatement pstmt = connection.prepareStatement(conf.get("info-insert-data"))) {
            pstmt.setInt(1, baseInfo.getProfile().getId());
            pstmt.setInt(2, baseInfo.getPublicationsCount());
            pstmt.setInt(3, baseInfo.getSubscribersCount());
            pstmt.setInt(4, baseInfo.getSubscriptionsCount());
            pstmt.setString(5, Utils.getDbTimeMark());

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Вставка аватара в таблицу аватаров пользователя
     * @param baseInfo полученная информация о пользователе (в ней нужен аватар и его тип)
     * @return true - вставка прошла успешно, false - встава закончилась ошибкой
     * @throws Error ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean insertIntoAvatar(BaseInfo baseInfo) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.insertIntoAvatar(baseInfo:" + baseInfo.toString() + ")");
        try (PreparedStatement pstmt = connection.prepareStatement(conf.get("avatar-insert-data"))) {
            pstmt.setInt(1, baseInfo.getProfile().getId());
            pstmt.setString(2, baseInfo.getAvatarType());
            pstmt.setBytes(3, baseInfo.getAvatar());
            pstmt.setString(4, Utils.getDbTimeMark());

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Сохранение фотографии пользователя в основную или временную таблицу (в зависимости от query)
     * @param query SQL добавления фотографии в нужную таблицу
     * @param profile профиль пользователя
     * @param type расширение изображения
     * @param blob фотография пользователя
     * @return результат вставки
     * @throws Error если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public boolean saveBlobImage(String query, Profile profile, String type, byte[] blob) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.saveBlobImage(query:" + query + ", type:" + type + ", profile:" + profile.toString() + ", blob: <--->)");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + query);

        Log.log(Log.levels.INFO, "Вставка blob в таблицу запросом " + query);

        String dbTimeMark = Utils.getDbTimeMark();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM: " + profile.getId());
            pstmt.setInt(1, profile.getId());

            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM-TYPE");
            pstmt.setString(2, type);

            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM-BLOB");
            pstmt.setBytes(3, blob);

            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM: " + dbTimeMark);
            pstmt.setString(4, dbTimeMark);

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Сохранение фотографии пользователя в основную или временную таблицу (в зависимости от query)
     * @param profile профиль пользователя
     * @return результат вставки
     * @throws Error если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public boolean deletePhotosTemp(Profile profile) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.deletePhotosTemp(profile:" + profile.toString() + ')');

        String query = conf.get("clear-profile-photos-temp");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + query);

        Log.log(Log.levels.INFO, "Удаление blob из таблицы запросом " + query);
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM: " + profile.getId());
            pstmt.setInt(1, profile.getId());

            return pstmt.executeUpdate() >= 1;
        }
    }

    /**
     * Получение новых фотографий пользователя (находящихся в таблице photos_temp, но отсутствующих в таблице photos)
     * @param profile профиль пользователя
     * @return массив новых фотографий
     * @throws Error если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public ArrayList<Photos> getDiffPhotos(Profile profile) throws Error, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.getDiffPhotos(profile:" + profile.toString() + ", blob: <--->)");

        String diffQuery = conf.get("get-diff-photos");
        String idColumn = conf.get("get-diff-photos--id");
        String profileIdColumn = conf.get("get-diff-photos--profile_id");
        String typeColumn = conf.get("get-diff-photos--type");
        String photoColumn = conf.get("get-diff-photos--photo");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + diffQuery + ", idColumn " + idColumn + ", profileIdColumn " + profileIdColumn + ", photoColumn " + photoColumn);

        Log.log(Log.levels.INFO, "Получение новых фотографий пользователя (не имеющиеся в основной таблице, но имеющиеся во временной таблице) в таблицу запросом " + diffQuery);

        ArrayList<Photos> photos = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(diffQuery)) {
            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM: " + profile.getId());
            pstmt.setInt(1, profile.getId());
            try (ResultSet result = pstmt.executeQuery()) {
                Log.log(Log.levels.DEBUG, "Получено новых фотографий из базы данных: " + result.getFetchSize());
                while (result.next())
                    photos.add(new Photos(result.getInt(idColumn), new Profile(result.getInt(profileIdColumn)), result.getString(typeColumn), result.getBytes(photoColumn)));
            }
        }

        return photos;
    }

    /**
     * Получение конфигурации для базы данных
     * @return конфигурация для базы данных
     */
    public Conf getConf() { return conf; }

    /**
     * Реализация toString
     * @return строка
     */
    @Override
    public String toString() {
        return "Соединение: " + connection.toString() + "; Конфигурация: " + conf.toString();
    }

    /**
     * Реализация close из AutoCloseable
     * @throws SQLException ошибка закрытия соединения
     */
    @Override
    public void close() throws SQLException {
        Log.log(Log.levels.DEBUG, "START Database.close()");
        Log.log(Log.levels.INFO, "Закрытие соединения с базой данных");
        connection.close();
    }
}