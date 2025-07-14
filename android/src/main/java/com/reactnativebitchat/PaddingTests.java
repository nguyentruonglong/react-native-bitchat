package com.reactnativebitchat;

import org.junit.Test;
import static org.junit.Assert.*;

public class PaddingTests {
    @Test
    public void testBasicPadding() {
        byte[] data = "test".getBytes();
        int targetSize = Message.optimalBlockSize(data.length);
        byte[] padded = Message.pad(data, targetSize);
        byte[] unpadded = Message.unpad(padded);
        assertArrayEquals(data, unpadded);
    }

    @Test
    public void testMultipleBlockSizes() {
        byte[] data = new byte[300];
        int size256 = Message.optimalBlockSize(data.length);
        int size512 = Message.optimalBlockSize(data.length + 256);
        assertEquals(512, size512);
        assertTrue(size256 <= 256);
    }

    @Test
    public void testLargeData() {
        byte[] data = new byte[1000];
        int targetSize = Message.optimalBlockSize(data.length);
        byte[] padded = Message.pad(data, targetSize);
        assertTrue(padded.length <= data.length + 255);
    }

    @Test
    public void testInvalidPadding() {
        byte[] data = "test".getBytes();
        byte[] invalid = Arrays.copyOf(data, data.length + 1);
        invalid[invalid.length - 1] = (byte) 256; // Invalid padding size
        byte[] unpadded = Message.unpad(invalid);
        assertArrayEquals(invalid, unpadded); // Returns original on invalid
    }

    @Test
    public void testRandomness() {
        byte[] data1 = "test".getBytes();
        byte[] data2 = "test".getBytes();
        int targetSize = Message.optimalBlockSize(data1.length);
        byte[] padded1 = Message.pad(data1, targetSize);
        byte[] padded2 = Message.pad(data2, targetSize);
        assertNotEquals(Arrays.hashCode(padded1), Arrays.hashCode(padded2)); // Random padding differs
    }
}