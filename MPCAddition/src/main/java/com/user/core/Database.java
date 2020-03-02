package com.user.core;

import com.user.core.db.Photos;
import com.user.core.db.Profile;
import com.user.support.Config;
import com.user.support.Log;
import com.user.support.Utils;
import org.sqlite.JDBC;

import java.security.InvalidParameterException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Класс работы с базой данных
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Database implements AutoCloseable {
    /**
     * connection соединение с базой данных
     * databaseConfiguration параметры, информация о таблицах базы данных
     */
    private Connection connection;
    private HashMap<String, String> databaseConfiguration;

    /**
     * Конструктор
     * @param config параметры, информация о таблицах базы данных (конфигурация)
     * @throws InvalidParameterException не найдено значение ключа в конфигурации
     * @throws SQLException ошибка соединения, регистрации драйвера
     */
    Database(HashMap<String, String> config) throws InvalidParameterException, SQLException {
        Log.log(Log.levels.DEBUG, "Database(config:" + config.toString() + ')');
        this.databaseConfiguration = config;
        DriverManager.registerDriver(new JDBC());

        connection = DriverManager.getConnection("jdbc:sqlite:" + Config.getValueByKey(databaseConfiguration, "file"));
    }

    /**
     * Получение профилей из таблицы Profile, отмеченных как необходимые для загрузки дополнительных фотографий
     * @return список всех профилей
     * @throws InvalidParameterException если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public ArrayList<Profile> getProfiles() throws InvalidParameterException, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.getProfiles()");
        String profilesQuery = Config.getValueByKey(databaseConfiguration, "get-profiles");
        String idColumn = Config.getValueByKey(databaseConfiguration, "get-profiles--id");
        String personColumn = Config.getValueByKey(databaseConfiguration, "get-profiles--person");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + profilesQuery + ", idColumn " + idColumn + ", personColumn " + personColumn);

        Log.log(Log.levels.INFO, "Получение профилей из таблицы profiles");
        ArrayList<Profile> profiles = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(profilesQuery)) {

            Log.log(Log.levels.DEBUG, "Получено профилей из базы данных: " + result.getFetchSize());
            while (result.next())
                profiles.add(new Profile(result.getInt(idColumn), result.getString(personColumn)));

        }
        Log.log(Log.levels.DEBUG, "RSTOP Database.getProfiles()");
        return profiles;
    }

    /**
     * Сохранение фотографии пользователя в основную или временную таблицу (в зависимости от query)
     * @param query SQL добавления фотографии в нужную таблицу
     * @param profile профиль пользователя
     * @param type расширение изображения
     * @param blob фотография пользователя
     * @return результат вставки
     * @throws InvalidParameterException если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public boolean saveBlobImage(String query, Profile profile, String type, byte[] blob) throws InvalidParameterException, SQLException {
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

            Log.log(Log.levels.DEBUG, "RSTOP Database.saveBlobImage");

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Сохранение фотографии пользователя в основную или временную таблицу (в зависимости от query)
     * @param profile профиль пользователя
     * @return результат вставки
     * @throws InvalidParameterException если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public boolean deletePhotosTemp(Profile profile) throws InvalidParameterException, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.deletePhotosTemp(profile:" + profile.toString() + ')');

        String query = Config.getValueByKey(databaseConfiguration, "clear-profile-photos-temp");
        Log.log(Log.levels.DEBUG, "DBEXEC: " + query);

        Log.log(Log.levels.INFO, "Вставка blob в таблицу запросом " + query);
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            Log.log(Log.levels.DEBUG, "DBEXEC-ADD-PARAM: " + profile.getId());
            pstmt.setInt(1, profile.getId());

            Log.log(Log.levels.DEBUG, "RSTOP Database.deletePhotosTemp");

            return pstmt.executeUpdate() >= 1;
        }
    }

    /**
     * Получение новых фотографий пользователя (находящихся в таблице photos_temp, но отсутствующих в таблице photos)
     * @param profile профиль пользователя
     * @return массив новых фотографий
     * @throws InvalidParameterException если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public ArrayList<Photos> getDiffPhotos(Profile profile) throws InvalidParameterException, SQLException {
        Log.log(Log.levels.DEBUG, "START Database.getDiffPhotos(profile:" + profile.toString() + ", blob: <--->)");

        String diffQuery = Config.getValueByKey(databaseConfiguration, "get-diff-photos");
        String idColumn = Config.getValueByKey(databaseConfiguration, "get-diff-photos--id");
        String profileIdColumn = Config.getValueByKey(databaseConfiguration, "get-diff-photos--profile_id");
        String typeColumn = Config.getValueByKey(databaseConfiguration, "get-diff-photos--type");
        String photoColumn = Config.getValueByKey(databaseConfiguration, "get-diff-photos--photo");
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

        Log.log(Log.levels.DEBUG, "RSTOP Database.getDiffPhotos");
        return photos;
    }

    /**
     * Получение конфигурации для базы данных
     * @return конфигурация для базы данных
     */
    public HashMap<String, String> getDatabaseConfiguration() { return databaseConfiguration; }

    /**
     * Реализация toString
     */
    @Override
    public String toString() {
        return "Соединение: " + connection.toString() + "; Конфигурация: " + databaseConfiguration.toString();
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
        Log.log(Log.levels.DEBUG, "STOP Database.close()");
    }
}
