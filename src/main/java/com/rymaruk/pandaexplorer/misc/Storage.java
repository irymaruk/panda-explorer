package com.rymaruk.pandaexplorer.misc;

import java.util.HashMap;

public class Storage {

    private static HashMap<Keys, Object> storage = new HashMap<>();

    public static HashMap<Keys, Object> getInstance() {
        return storage;
    }


    public enum Keys {
        comboBox
    }

}
