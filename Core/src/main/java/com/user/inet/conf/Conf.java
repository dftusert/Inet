package com.user.inet.conf;

import com.user.support.Config;

import java.util.HashMap;

/**
 * Класс для хранения конфигурации
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Conf {
    /**
     * Хранение конфигурации (K-V) в виде HashMap
     */
    private HashMap<String, String> config;

    /**
     * Конструктор, инициализация конфигурации
     * @param config конфигурация
     */
    public Conf(HashMap<String, String> config) {
        this.config = config;
    }

    /**
     * Получение значение параметра из конфигурации по ключу
     * @param key ключ
     * @return значение
     */
    public String get(String key) {
        return Config.getValueByKey(config, key);
    }

    /**
     * Реализация toString
     * @return строка
     */
    @Override
    public String toString() {
        return config.toString();
    }
}
