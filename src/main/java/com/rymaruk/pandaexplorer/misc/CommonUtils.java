package com.rymaruk.pandaexplorer.misc;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rymaruk.pandaexplorer.view.MainUI;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CommonUtils {

    private static final ObjectMapper objectMapper = initObjectMapperWithIndentation();
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private static ObjectMapper initObjectMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static ObjectMapper initObjectMapperWithIndentation() {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter()
//                .withArrayIndenter(indenter)
                .withObjectIndenter(indenter);
        return new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPrettyPrinter(printer);
    }

    public static ExecutorService getExecutorService() {
        return executor;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static URL getResource(String name) {
        return CommonUtils.class.getResource(name);
    }

    public static ImageView getImageView(String fileName) {
        return new ImageView(new Image("/icons/" + fileName));
    }

    public static void bindProgress(Task task) {
        MainUI.progressText.textProperty().bind(task.messageProperty());
        MainUI.progressText.visibleProperty().bind(task.runningProperty());
        MainUI.progressBar.progressProperty().bind(task.progressProperty());
        MainUI.progressBar.visibleProperty().bind(task.runningProperty());
    }

    public static void setStatusText(String text) {
        MainUI.progressText.visibleProperty().unbind();
        MainUI.progressText.textProperty().unbind();
        MainUI.progressText.setVisible(true);
        MainUI.progressText.setText(text);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static String convertToJsonString(String str) {
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(str));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not convert object to json string: " + e.getMessage());
        }
    }

    public static String convertToJsonString(List<String> list) {
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(list.toString()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not convert object to json string: " + e.getMessage());
        }
    }

    public static String convertToJsonString(ArrayList<JsonNode> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not convert object to json string: " + e.getMessage());
        }
    }

}
