package com.github.concurrency.ch10.exercises;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.LongAccumulator;

/**
 * Use a LongAccumulator to compute the maximum or minimum of the accumulated elements.
 */
public class LongAccumulator_10_9 {

    public static void main(String[] args) {
        new LongAccumulator_10_9().execute();
    }

    private void execute() {
        LongAccumulator maxAccum = new LongAccumulator((a, b) -> Math.max(a, b), Long.MIN_VALUE);
        LongAccumulator minAccum = new LongAccumulator((a, b) -> Math.min(a, b), Long.MAX_VALUE);

        List<Long> list = createList(20);
        list.forEach(System.out::println);

        list.forEach((e) -> {
            maxAccum.accumulate(e);
            minAccum.accumulate(e);
        });

        System.out.println("=======================\nmax=" + maxAccum.get());
        System.out.println("min=" + minAccum.get());
    }

    private List<Long> createList(int size) {
        Random r = new Random();
        List<Long> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add((long) r.nextInt(200));
        }
        return list;
    }
}
