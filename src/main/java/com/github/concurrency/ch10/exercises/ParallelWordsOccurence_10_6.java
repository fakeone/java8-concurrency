package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Repeat the preceding exercise, but use computeIfAbsent instead. What is the
 * advantage of this approach?
 */
public class ParallelWordsOccurence_10_6 {

    private ExecutorService pool = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, Set<File>> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        File[] files = new File(FindWord_YieldOthers_10_3.class.getResource("/words").toURI().getPath())
                .listFiles(File::isFile);
        ParallelWordsOccurence_10_6 app = new ParallelWordsOccurence_10_6();
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

                                    Set<File> fileSet = Optional.ofNullable(map.get(word)).orElse(new HashSet<>());
                                    fileSet.add(f);

                                    /*
                                     * If the specified key is not already associated with a value,
                                     * attempts to compute its value using the given mapping function
                                     * and enters it into this map unless {@code null}.
                                     *
                                     * The entire method invocation is performed atomically, so the function is
                                     * applied at most once per key.  Some attempted update operations
                                     * on this map by other threads may be blocked while computation
                                     * is in progress, so the computation should be short and simple,
                                     * and must not attempt to update any other mappings of this map.
                                     */
                                    map.computeIfAbsent(word, (key) -> fileSet);
                                });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }));
    }
}
