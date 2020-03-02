package com.user.core;

import com.user.core.dbdesc.Profile;
import com.user.core.info.BaseInfo;
import com.user.exceptions.PRException;
import com.user.support.Config;
import com.user.support.Utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Основной класс
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Core {
    /**
     * Сбор данных с профилей
     * @param logFile путь к файлу для логов или null, если нужно выводить логи в терминал
     * @param databaseProperties properties-файл для базы данных
     * @param seleniumProperties properties-файл для драйвера
     * @param isPropertyFilesIsExternal true - "внешний" properties-файл, false - "внутренний"
     */
    public static void collect(String logFile, String databaseProperties, String seleniumProperties, boolean isPropertyFilesIsExternal) {
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "Запуск основного кода сборки информации о профилях в instagram");

        HashMap<String, String> databaseConfiguration, driverConfiguration;
        // ....
        if(logFile != null) Utils.logFile = logFile;

        try {
            if (isPropertyFilesIsExternal) {
                databaseConfiguration = Config.readExternalPropertiesFile(databaseProperties);
                driverConfiguration = Config.readExternalPropertiesFile(seleniumProperties);
            } else {
                databaseConfiguration = Config.readPropertiesFile(databaseProperties);
                driverConfiguration = Config.readPropertiesFile(seleniumProperties);
            }

            try (Database db = new Database(databaseConfiguration)) {
                Driver driver = new Driver(driverConfiguration);

                ArrayList<Profile> profiles = db.getProfiles();
                ArrayList<BaseInfo> baseInfo = driver.collectBaseInfo(profiles);

                BaseInfo baseInfoFromDb;
                for (BaseInfo info : baseInfo) {
                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "id в базе: " + info.getProfile().getId());
                    Utils.log(Utils.logLevels.INFO, "профиль: " + info.getProfile().getPerson());
                    Utils.log(Utils.logLevels.INFO, "публикаций: " + info.getPublicationsCount());
                    Utils.log(Utils.logLevels.INFO, "подписчиков: " + info.getSubscribersCount());
                    Utils.log(Utils.logLevels.INFO, "подписок: " + info.getSubscriptionsCount());

                    baseInfoFromDb = db.getLatestProfileInfo(info.getProfile());
                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "Информация не поменялась: " + info.infoEquals(baseInfoFromDb));
                    Utils.log(Utils.logLevels.INFO, "Аватар не поменялся: " + info.avatarEquals(baseInfoFromDb));

                    if(db.noInfoForProfile(info.getProfile()) || !info.infoEquals(baseInfoFromDb))
                        // @MARK:LOG
                        Utils.log(Utils.logLevels.INFO, "Информация добавлена: " + db.insertIntoInfo(info));
                    if(db.noAvatarForProfile(info.getProfile()) || !info.avatarEquals(baseInfoFromDb))
                        // @MARK:LOG
                        Utils.log(Utils.logLevels.INFO, "Аватар добавлен: " + db.insertIntoAvatar(info));
                }
            } catch (IOException | SQLException | PRException ex) {
                // @MARK:LOG
                Utils.log(Utils.logLevels.CRITICAL, "Произошла ошибка: " + ex.getMessage());
            }
        } catch(IOException ex) {
            // @MARK:LOG
            Utils.log(Utils.logLevels.CRITICAL, "Произошла ошибка: " + ex.getMessage());
        } finally {
            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "Завершение основного кода сборки информации о профилях в instagram");
            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "");
        }
    }
}
