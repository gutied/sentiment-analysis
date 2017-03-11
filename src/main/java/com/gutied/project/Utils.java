package com.gutied.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static List<String> listFolders(File file) {
        return Arrays.asList(file.list((current, name) -> new File(current, name).isDirectory()));
    }

    public static List<String> listFiles(File file) {
        return Arrays.asList(file.list());
    }

}
