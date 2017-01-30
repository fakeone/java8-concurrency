package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {

    public boolean containsWord(File file, String word) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.forName("utf-8"));
        for (String line : lines) {
            if (line.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
