package com.github.concurrency.ch10.exercises;

/**
 * Write a program that walks a directory tree and generates a thread for each file. In
 * the threads, count the number of words in the files and, without using locks, update a
 * shared counter that is declared as
 * public static long count = 0;
 * Run the program multiple times. What happens? Why?
 */
public class LockFreeWithErrors_10_16 {

    public static void main(String[] args) {
        new LockFreeWithErrors_10_16().execute();
    }

    private void execute() {

    }
}
