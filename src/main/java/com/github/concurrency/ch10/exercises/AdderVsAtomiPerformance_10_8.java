package com.github.concurrency.ch10.exercises;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

/**
 * Generate 1,000 threads, each of which increments a counter 100,000 times.
 * Compare the performance of using AtomicLong versus LongAdder.
 */
public class AdderVsAtomiPerformance_10_8 {

    public static final int NUM_OF_THREADS = 1000;
    public static final int INCREMENT_TIMES = 1000000;

    public static void main(String[] args) throws InterruptedException {
        new AdderVsAtomiPerformance_10_8().execute();
    }

    private void execute() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            //=======================Adder===================================================

            AtomicLong atomicLong = new AtomicLong(0);
            StopWatch stopWatch = StopWatch.createStarted();
            stopWatch.stop();
            stopWatch.reset();
            stopWatch.start();

            ExecutorService pool2 = Executors.newFixedThreadPool(NUM_OF_THREADS);
            LongAdder longAdder = new LongAdder();

            IntStream.range(0, INCREMENT_TIMES).flatMap((num) -> {
                        pool2.execute(() -> longAdder.increment());
                        return IntStream.of(1);
                    }
            ).count();
            System.out.println("Adder=\t" + stopWatch.getNanoTime());

            //=======================Atomic===================================================

            stopWatch.reset();
            stopWatch.start();

            ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
            IntStream.range(0, INCREMENT_TIMES).flatMap((num) -> {
                        pool.execute(() -> atomicLong.incrementAndGet());
                        return IntStream.of(1);
                    }
            ).count();
            System.out.println("Atomic=\t" + stopWatch.getNanoTime());
            System.out.println("========================================");

            //shutdown
            pool.shutdown();
            pool2.shutdown();
        }
    }
}
