package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class WordsFrequencyCallablePerFile_10_12 {

    public static final int NUM_OF_READERS = 4;
    //    public static final File DUMMY = new File("<DUMMY>");
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

    private void execute(String basePath) throws Exception {
        Stream<Path> paths = Files.list(Paths.get(basePath));
        executeWriter(paths);

        int numOfFiles = Long.valueOf(paths.count()).intValue();
        pool = Executors.newFixedThreadPool(numOfFiles);

        Collection<Callable<Map<String, Integer>>> callables = new ArrayList<>(numOfFiles);
        paths.forEach(path -> {
            Callable<Map<String, Integer>> callable = () -> {

                Map<String, Integer> map = new HashMap<>();
                countFrequency(map, path.toFile());
                return map;
            };
            callables.add(callable);
        });

        Map<String, Integer> aggregation = new HashMap<>();
        List<Future<Map<String, Integer>>> futures = pool.invokeAll(callables);
        for (Future<Map<String, Integer>> future : futures) {
            Map<String, Integer> map = future.get();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                Integer result = map.get(entry.getKey());
                Integer finalResult;
                if (result != null) {
                    finalResult = entry.getValue() + result;
                } else {
                    finalResult = entry.getValue();
                }
                aggregation.put(entry.getKey(), finalResult);
            }
        }
        System.out.println(">>> result: ");
        Iterator<Map.Entry<String, Integer>> iterator = aggregation.entrySet().iterator();
        for (int i = 0; i < 10; i++) {
            System.out.println(">>> " + iterator.next());
        }
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

//            //When the producer is done, it should put a dummy file into the queue.
//            try {
//                filesMap.put(DUMMY);
//                System.out.println(">>> put DUMMY to queue <<<");
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
        }, pool);
    }

    private void countFrequency(Map<String, Integer> map, File file) throws IOException {
        Files.readAllLines(Paths.get(file.getAbsolutePath()), Charset.forName("utf-8"))
                .stream()
                .flatMap((list) -> Stream.of(list.split(" ")))
                .forEach(w -> {

                    Integer frequency = Optional.ofNullable(map.get(w)).orElse(0) + 1;
                    map.put(w, frequency);

                });
    }
}
