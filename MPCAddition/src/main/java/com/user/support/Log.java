package com.user.support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Класс для логгирования
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Log {
    /**
     * logInstance экземпляр класса для логгирования
     * levels уровни логгирования
     * handlers возможные вариации логгирования (в консоль, в файл)
     */
    private volatile static Log logInstance;
    public enum levels {DEBUG, INFO, ERROR, CRITICAL}
    public enum handlers {CONSOLE, FILE}

    /**
     * handler текущий вариант логгирования
     * level минимальный уровень логгирования
     * logfile путь к файлу для логов
     * maxStackTraceElements максимальное количество выводимых элементов StackTrace
     */
    private handlers handler;
    private levels level;
    private String logfile;
    private int maxStackTraceElements;

    /**
     * Конструктор приватный
     */
    private Log() {
        handler = null;
        level = null;
        logfile = null;
        maxStackTraceElements = -1;
    }

    /**
     * Получение или создание экземпляра класса для логгирования
     * @return существующий экземпляр класса для логгирования, или новый если еще не создан
     */
    public static Log getInstance() {
        if(logInstance == null) {
            synchronized (Log.class) {
                if(logInstance == null) {
                    logInstance = new Log();
                }
            }
        }
        return logInstance;
    }

    /**
     * Логгирование
     * @param currentLevel уровень лога
     * @param message сообщение
     * @param newlineVA true - добавить новую строку, false - не добавлять, по умолчанию true
     */
    public static void log(levels currentLevel, String message, Boolean... newlineVA) {
        getInstance().dolog(currentLevel, message, newlineVA);
    }

    /**
     * Изменение конфигурации логгирования
     * @param config конфигурация
     * @throws InvalidParameterException ошибки разбора конфигурации
     */
    public void setLogConfig(HashMap<String, String> config) throws InvalidParameterException {
        if (config == null)
            throw new InvalidParameterException("Задаваемая логгеру конфигурация равна null");

        String newHandler = Config.getValueByKey(config, "handler");
        String newLevel = Config.getValueByKey(config, "level");

        handler = null;
        for (handlers hnd : handlers.values())
            if (hnd.name().equals(newHandler)) handler = hnd;
        if (handler == null)
            throw new InvalidParameterException("Задаваемый логгеру handler не существует в списке handlers");

        if (handler == handlers.FILE)
            logfile = Config.getValueByKey(config, "file");

        level = null;
        for (levels lvl : levels.values())
            if (lvl.name().equals(newLevel)) level = lvl;
        if (level == null)
            throw new InvalidParameterException("Задаваемый логгеру level не существует в списке levels");

        maxStackTraceElements = Integer.parseInt(Config.getValueByKey(config, "max-stack-trace-elements"));
        if(maxStackTraceElements < 0)
            throw new InvalidParameterException("Задаваемый логгеру max-stack-trace-elements меньше 0");
    }

    /**
     * Проверка существующей конфигурации
     * @return true - конфигурация корректна, false - конфигурация некорректна
     */
    public boolean checkConfig() {
        return (handler != null && level != null && maxStackTraceElements != -1) &&
                (handler != handlers.FILE || logfile != null);
    }

    /**
     * Логгирование
     * @param currentLevel уровень лога
     * @param message сообщение
     * @param newlineVA true - добавить новую строку, false - не добавлять, по умолчанию true
     */
    public void dolog(levels currentLevel, String message, Boolean... newlineVA) {
        if (currentLevel == null) throw new InvalidParameterException("В логгер аргументу currentLevel передан null");
        if (message == null)
            throw new InvalidParameterException("В логгер аргументу message передан null");

        if (!checkConfig()) {
            System.out.println("!!!!!NOLOGCONFIG!!!!! USE CONSOLE, MSG:" + currentLevel.name() + " : "+ message);
            return;
        }

        if (currentLevel.ordinal() >= level.ordinal()) {
            boolean newline = newlineVA.length > 0 ? newlineVA[0] : true;
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

            int startIndex = 3;
            int tracesToPrint = Math.min(maxStackTraceElements, stackTraceElements.length - startIndex);
            StringBuilder traceAppend = new StringBuilder();
            for (int i = 0; i < tracesToPrint; ++i) {
                if(i == tracesToPrint - 1) traceAppend.append(stackTraceElements[startIndex + i].toString());
                else traceAppend.append(stackTraceElements[startIndex + i].toString()).append(" <--- ");
            }

            if(traceAppend.toString().length() == 0) traceAppend.append("StackTrace отсутствует");

            DateFormat time = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss:SSSSSSS");
            Calendar calendar = Calendar.getInstance();
            time.format(calendar.getTime());
            if(!message.equals("")) message = "[" + time.format(calendar.getTime()) + "]: " + currentLevel.name() + " : " + message + " : " + traceAppend;

            if (handler == handlers.CONSOLE) {
                if(newline) System.out.println(message);
                else System.out.print(message);
            } else {
                try (FileWriter writer = new FileWriter(new File(logfile), true)) {
                    if(newline) writer.write(message + '\n');
                    else writer.write(message);
                    writer.flush();
                } catch (IOException ex) {
                    System.out.println("Произошла ошибка: " + ex.getMessage() + " при выводе логов: " + message);
                }
            }
        }
    }
}
