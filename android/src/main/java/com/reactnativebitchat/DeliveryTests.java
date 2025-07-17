package com.reactnativebitchat;

import org.junit.Test;
import static org.junit.Assert.*;

public class DeliveryTests {
    @Test
    public void testTrackMessageStatus() {
        DeliveryTracker tracker = new DeliveryTracker();
        tracker.trackMessage("msg1", "DELIVERED");
        assertEquals("DELIVERED", tracker.getStatus("msg1"));
    }

    @Test
    public void testGenerateAck() {
        DeliveryTracker tracker = new DeliveryTracker();
        DeliveryAck ack = tracker.generateAck("msg1", "rec1", "nick", 1);
        assertNotNull(ack.id);
        assertEquals("msg1", ack.messageID);
    }

    @Test
    public void testProcessMultipleAcks() {
        DeliveryTracker tracker = new DeliveryTracker();
        tracker.trackMessage("msg1", "PENDING");
        DeliveryAck ack1 = tracker.generateAck("msg1", "rec1", "nick1", 0);
        DeliveryAck ack2 = tracker.generateAck("msg1", "rec2", "nick2", 1);
        tracker.processAck(ack1);
        tracker.processAck(ack2);
        assertEquals(2, tracker.getAcks("msg1").size());
        assertEquals("DELIVERED", tracker.getStatus("msg1"));
    }

    @Test
    public void testUnknownMessageStatus() {
        DeliveryTracker tracker = new DeliveryTracker();
        assertEquals("PENDING", tracker.getStatus("unknown"));
    }

    @Test
    public void testStatusUpdateFromAck() {
        DeliveryTracker tracker = new DeliveryTracker();
        tracker.trackMessage("msg1", "PENDING");
        DeliveryAck ack = tracker.generateAck("msg1", "rec1", "nick", 0);
        tracker.processAck(ack);
        assertEquals("DELIVERED", tracker.getStatus("msg1"));
    }
}