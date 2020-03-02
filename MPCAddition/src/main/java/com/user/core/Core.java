package com.user.core;

import com.user.support.Config;
import com.user.support.Log;

/**
 * Основной класс
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Core {
    /**
     * Основной метод
     * @param configDir путь к директории с конфигурационными файлами
     */
    public static void run(String configDir) {
        try {
            Log.getInstance().setLogConfig(Config.readConfig(configDir, "log"));
            Log.log(Log.levels.INFO, "BEGIN Core.run(configDir:" + configDir + ')');

            Log.log(Log.levels.INFO, "Создание драйвера и подключения к базе данных...");
            try(Database db = new Database(Config.readConfig(configDir, "database"))) {
                Driver driver = new Driver(Config.readConfig(configDir, "webdriver"));

                Log.log(Log.levels.INFO, "Запуск сбора фотографий...");
                driver.collectImages(db);
            }
        } catch(Exception ex) {
            Log.log(Log.levels.CRITICAL, ex.getMessage());
        } finally {
            Log.log(Log.levels.INFO, "STOP Core.run");
        }
    }
}
