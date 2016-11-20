package com.github.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * "Goodbye" won't be printed, and program won't terminate, as value of done==true never visible to Goodbye thread.
 */
public class VisibilityFail {

    private static boolean done = false;

    public static void main(String[] args) {
        Runnable hellos = () -> {
            for (int i = 0; i < 1000; i++) {
                System.out.println("Hello " + i);
            }
            done = true;
        };

        Runnable goodbye = () -> {
            int i = 0;
            while (!done) {
                i++;
            }
            System.out.println("Goodbye " + i);

        };

        ExecutorService threadPool = Executors.newCachedThreadPool();
        threadPool.execute(hellos);
        threadPool.execute(goodbye);
    }
}