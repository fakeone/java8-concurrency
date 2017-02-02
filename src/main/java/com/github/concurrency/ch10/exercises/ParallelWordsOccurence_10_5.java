package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Write an application in which multiple threads read all words from a collection of
 * files. Use a ConcurrentHashMap<String, Set<File>> to track in which
 * files each word occurs. Use the merge method to update the map.
 */
public class ParallelWordsOccurence_10_5 {

    private ExecutorService pool = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, Set<File>> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        File[] files = new File(FindWord_YieldOthers_10_3.class.getResource("/words").toURI().getPath())
                .listFiles(File::isFile);
        ParallelWordsOccurence_10_5 app = new ParallelWordsOccurence_10_5();
        app.execute(files);
        app.stop();
        app.printResults();
    }

    private void stop() throws InterruptedException {
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    private void printResults() {
        map.forEach((k, v) -> System.out.println(k + " - " + v.stream().flatMap(f -> Stream.of(f.getName())).collect(Collectors.joining(","))));
        System.out.println("Received " + map.size() + " results");
    }

    private void execute(File[] files) throws Exception {

        Arrays.stream(files)
                .forEach(f -> pool.submit(() -> {
                    try {
                        Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.forName("utf-8"))
                                .stream()
                                .flatMap(line -> Stream.of(line.split(" ")))
                                .forEach(word -> {

                                    Set<File> fileSet = new HashSet<File>();
                                    fileSet.add(f);

//                                    System.out.println("word=" + word + " file=" + f.getName());
                                    /*
                                     * If the specified key is not already associated with a
                                     * (non-null) value, associates it with the given value.
                                     */
                                    map.merge(word, fileSet,
                                            (prevVal, newVal) -> {
                                                prevVal.addAll(newVal);
                                                return prevVal;
                                            });

                                });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }));
    }
}
