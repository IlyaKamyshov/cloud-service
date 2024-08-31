package ru.netology.cloudservice.logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class CloudServiceLogger {

    @Value("${info.log.path}")
    private String infoLogPath;
    @Value("${error.log.path}")
    private String errorLogPath;


    public void logInfo(String msg) {
        log(msg, infoLogPath);
    }

    public void logError(String msg) {
        log(msg, errorLogPath);
    }

    private void log(String msg, String logPath) {

        try (PrintWriter writer = new PrintWriter(new FileWriter(logPath, true))) {
            writer.append("[")
                    .append(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                    .append(" ")
                    .append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.nnn")))
                    .append("]\t")
                    .append(msg)
                    .append("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}