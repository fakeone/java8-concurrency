package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Repeat the preceding exercise, but instead have each consumer compile a map of
 * words and their frequencies that are inserted into a second queue.
 * A final thread merges the dictionaries and prints the ten most common words.
 * Why don’t you need to use a ConcurrentHashMap?
 */
//TODO use separate map for every writer?
public class WordsFrequencyMap_10_11 {

    public static final int NUM_OF_READERS = 4;
    public static final File DUMMY = new File("<DUMMY>");
    private BlockingQueue<File> filesMap = new LinkedBlockingQueue<>();
    private BlockingQueue<Map<String, Integer>> queueOfMaps = new LinkedBlockingQueue<>();

    private ExecutorService pool;

    public static void main(String[] args) throws InterruptedException {
        final String path = args[0];
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Provide path as first argument");
        }

        new WordsFrequencyMap_10_11().execute(path);
    }

    private void execute(String path) throws InterruptedException {
        pool = Executors.newFixedThreadPool(NUM_OF_READERS + 1);

        executeWriter(path);
        List<CompletableFuture> readers = executeReaders();

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("\n********************************************\nfound frequencies: " + queueOfMaps.size());
        System.out.println("frequencies:");
        queueOfMaps.forEach(System.out::println);

        CompletableFuture<?>[] readersArr = new CompletableFuture[readers.size()];
        CompletableFuture<Void> aggregator = CompletableFuture.allOf(readers.toArray(readersArr))
                .thenRun(() -> {
                    Map<String, Integer> map = queueOfMaps.stream().reduce((m1, m2) -> {
                        for (Map.Entry<String, Integer> entry : m1.entrySet()) {
                            Integer secondMapValue = m2.get(entry.getKey());
                            if (secondMapValue == null) {
                                m2.put(entry.getKey(), entry.getValue());
                            } else {
                                m2.put(entry.getKey(), secondMapValue + entry.getValue());
                            }
                        }
                        return m2;
                    }).get();

                    List<Integer> intList = new ArrayList<Integer>(map.values());
                    Collections.sort(intList, (o1, o2) -> {
                        // for descending order
                        return o2 - o1;
                    });

                    Map<String, Integer> sortedMap = sortMap(map);
                    Iterator<Map.Entry<String, Integer>> iterator = sortedMap.entrySet().iterator();
                    for (int i = 0; i < 10; i++) {
                        System.out.println(">>> " + iterator.next());
                    }
                });

//        while (!aggregator.isDone()) {
//
//        }
    }

    private static Map<String, Integer> sortMap(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        Collections.sort(list, (a, b) -> {
            int cmp1 = a.getValue().compareTo(b.getValue());
            if (cmp1 != 0)
                return cmp1;
            else
                return a.getKey().compareTo(b.getKey());
        });
        //redundant
        Collections.reverse(list);

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list)
            result.put(entry.getKey(), entry.getValue());

        return result;
    }

    private void executeWriter(String path) {
        //writer
        CompletableFuture.runAsync(() -> {

            try {
                Files.list(Paths.get(path)).forEach(p -> {
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

    private List<CompletableFuture> executeReaders() {
        List<CompletableFuture> readers = new ArrayList<>();

        IntStream.range(0, NUM_OF_READERS).forEach(
                (ignore) -> readers.add(CompletableFuture.runAsync(() -> {
                            //let it just check name of file not it's content

                            Map<String, Integer> map = new HashMap<String, Integer>();

                            boolean completed = false;
                            while (!completed) {

                                try {
                                    File file = filesMap.poll(100, TimeUnit.MILLISECONDS);
                                    if (file != null) {

                                        System.out.println("<<< peeked file=" + file.getName());
                                        if (file.equals(DUMMY)) {
                                            completed = true;
                                            //put dummy to queue for reader threads which a waiting for completion marker
                                            filesMap.put(DUMMY);
                                        }

                                        if (!completed) {
                                            countFrequency(map, file);

                                        }
                                    }
                                } catch (Exception e) {
                                    //ignore
                                }
                            }

                            queueOfMaps.add(map);
                            System.out.println(">>> COMPLETE");
                        }, pool)
                ));

        return readers;
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
