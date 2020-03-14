package com.user.inet.core.webdriver.driver;

import com.user.inet.conf.Conf;

/**
 * Класс получения конкретного драйвера на основе конфигурации
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class DriverInitializer {
    /**
     * Получение конкретного драйвера на основе конфигурации
     * @param conf конфигурация
     * @return конкретный драйвер
     * @throws Error ошибка в конфигурации
     */
    public static BaseDriver initialize(Conf conf)throws Error {
        String driver = conf.get("driver");
        if (driver.equals("firefox")) return new FirefoxWebDriver(conf);
        else if(driver.equals("chrome")) return new ChromeWebDriver(conf);
        else throw new Error("Неправильно задан драйвер в конфигурационном файле");
    }
}
