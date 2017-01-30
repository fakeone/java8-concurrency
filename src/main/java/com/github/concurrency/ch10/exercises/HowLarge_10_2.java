package com.github.concurrency.ch10.exercises;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

/**
 * How large does an array have to be for Arrays.parallelSort to be faster
 * than Arrays.sort on your computer?
 */
public class HowLarge_10_2 {

    private Random random = new Random();

    @Test
    public void howLargetToBeBetter() throws Exception {

        boolean parallelWins = false;
        int size = 100;
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.reset();

        while (!parallelWins) {
            System.out.println("current array size: " + size);
            int[] arr = createArchive(size);
            int[] parr = Arrays.copyOf(arr, arr.length);

            watch.start();
            Arrays.parallelSort(parr);
            watch.stop();
            long parallelTime = watch.getNanoTime();
            watch.reset();

            watch.start();
            Arrays.sort(arr);
            watch.stop();
            long sequentialTime = watch.getNanoTime();
            watch.reset();

            size = increaseSize(size);

            System.out.println("sequential time:" + sequentialTime);
            System.out.println("parallel time:" + parallelTime);

            if (parallelTime < sequentialTime) {
                parallelWins = true;
            }
            System.out.println("\n");
        }

    }

    private int[] createArchive(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt();
        }
        return arr;
    }

    private int increaseSize(int size) {
        return (int) (size * 1.2);
    }
}
