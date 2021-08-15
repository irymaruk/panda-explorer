package com.rymaruk.pandaexplorer.misc;

import com.rymaruk.pandaexplorer.view.ToolBarUI;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    private static final String scriptFileName = "scripts.txt";

    public static String getBasePath() {
        try {
            return new File(ToolBarUI.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can not determine jar execution path");
        }
    }

    public static String readScriptContent() {
        try {
            return new String(Files.readAllBytes(Paths.get(getBasePath(), scriptFileName)));
        } catch (IOException e) {
            return "Can not find scripts.txt";
        }
    }

    public static String readFileContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(getBasePath(), filePath)));
        } catch (IOException e) {
            return "Can not find " + filePath;
        }
    }

    public static void writeScriptContent(String text) {
        try {
            Path path = Paths.get(getBasePath(), scriptFileName);
            Files.write(path, text.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
