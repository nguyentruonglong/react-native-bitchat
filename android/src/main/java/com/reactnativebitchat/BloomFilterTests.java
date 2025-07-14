package com.reactnativebitchat;

import org.junit.Test;
import static org.junit.Assert.*;

public class BloomFilterTests {
    @Test
    public void testInsertionAndLookup() {
        BloomFilter filter = new BloomFilter(1000, 0.01);
        filter.insert("test");
        assertTrue(filter.mightContain("test"));
        assertFalse(filter.mightContain("notest"));
    }

    @Test
    public void testFalsePositiveRate() {
        BloomFilter filter = new BloomFilter(100, 0.01);
        for (int i = 0; i < 50; i++) filter.insert("item" + i);
        int falsePositives = 0;
        for (int i = 50; i < 100; i++) if (filter.mightContain("item" + i)) falsePositives++;
        assertTrue(falsePositives / 50.0 < 0.02); // Should be below target 0.01 with some margin
    }

    @Test
    public void testReset() {
        BloomFilter filter = new BloomFilter(100, 0.01);
        filter.insert("test");
        filter.reset();
        assertFalse(filter.mightContain("test"));
    }

    @Test
    public void testHashDistribution() {
        BloomFilter filter = new BloomFilter(100, 0.01);
        int[] hashCounts = new int[filter.getHashCount()];
        for (int i = 0; i < 100; i++) {
            int[] hashes = filter.getHashes("item" + i);
            for (int hash : hashes) hashCounts[hash % hashCounts.length]++;
        }
        for (int count : hashCounts) assertTrue(count > 0); // Ensure all hashes are used
    }

    @Test
    public void testAdaptiveSizing() {
        BloomFilter filter = new BloomFilter(100, 0.01);
        for (int i = 0; i < 200; i++) filter.insert("item" + i);
        assertTrue(filter.getSize() > 100); // Should grow
    }
}