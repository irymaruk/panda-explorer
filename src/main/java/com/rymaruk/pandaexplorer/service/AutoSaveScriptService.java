package com.rymaruk.pandaexplorer.service;

import com.rymaruk.pandaexplorer.MainApp;
import com.rymaruk.pandaexplorer.misc.CommonUtils;
import com.rymaruk.pandaexplorer.misc.FileUtils;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AutoSaveScriptService {

    private static final Logger log = LoggerFactory.getLogger(MainApp.class);
    private static CodeArea area;
    private static String lastSavedStr;

    private AutoSaveScriptService() {
    }

    public static void start(CodeArea codeArea) {
        area = codeArea;
        lastSavedStr = area.getText();
        CommonUtils.getScheduledExecutorService().scheduleWithFixedDelay(new SaveScriptFileTask(), 10, 10, TimeUnit.SECONDS);
    }

    private static class SaveScriptFileTask implements Runnable {
        @Override
        public void run() {
            String newText = area.getText();
            if (!lastSavedStr.equals(newText)) {
                log.info("Saving scripts to the file");
                FileUtils.writeScriptContent(newText);
                lastSavedStr = newText;
            }
        }
    }
}
