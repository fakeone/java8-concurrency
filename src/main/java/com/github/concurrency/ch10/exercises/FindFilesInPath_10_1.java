package com.github.concurrency.ch10.exercises;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
Using parallel streams, find all files in a directory that contain a given word.
How do you find just the first one?
Are the files actually searched concurrently?
 */
public class FindFilesInPath_10_1 {

    private final File source = new File("/home/oleksandrza");//TODO get user dir
    private final String word = "a";

    @Test
    public void one() throws Exception {
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."))) {
//            for (Path file : stream) {
//                System.out.println(file.getFileName());
//            }
//        } catch (IOException | DirectoryIteratorException x) {
//            // IOException can never be thrown by the iteration.
//            // In this snippet, it can only be thrown by newDirectoryStream.
//            System.err.println(x);
//        }

        FileUtil util = new FileUtil();
        final AtomicBoolean[] terminate = {new AtomicBoolean(false)};

        Arrays.asList(source.listFiles(File::isFile))
                .parallelStream()
                .forEach(f -> {
                    boolean flag = terminate[0].get();
                    System.out.println(f.getName() + " : flag=" + flag);
                    if (!flag) {
                        try {

                            if (util.containsWord(f, word)) {
                                System.out.println(">>> Found file containing word = " + f.getName());
                                terminate[0].set(true);
                                return;
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    @Test
    public void two() throws Exception {
        Charset encoding = Charset.forName("utf-8");

        Arrays.asList(source.listFiles(File::isFile))
                .parallelStream()
                .flatMap(file -> {
                    try {
                        return Files.readAllLines(Paths.get(file.getAbsolutePath()), encoding)
                                .stream()
                                .flatMap(line -> Stream.of(new Pair(file, line)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(pair -> pair.getLine().contains(word))
                .flatMap(pair -> Stream.of(pair.getFile().getName()))
                .collect(Collectors.toSet())
                .forEach(System.out::println);
    }

    private class Pair {
        private File file;
        private String line;

        public Pair(File file, String line) {
            this.file = file;
            this.line = line;
        }

        public File getFile() {
            return file;
        }

        public String getLine() {
            return line;
        }
    }
}
