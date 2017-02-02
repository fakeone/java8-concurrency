package com.github.concurrency.ch10.exercises;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Implement a method yielding a task that reads through all words in a file, trying to
 * find a given word. The task should finish immediately (with a debug message) when
 * it is interrupted.
 * For all files in a directory, schedule one task for each file. Interrupt
 * all others when one of them has succeeded
 */
public class FindWord_YieldOthers_10_3 {

    public static void main(String[] args) throws Exception {
        File[] files = new File(FindWord_YieldOthers_10_3.class.getResource("/text").toURI().getPath())
                .listFiles(File::isFile);
        new FindWord_YieldOthers_10_3().execute(files);
    }

    /**
     * Checks not word presence but presence of symbols sequence.
     * @throws Exception
     * @param files
     */
    public void execute(File[] files) throws Exception {

        String word = "and";

        ExecutorService pool = Executors.newCachedThreadPool();
        FileUtil util = new FileUtil();

        Stream.of(files).forEach(f -> {
            System.out.println("supply task for file=" + f.getName());

            CompletableFuture.supplyAsync(() -> {

                try {
                    System.out.println("check file=" + f.getName());
                    if (util.containsWord(f, word)) {
                        System.out.println(">>> complete by finding work=" + word + " in file=" + f.getName());
                        return true;
                    }
                } catch (IOException e) {
                    return false;
                }

                System.out.println("file=" + f.getName() + " doesn't contain word=" + word);
                return false;

            }, pool)
                    .thenAccept(r -> {
                        System.out.println("handle result");
                        if (r) {
                            System.out.println("stop execution");
                            pool.shutdownNow();
                        }
                    });
        });
    }
}
