package com.user.support;

import com.user.Main;
import com.user.exceptions.PRException;

import java.io.*;
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
     * Получение конфигурации в виде ключ-значение, используя "внутренний" properties файл
     * @param propFile путь к .properties файлу
     * @return HashMap параметров конфигурации
     * @throws IOException невозможно найти файл конфигурации или проблемы с InputStream
     */
    public static HashMap<String, String> readPropertiesFile(String propFile) throws IOException {
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream(propFile)) {
            Properties prop = new Properties();
            HashMap<String, String> kvConfig = new HashMap<>();

            if (input == null) throw new IOException("Невозможно найти " + propFile + ", выход из программы");

            prop.load(input);

            prop.forEach((k, v) -> kvConfig.put(k.toString(), v.toString()));
            return kvConfig;
        }
    }

    /**
     * Получение конфигурации в виде ключ-значение, используя "внешний" properties файл
     * @param propFile путь к .properties файлу
     * @return HashMap параметров конфигурации
     * @throws IOException невозможно найти файл конфигурации или проблемы с InputStream
     */
    public static HashMap<String, String> readExternalPropertiesFile(String propFile) throws IOException {
        try (InputStreamReader input = new InputStreamReader(new FileInputStream(propFile))) {
            Properties prop = new Properties();
            HashMap<String, String> kvConfig = new HashMap<>();

            prop.load(input);

            prop.forEach((k, v) -> kvConfig.put(k.toString(), v.toString()));
            return kvConfig;
        }
    }

    /**
     * Получение значения параметра из конфигурации
     * @param config key-value конфигурация
     * @param key ключ, для которого необходимо получить значение
     * @return значение ключа
     * @throws PRException если для ключа не найдено значение или его не существует
     */
    public static String getValueByKey(HashMap<String, String> config, String key) throws PRException {
        // @MARK:LOG
        Utils.log(Utils.logLevels.DEBUG, "getValueByKey " + key);

        String value = config.get(key);
        if(value == null) throw new PRException("Не задано значение driver в файле конфигурации");
        return value;
    }
}
