package com.rymaruk.pandaexplorer;

import com.rymaruk.pandaexplorer.misc.CommonUtils;
import com.rymaruk.pandaexplorer.view.MainUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static Application instance;
    private static final Logger log = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        launch(args);
    }

    public static Application getAppInstance() {
        return instance;
    }

    public void start(Stage stage) {
        Parent rootNode = new MainUI().get();
        Scene scene = new Scene(rootNode, 1500, 900);
        scene.getStylesheets().add(CommonUtils.getResource("/style.css").toExternalForm());
        stage.setTitle("Panda Explorer - 0.7.4");
        stage.getIcons().add(new Image(CommonUtils.getResource("/icons/panda_face.jpg").toExternalForm()));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        Platform.setImplicitExit(true);
        instance = this;
    }

    @Override
    public void stop() {
        log.info("Shutting down ExecutorService");
        CommonUtils.getExecutorService().shutdownNow();
        log.info("Shutting down ScheduledExecutorService");
        CommonUtils.getScheduledExecutorService().shutdownNow();
        System.exit(0);
    }
}