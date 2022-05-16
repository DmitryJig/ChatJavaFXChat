package com.example.chatjavafx.logger;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.input.ReversedLinesFileReader;


public class LoggerImpl implements ChatLogger {

    private final String PATH_DIR = "src/main/resources/userHistory"; // директория куда складываем файлы
    private final String FILE_NAME = "history_%s.txt";  // шаблон имени файла
    private final int COUNT_LAST_LINES = 100;
    private PrintWriter printWriter;
    private File file;


    public LoggerImpl(String login) {
        String fileName = String.format(FILE_NAME, login);
        file = new File(PATH_DIR, fileName);
        try {
            printWriter = new PrintWriter(new FileWriter(file, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

    }

    @Override
    public void write(String msg) {
        this.printWriter.println(msg);
    }

    @Override
    public String readLast100Lines() {
        List<String> last100Lines = new LinkedList<>();
        String line;

        try (ReversedLinesFileReader fileReader = new ReversedLinesFileReader(file)) {  // только с объектом File конструктор данного класса помечен Deprecated (можно Path)
            for (int i = 0; i < COUNT_LAST_LINES; i++) {
                line = fileReader.readLine();
                if (line == null){
                    break;
                }
                last100Lines.add(0, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (last100Lines == null){
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        last100Lines.forEach(s -> stringBuilder.append(s).append("\n"));
        return stringBuilder.toString();
    }

    @Override
    public void close() {
        printWriter.flush();
        printWriter.close();
    }

}
