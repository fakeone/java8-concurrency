package com.github.concurrency.ch10.exercises;

/**
 * Repeat the preceding exercise, but instead have each consumer compile a map of
 * words and their frequencies that are inserted into a second queue.
 * A final thread merges the dictionaries and prints the ten most common words.
 * Why don’t you need to use a ConcurrentHashMap?
 */
//TODO use separate map for every writer?
public class WordsFrequencyMap_10_11 {

    public static void main(String[] args) {
        new WordsFrequencyMap_10_11().execute();
    }

    private void execute() {
        //TODO update 10.10 so it would correspond to it's task completely
        //TODO implement 10.11
    }
}
