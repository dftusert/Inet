package com.user.support;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Класс работы с конфигурацией
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Config {
    /**
     * Получение конфигурации из файла
     * @param configDir путь к директории с конфигурационными файлами
     * @param propFileName имя конфигурационного файла
     * @return конфигурация
     * @throws IOException ошибки с чтением или существованием файла
     */
    public static HashMap<String, String> readConfig(String configDir, String propFileName) throws IOException {
        try {
            Log.log(Log.levels.DEBUG, "START Config.readConfig(configDir:" + configDir + ", propFileName:" + propFileName + ')');
            File propFile = new File(configDir + File.separator + propFileName + ".properties");
            try (InputStreamReader input = new InputStreamReader(new FileInputStream(propFile))) {
                Properties prop = new Properties();
                HashMap<String, String> kvConfig = new HashMap<>();

                prop.load(input);

                prop.forEach((k, v) -> kvConfig.put(k.toString(), v.toString()));
                Log.log(Log.levels.DEBUG, "RSTOP Config.readConfig");
                return kvConfig;
            }
        } catch (IOException ex) {
            throw new IOException("Ошибка чтения конфигурации: " + configDir + File.separator + propFileName + ".properties");
        }
    }

    /**
     * Получение значения по ключу из конфигурации
     * @param config конфигурация
     * @param key ключ
     * @return значение по ключу
     * @throws InvalidParameterException невозможно найти значение для ключа, возможно ключ не существует
     */
    public static String getValueByKey(HashMap<String, String> config, String key) throws InvalidParameterException {
        Log.log(Log.levels.DEBUG, "START Config.getValueByKey(config:" + config.toString() + ", key:" + key + ')');
        String value = config.get(key);
        if(value == null) throw new InvalidParameterException("Ошибка получения значения в конфигурации по ключу " + key + ", значение равно null");
        Log.log(Log.levels.DEBUG, "RSTOP Config.getValueByKey");
        return value;
    }
}
