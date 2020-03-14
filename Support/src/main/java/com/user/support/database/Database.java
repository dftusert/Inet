package com.user.support.database;

import com.user.support.Log;
import com.user.support.Utils;
import com.user.support.database.configuration.DatabaseConfig;
import com.user.support.database.entity.Levels;
import com.user.support.database.entity.Messages;
import com.user.support.database.entity.Tracebacks;
import org.sqlite.JDBC;

import java.sql.*;
import java.util.HashMap;

/**
 * Класс работы с базой данных
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Database implements AutoCloseable, DatabaseConfig {
    /**
     * connection соединение с базой данных
     * databaseFile имя файла базы данных
     */
    Connection connection;
    String databaseFile;

    /**
     * Конструктор, в нем устанавливается соединение с базой данных
     * @param databaseFile имя файла базы данных
     * @throws Error не найдено значение ключа в конфигурации
     * @throws SQLException ошибка соединения, регистрации драйвера
     */
    public Database(String databaseFile) throws Error, SQLException {
        DriverManager.registerDriver(new JDBC());
        this.databaseFile = databaseFile;
        connection = DriverManager.getConnection("jdbc:sqlite:" + this.databaseFile);
    }

    /**
     * Получение уровня по его названию
     * @param levelName название уровня
     * @return уровень
     * @throws SQLException ошибка запроса
     */
    public Levels getLevelByLevelName(String levelName) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_GET_LEVEL_BY_LEVEL_NAME)) {
            pstmt.setString(1, levelName);
            try(ResultSet result = pstmt.executeQuery()) {
                if (result.next()) return new Levels(result.getInt(COLUMN_LEVELS_ID), result.getString(COLUMN_LEVELS_LEVEL_NAME));
                return null;
            }
        }
    }

    /**
     * Получение уровня по его id
     * @param id идентификатор уровня
     * @return уровень
     * @throws SQLException ошибка запроса
     */
    public Levels getLevelById(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_GET_LEVEL_BY_ID)) {
            pstmt.setInt(1, id);
            try(ResultSet result = pstmt.executeQuery()) {
                if (result.next()) return new Levels(result.getInt(COLUMN_LEVELS_ID), result.getString(COLUMN_LEVELS_LEVEL_NAME));
                return null;
            }
        }
    }

    /**
     * Получение сообщения по его id
     * @param id идентификатор сообщения
     * @return сообщение
     * @throws SQLException ошибка запроса
     */
    public Messages getMessageById(int id) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_GET_MESSAGE_BY_ID)) {
            pstmt.setInt(1, id);
            try(ResultSet result = pstmt.executeQuery()) {
                if (result.next()) return new Messages(result.getInt(COLUMN_MESSAGES_ID), getLevelById(result.getInt(COLUMN_MESSAGES_LEVEL_ID)),
                        result.getString(COLUMN_MESSAGES_MESSAGE), result.getString(COLUMN_MESSAGES_TIMESTAMP));
                return null;
            }
        }
    }

    /**
     * Получение вставленного сообщение по вставленным данным (по всем данным кроме id т.к. его не знаем)
     * @param message сообщение
     * @return сообщение с найденым id
     * @throws SQLException ошибка запроса
     */
    public Messages getInsertedMessageByMessage(Messages message) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_GET_INSERTED_MESSAGE_BY_MESSAGE)) {
            pstmt.setInt(1, message.getLevel().getId());
            pstmt.setString(2, message.getMessage());
            pstmt.setString(3, message.getTimestamp());

            try(ResultSet result = pstmt.executeQuery()) {
                if (result.next()) return new Messages(result.getInt(COLUMN_MESSAGES_ID), getLevelById(result.getInt(COLUMN_MESSAGES_LEVEL_ID)),
                        result.getString(COLUMN_MESSAGES_MESSAGE), result.getString(COLUMN_MESSAGES_TIMESTAMP));
                return null;
            }
        }
    }

    /**
     * Вставка сообщения в таблицу и получение его из таблицы с вытащенным оттуда его id
     * @param message сообщение
     * @return вставленное сообщение с корректным id
     * @throws SQLException ошибка запроса
     * @throws Error ошибка вставки строки в таблицу
     */
    public Messages insertMessageAndReturnItInserted(Messages message) throws SQLException, Error {
        if(!insertMessage(message)) throw new Error("Не удалось вставить сообщение " + message.toString());
        return getInsertedMessageByMessage(message);
    }

    /**
     * Вставка сообщения в таблицу Messages
     * @param message сообщение
     * @return true - сообщение успешно вставлено, false - произошли ошибки
     * @throws SQLException ошибка запроса
     */
    public boolean insertMessage(Messages message) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_INSERT_MESSAGE)) {
            pstmt.setInt(1, message.getLevel().getId());
            pstmt.setString(2, message.getMessage());
            pstmt.setString(3, message.getTimestamp());

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Вставка traceback в таблицу и получение его из таблицы с вытащенным оттуда его id
     * @param traceback traceback для сообщения message
     * @return вставленный traceback с корректным id
     * @throws SQLException ошибка запроса
     */
    public Tracebacks insertTracebackAndReturnItInserted(Tracebacks traceback) throws SQLException {
        if(!insertTraceback(traceback)) throw new Error("Не удалось вставить traceback " + traceback.toString());
        return getInsertedTracebackByTraceback(traceback);
    }

    /**
     * Вставка traceback в таблицу Tracebacks
     * @param traceback traceback для сообщения message
     * @return true - traceback успешно вставлен, false - произошли ошибки
     * @throws SQLException ошибка запроса
     */
    public boolean insertTraceback(Tracebacks traceback) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_INSERT_TRACEBACK)) {
            pstmt.setInt(1, traceback.getMessage().getId());
            pstmt.setString(2, traceback.getTraceback());

            return pstmt.executeUpdate() == 1;
        }
    }

    /**
     * Получение вставленного traceback по вставленным данным (по всем данным кроме id т.к. его не знаем)
     * @param traceback traceback для сообщения message
     * @return traceback с найденым id
     * @throws SQLException ошибка запроса
     */
    public Tracebacks getInsertedTracebackByTraceback(Tracebacks traceback) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(QUERY_GET_INSERTED_TRACEBACK_BY_TRACEBACK)) {
            pstmt.setInt(1, traceback.getMessage().getId());
            pstmt.setString(2, traceback.getTraceback());

            try(ResultSet result = pstmt.executeQuery()) {
                if (result.next()) return new Tracebacks(result.getInt(COLUMN_TRACEBACKS_ID), getMessageById(result.getInt(COLUMN_TRACEBACKS_MESSAGE_ID)),
                        result.getString(COLUMN_TRACEBACKS_TRACEBACK));
                return null;
            }
        }
    }

    /**
     * Получение имени файла базы данных
     * @return имя файла базы данных
     */
    public String getDatabaseFile() { return databaseFile; }

    /**
     * Реализация toString
     * @return строка
     */
    @Override
    public String toString() {
        return "Соединение: " + connection.toString() + ", файл базы данных: " + databaseFile;
    }

    /**
     * Реализация close из AutoCloseable
     * @throws SQLException ошибка закрытия соединения
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
