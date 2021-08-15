package com.rymaruk.pandaexplorer.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DateTimeEnum {
    RAW,
    LOCAL,
    UTC;

    public static List<String> getAllStringValues() {
        return Arrays.stream(DateTimeEnum.values()).map(Enum::name).collect(Collectors.toList());
    }
}
