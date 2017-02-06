package com.github.concurrency.ch10.exercises;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Write a program that walks a directory tree and generates a thread for each file. In
 * the threads, count the number of words in the files and, without using locks, update a
 * shared counter that is declared as
 * public static long count = 0;
 * Run the program multiple times. What happens? Why?
 */
public class LockFreeWithErrors_10_16 {

    private long counter = 0;
    private ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException, InterruptedException {
        final String path = args[0];
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Provide path as first argument");
        }

        new LockFreeWithErrors_10_16().execute(path);
    }

    private void execute(String path) throws IOException, InterruptedException {

        List<CompletableFuture> futures = new ArrayList<>();

        Files.list(Paths.get(path)).forEach((p) -> futures.add(
                CompletableFuture.runAsync(() -> {
                    try {
                        long count = Files.readAllLines(Paths.get(p.toFile().getAbsolutePath()), Charset.forName("utf-8"))
                                .stream()
                                .flatMap(lines -> Stream.of(lines.split(" ")))
                                .count();

                        counter = counter + count;
//                        System.out.println(">>> counter=" + counter);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, pool)
        ));

        CompletableFuture[] futuresArr = new CompletableFuture[futures.size()];
        futures.toArray(futuresArr);
        CompletableFuture.allOf(futuresArr)
                .thenRunAsync(() -> System.out.println("Result: " + counter), pool);

        pool.shutdown();
        pool.awaitTermination(100, TimeUnit.SECONDS);
    }
}
