package com.github.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fix of "VisibilityFail" class issue.
 */
public class VisibilityFailFix {

    //TODO "volatile" keyword makes changes done in first thread visible to the second.
    private static volatile boolean done = false;

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