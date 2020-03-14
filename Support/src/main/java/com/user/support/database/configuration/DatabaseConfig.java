package com.user.support.database.configuration;

public interface DatabaseConfig {
    String TABLE_LEVELS = "levels";
    String COLUMN_LEVELS_ID = "id";
    String COLUMN_LEVELS_LEVEL_NAME = "level_name";

    String TABLE_MESSAGES = "messages";
    String COLUMN_MESSAGES_ID = "id";
    String COLUMN_MESSAGES_LEVEL_ID = "level_id";
    String COLUMN_MESSAGES_MESSAGE = "message";
    String COLUMN_MESSAGES_TIMESTAMP = "timestamp";

    String TABLE_TRACEBACKS = "tracebacks";
    String COLUMN_TRACEBACKS_ID = "id";
    String COLUMN_TRACEBACKS_MESSAGE_ID = "message_id";
    String COLUMN_TRACEBACKS_TRACEBACK = "traceback";

    String QUERY_GET_LEVEL_BY_LEVEL_NAME = "select " + COLUMN_LEVELS_ID + ", " +
                                                       COLUMN_LEVELS_LEVEL_NAME +
                                           " from " + TABLE_LEVELS +
                                           " where " + COLUMN_LEVELS_LEVEL_NAME + "=?";

    String QUERY_GET_LEVEL_BY_ID = "select " + COLUMN_LEVELS_ID + ", " +
                                               COLUMN_LEVELS_LEVEL_NAME +
                                   " from " + TABLE_LEVELS +
                                   " where " + COLUMN_LEVELS_ID + "=?";

    String QUERY_GET_MESSAGE_BY_ID = "select " + COLUMN_MESSAGES_ID + ", " +
                                                 COLUMN_MESSAGES_LEVEL_ID + ", "  +
                                                 COLUMN_MESSAGES_MESSAGE + ", " +
                                                 COLUMN_MESSAGES_TIMESTAMP +
                                     " from " + TABLE_MESSAGES +
                                     " where "  + COLUMN_MESSAGES_ID + "=?";

    String QUERY_GET_INSERTED_MESSAGE_BY_MESSAGE = "select " + COLUMN_MESSAGES_ID + ", " +
                                                               COLUMN_MESSAGES_LEVEL_ID + ", " +
                                                               COLUMN_MESSAGES_MESSAGE + ", " +
                                                               COLUMN_MESSAGES_TIMESTAMP +
                                                   " from " + TABLE_MESSAGES +
                                                   " where " + COLUMN_MESSAGES_LEVEL_ID + "=? and " +
                                                               COLUMN_MESSAGES_MESSAGE + "=? and " +
                                                               COLUMN_MESSAGES_TIMESTAMP + "=?";

    String QUERY_INSERT_MESSAGE = "insert into " + TABLE_MESSAGES + "(" + COLUMN_MESSAGES_LEVEL_ID + ", " +
                                                                          COLUMN_MESSAGES_MESSAGE + ", " +
                                                                          COLUMN_MESSAGES_TIMESTAMP + ") values (?,?,?)";

    String QUERY_GET_INSERTED_TRACEBACK_BY_TRACEBACK = "select " + COLUMN_TRACEBACKS_ID + ", " +
                                                                   COLUMN_TRACEBACKS_MESSAGE_ID + ", " +
                                                                   COLUMN_TRACEBACKS_TRACEBACK +
                                                       " from " + TABLE_TRACEBACKS +
                                                       " where " + COLUMN_TRACEBACKS_MESSAGE_ID + "=? and " +
                                                                   COLUMN_TRACEBACKS_TRACEBACK + "=?";

    String QUERY_INSERT_TRACEBACK = "insert into " + TABLE_TRACEBACKS + "(" + COLUMN_TRACEBACKS_MESSAGE_ID + ", " +
                                                                              COLUMN_TRACEBACKS_TRACEBACK + ") values(?,?);";

}
