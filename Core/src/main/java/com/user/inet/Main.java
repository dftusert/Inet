package com.user.inet;

import com.user.inet.conf.Conf;
import com.user.inet.core.database.Database;
import com.user.inet.core.webdriver.collector.CollectInstInfo;
import com.user.inet.core.webdriver.collector.CollectInstPhotos;
import com.user.inet.core.webdriver.collector.Collector;
import com.user.inet.core.webdriver.driver.BaseDriver;
import com.user.inet.core.webdriver.driver.DriverInitializer;
import com.user.support.Config;
import com.user.support.Log;

import java.util.ArrayList;

public class Main {
    /**
     * Entry Point
     * @param args аргументы
     */
    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("ERROR: Отсутствует аргумент для директории конфигурационных файлов");
            return;
        }
        try {
            Log.getInstance().setLogConfig(Config.readConfig(args[0], "log"));
            try (Database db = new Database(new Conf(Config.readConfig(args[0], "database")));
                 BaseDriver driver = DriverInitializer.initialize(new Conf(Config.readConfig(args[0], "webdriver")))) {

                ArrayList<Collector> collectors = new ArrayList<>();
                collectors.add(new CollectInstInfo(driver, db));
                collectors.add(new CollectInstPhotos(driver, db));
                for (Collector collector : collectors) collector.collect();
            }
        } catch (Exception ex) {
            Log.log(Log.levels.CRITICAL, ex.getMessage());
        }
    }
}
