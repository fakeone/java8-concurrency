package com.github.concurrency.ch10.exercises;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In a ConcurrentHashMap<String, Long>, find the key with maximum
 * value (breaking ties arbitrarily). Hint: reduceEntries.
 */
public class FindMapInMap_10_7 {

    private ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
    private Random random = new Random();

    public static void main(String[] args) {
        new FindMapInMap_10_7().execute();
    }

    private void execute() {
        fillMap(10);
        map.forEach((k, v) -> System.out.println(k + "\t- " + v));

        Map.Entry<String, Long> reduced = reduceToMax();

        System.out.println("\n=======================\nresult:");
        System.out.println(reduced.getKey() + "\t- " + reduced.getValue());
    }

    private Map.Entry<String, Long> reduceToMax() {
        return map.reduceEntries(16, (prevEntry, currentEntry) -> {
            String key;
            if (currentEntry.getValue() > prevEntry.getValue()) {
                key = currentEntry.getKey();
            } else {
                key = prevEntry.getKey();
            }
            return new ConcurrentHashMap.SimpleEntry<>(key, Math.max(prevEntry.getValue(), currentEntry.getValue()));
        });
    }

    private void fillMap(int size) {
        for (int i = 0; i < size; i++) {
            map.put(UUID.randomUUID().toString(), (long) random.nextInt(20));
        }
    }
}
