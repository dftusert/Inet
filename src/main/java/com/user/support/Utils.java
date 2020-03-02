package com.user.support;

import com.user.exceptions.PRException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Класс для дополнительных функций
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Utils {
    /**
     * logLevels уровни логгирования
     * logLevel 'порог' логгирования
     * logFile путь к файлу в который будут записываться логи или null, тогда логи будут выводиться на терминал
     * showGraphicNotifiction true - показывать графические уведомления, false - не показывать
     * graphicNotificationShowingTime время показа графического уведомления в мс
     */
    // TODO: более полный enum - DEBUG, INFO, WARNING, ERROR, CRITICAL - в таком порядке
    public enum logLevels {DEBUG, INFO, WARNING, CRITICAL}
    public static logLevels logLevel = logLevels.INFO;
    public static String logFile = null;

    /**
     * Получение расширения изображения
     * @param imageURL ссылка на изображения
     * @return расширение
     */
    public static String getImgExtensionFromURL(URL imageURL) {
        String fileExtension = imageURL.toString().substring(0, imageURL.toString().indexOf("?"));
        return fileExtension.substring(fileExtension.lastIndexOf(".") + 1);
    }

    /**
     * Сохранение изображения, находящегося поссылке, в файл
     * @param imageURL ссылка на изображение
     * @param imagePath путь к изображению
     * @param imageName название изображения
     * @param imageExtension расширение изображения
     * @return записанные данные
     * @throws IOException проблемы с записью
     * @throws PRException при проблемах с правами на создание директорий и удаление файлов из этих директорий
     */
    public static byte[] saveImageToFileByURL(URL imageURL, String imagePath, String imageName, String imageExtension) throws IOException, PRException {
        BufferedImage saveImage = ImageIO.read(imageURL);
        prepareDirectoryToWriteFile(imagePath, imageName, imageExtension);
        ImageIO.write(saveImage, imageExtension, new File(imagePath + '/' + imageName + '.' + imageExtension));
        return getFileData(imagePath + '/' + imageName + '.' + imageExtension);
    }

    /**
     * Считывание файла
     * @param file путь и имя файла
     * @return данные файла
     * @throws IOException проблемы чтения файла
     */
    public static byte[] getFileData(String file) throws IOException {
        try(FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        }
    }

    /**
     * Подготовка к созданию и записи в файл
     * @param path директория для файла
     * @param name имя файла
     * @param extension расширение файла
     * @throws PRException при проблемах с правами на создание директорий и удаление файлов из этих директорий
     */
    private static void prepareDirectoryToWriteFile(String path, String name, String extension) throws PRException {
        File directory = new File(path);
        if (!directory.exists())
            if(!directory.mkdirs()) throw new PRException("Невозможно создать директорию " + directory.getAbsolutePath());
        File file = new File(path + '/' + name + '.' + extension);
        if (file.exists())
            if(!file.delete()) throw new PRException("Невозможно удалить файл " + file.getAbsolutePath());
    }

    /**
     * Специальная информация для добавления к имени файла для хранения снимков
     * @return .yyyy_MM_dd_HH_mm_ss_SSSSSSS.bcp
     */
    public static String getBcpMark() {
        DateFormat time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSSSSS");
        Calendar calendar = Calendar.getInstance();
        return '.' + time.format(calendar.getTime()) + ".bcp";
    }

    /**
     * Сравнение двух массивов из байт
     * @param first первый массив из байт
     * @param second второй массив из байт
     * @return true - массивы равны, false - массивы не равны
     */
    public static boolean isByteArraysEquals(byte[] first, byte[] second) {
        if(first.length != second.length) return false;
        for(int i = 0; i < first.length; ++i)
            if(first[i] != second[i]) return false;
        return true;
    }

    /**
     * Удаление файла
     * @param path путь к файлу
     * @param name имя файла
     * @param extension расширение файла
     * @throws PRException проблемы с удалением файла (в большинстве случаев из-за прав)
     */
    public static void deleteFile(String path, String name, String extension) throws PRException {
        File directory = new File(path);
        if (!directory.exists())
            throw new PRException("Невозможно найти директорию " + directory.getAbsolutePath());
        File file = new File(path + '/' + name + '.' + extension);
        if (file.exists())
            if(!file.delete()) throw new PRException("Невозможно удалить файл " + file.getAbsolutePath());
    }

    /**
     * Получение временной метки для хранения информации о времени добавления данных в базу данных
     * @return временная метки
     */
    public static String getDbTimeMark() {
        DateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSSSSS");
        Calendar calendar = Calendar.getInstance();
        return time.format(calendar.getTime());
    }

    /**
     * Логгирование происходящих событий
     * @param level уровень
     * @param message сообщение
     */
    public static void log(logLevels level, String message) {
        if(level.ordinal() >= logLevel.ordinal()) {
            DateFormat time = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss:SSSSSSS");
            Calendar calendar = Calendar.getInstance();
            time.format(calendar.getTime());
            if(!message.equals("")) message = "[" +  time.format(calendar.getTime()) + "]: " + level.name() + " : " + message;

            // @MARK:LOG
            if(logFile == null) System.out.println(message);
            else {
                try(FileWriter writer = new FileWriter(new File(logFile), true)) {
                    writer.write(message + '\n');
                    writer.flush();
                } catch (IOException ex) {
                    // @MARK:LOG
                    System.out.println("Произошла ошибка: " + ex.getMessage() + " при выводе логов: " + message);
                }
            }
        }
    }
}
