package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class WordsFrequencyCallablePerFile_10_12 {

    public static final int NUM_OF_READERS = 4;
    public static final File DUMMY = new File("<DUMMY>");
    private BlockingQueue<File> filesMap = new LinkedBlockingQueue<>();
    private BlockingQueue<Map<String, Integer>> queueOfMaps = new LinkedBlockingQueue<>();

    private ExecutorService pool;

    public static void main(String[] args) throws Exception {
        final String path = args[0];
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Provide path as first argument");
        }

        new WordsFrequencyCallablePerFile_10_12().execute(path);
    }

    private void execute(String path) throws Exception {
        Stream<Path> files = Files.list(Paths.get(path));
        executeWriter(files);

        int numOfFiles = Long.valueOf(files.count()).intValue();
        pool = Executors.newFixedThreadPool(numOfFiles);

        Collection<Callable<Map<String, Integer>>> callables = new ArrayList<>(numOfFiles);
        files.forEach(file -> {
            Callable<Map<String, Integer>> callable = () -> {
                return null;//TODO implement
            };
            callables.add(callable);
        });

        pool.invokeAll(callables);
    }

    private void executeWriter(Stream<Path> files) {
        //writer
        CompletableFuture.runAsync(() -> {

            try {
                files.forEach(p -> {
                    try {
                        File file = p.toFile();
                        filesMap.put(file);
                        System.out.println(">>> put " + file.getAbsolutePath() + " to queue");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //When the producer is done, it should put a dummy file into the queue.
            try {
                filesMap.put(DUMMY);
                System.out.println(">>> put DUMMY to queue <<<");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, pool);
    }

}
