package com.user.core;

import com.user.core.dbdesc.Profile;
import com.user.core.info.BaseInfo;
import com.user.exceptions.PRException;
import com.user.support.Config;
import com.user.support.Utils;
import org.sqlite.JDBC;

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
    Connection connection;
    HashMap<String, String> databaseConfiguration;

    /**
     * Конструктор
     * @param config параметры, информация о таблицах базы данных (конфигурация)
     * @throws PRException не найдено значение ключа в конфигурации
     * @throws SQLException ошибка соединения, регистрации драйвера
     */
    Database(HashMap<String, String> config) throws PRException, SQLException {
        this.databaseConfiguration = config;
        DriverManager.registerDriver(new JDBC());

        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--Создание соединения с базой данных " + "jdbc:sqlite:" +
                Config.getValueByKey(databaseConfiguration, "database-file"));
        connection = DriverManager.getConnection("jdbc:sqlite:" +
                                                    Config.getValueByKey(databaseConfiguration, "database-file"));
    }

    /**
     * Получение всех профилей из таблицы Profile
     * @return список всех профилей
     * @throws PRException  если для ключа не найдено значение или его не существует
     * @throws SQLException ошибка при выполнении запроса
     */
    public ArrayList<Profile> getProfiles() throws PRException, SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--getProfiles() START");

        String profilesQuery = Config.getValueByKey(databaseConfiguration, "all-profiles");
        String idColumn = Config.getValueByKey(databaseConfiguration, "all-profiles--id");
        String personColumn = Config.getValueByKey(databaseConfiguration, "all-profiles--person");

        ArrayList<Profile> profiles = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(profilesQuery)) {
            while (result.next())
                profiles.add(new Profile(result.getInt(idColumn), result.getString(personColumn)));

        }

        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--getProfiles() END");
        return profiles;
    }

    /**
     * Проверка на отсутствие сохраненных в базе аватаров
     * @param profile профиль пользователя
     * @return true - аватаров пользователя в базе не найдено, false - найден хотя бы один аватар
     * @throws PRException ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean noAvatarForProfile(Profile profile) throws PRException, SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--noAvatarForProfile() START");

        int count = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(Config.getValueByKey(databaseConfiguration, "avatar-count-for-profile"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) count = result.getInt("id");
                if (count == -1) throw new PRException("Ошибка выполнения запроса: " + pstmt.toString());
            }
        }

        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--noAvatarForProfile() END");
        return count == 0;
    }

    /**
     * Проверка на отсутствие информации о пользователе в базе данных
     * @param profile профиль пользователя
     * @return true - информации о пользователе в базе не найдено, false - найдена хотя бы одна запись
     * @throws PRException ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean noInfoForProfile(Profile profile) throws PRException, SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--noInfoForProfile() START");

        int count = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(Config.getValueByKey(databaseConfiguration, "info-count-for-profile"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) count = result.getInt("id");
                if (count == -1) throw new PRException("Ошибка выполнения запроса: " + pstmt.toString());
            }
        }

        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--noInfoForProfile() END");
        return count == 0;
    }

    /**
     * Получение последней базовой информации по профилю
     * @param profile профиль пользователя
     * @return последняя базовая информация по пользователю из базы данных
     * @throws PRException ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public BaseInfo getLatestProfileInfo(Profile profile) throws PRException, SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--getLatestProfileInfo() START");

        int publicationsCount = -1, subscribersCount = -1, subscriptionsCount= -1;
        byte[] avatar = null;
        String avatarType = null;

        try (PreparedStatement pstmt = connection.prepareStatement(Config.getValueByKey(databaseConfiguration, "info-latest-profile-info"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    publicationsCount = result.getInt("publications_count");
                    subscribersCount = result.getInt("subscribers_count");
                    subscriptionsCount = result.getInt("subscriptions_count");
                }
            }
        }

        try (PreparedStatement pstmt = connection.prepareStatement(Config.getValueByKey(databaseConfiguration, "avatar-latest-profile-avatar"))) {
            pstmt.setInt(1, profile.getId());
            try(ResultSet result = pstmt.executeQuery()) {
                while (result.next()) {
                    avatar = result.getBytes("data");
                    avatarType = result.getString("type");
                }
            }
        }
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--getLatestProfileInfo() END");
        return new BaseInfo(profile, publicationsCount, subscribersCount, subscriptionsCount, avatarType, avatar);
    }

    /**
     * Вставка данных в таблицу базовой информации о пользователе
     * @param baseInfo полученная информация о пользователе
     * @return true - вставка прошла успешно, false - встава закончилась ошибкой
     * @throws PRException ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean insertIntoInfo(BaseInfo baseInfo) throws PRException, SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--insertIntoInfo() START");

        try (PreparedStatement pstmt = connection.prepareStatement(Config.getValueByKey(databaseConfiguration, "info-insert-data"))) {
            pstmt.setInt(1, baseInfo.getProfile().getId());
            pstmt.setInt(2, baseInfo.getPublicationsCount());
            pstmt.setInt(3, baseInfo.getSubscribersCount());
            pstmt.setInt(4, baseInfo.getSubscriptionsCount());
            pstmt.setString(5, Utils.getDbTimeMark());

            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "DB--insertIntoInfo() END");
            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Вставка аватара в таблицу аватаров пользователя
     * @param baseInfo полученная информация о пользователе (в ней нужен аватар и его тип)
     * @return true - вставка прошла успешно, false - встава закончилась ошибкой
     * @throws PRException ошибка чтения конфигурации
     * @throws SQLException ошибка выполнения запроса
     */
    public boolean insertIntoAvatar(BaseInfo baseInfo) throws PRException, SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--insertIntoAvatar() START");

        try (PreparedStatement pstmt = connection.prepareStatement(Config.getValueByKey(databaseConfiguration, "avatar-insert-data"))) {
            pstmt.setInt(1, baseInfo.getProfile().getId());
            pstmt.setString(2, baseInfo.getAvatarType());
            pstmt.setBytes(3, baseInfo.getAvatar());
            pstmt.setString(4, Utils.getDbTimeMark());

            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "DB--insertIntoAvatar() END");
            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Реализация close из AutoCloseable
     * @throws SQLException ошибка закрытия соединения
     */
    @Override
    public void close() throws SQLException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "DB--Закрытие соединения с базой данных");
        connection.close();
    }
}
