package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * Use a blocking queue for processing files in a directory. One thread walks the file
 * tree and inserts files into a queue. Several threads remove the files and search each
 * one for a given keyword, printing out any matches. When the producer is done, it
 * should put a dummy file into the queue.
 */
public class BlockQueueFileCrawler_10_10 {

    public static final int NUM_OF_READERS = 4;
    public static final File DUMMY = new File("<DUMMY>");
    private BlockingQueue<File> queue = new LinkedBlockingQueue<>();
    private ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_READERS + 1);

    public static void main(String[] args) {
        final String path = args[0];
        final String keyword = args[1];
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Provide path as first argument");
        }
        if (keyword == null || keyword.isEmpty()) {
            throw new IllegalArgumentException("Provide keyword as second argument");
        }


        new BlockQueueFileCrawler_10_10().execute(path, keyword);
    }

    private void execute(String path, String keyword) {
        System.out.println("start");
        CompletableFuture.runAsync(() -> {

            try {
                Files.list(Paths.get(path)).forEach(p -> {
                    try {
                        File file = p.toFile();
                        queue.put(file);
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
                queue.put(DUMMY);
                System.out.println(">>> put DUMMY to queue <<<");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, pool);


        AtomicBoolean completed = new AtomicBoolean(false);
        IntStream.range(0, NUM_OF_READERS).forEach(
                (ignore) -> CompletableFuture.runAsync(() -> {
                    //let it just check name of file not it's content
                    while (!completed.get()) {

                        try {
                            File file = queue.poll(100, TimeUnit.MILLISECONDS);
                            if (file != null) {

                                System.out.println("<<< polled file=" + file.getName());
                                if (file.equals(DUMMY)) {
                                    completed.set(true);
                                }

                                if (!completed.get() && file.getName().contains(keyword)) {
                                    System.out.println("found keyword '" + keyword + "' in file=" + file.getName());
                                }
                            }

                            if (completed.get()) {
                                System.out.println(">>>>>>>>>>>>>> Mark as COMPLETED");
                            }

                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }
                    System.out.println(">>> COMPLETE");
                }, pool)
        );

        pool.shutdown();
        System.out.println("end");
    }
}
