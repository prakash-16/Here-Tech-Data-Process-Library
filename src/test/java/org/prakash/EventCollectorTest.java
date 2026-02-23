package org.prakash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.prakash.collector.ProcessEvents;
import org.prakash.model.Event;
import org.prakash.model.Statistic;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class EventCollectorTest {

    @Test
    @DisplayName("Test case 1: Empty input should return empty map")
    void testEmptyInput(){
        List<Event> eventList = List.of();
        Map<String, Statistic> result = eventList.stream().collect(ProcessEvents.collect());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test case 2: Single input")
    void testSingleEvent(){
        List<Event> eventList = List.of(new Event("A", 100L, 10.0));
        Map<String, Statistic> result = eventList.stream().collect(ProcessEvents.collect());

        Statistic stats = result.get("A");
        assertEquals(1, stats.count());
        assertEquals(100L, stats.minTimestamp());
        assertEquals(100L, stats.maxTimestamp());
        assertEquals(10.0, stats.avgValue());
    }

    @Test
    @DisplayName("Test case 3: Multiple events with same ID and different timestamp should be aggregated")
    void testMultipleEventsSameId(){
        List<Event> eventList = List.of(
                new Event("A", 100L, 10.0),
                new Event("A", 200L, 20.0),
                new Event("A", 300L, 30.0)
        );
        Map<String, Statistic> result = eventList.stream().collect(ProcessEvents.collect());
        Statistic stats = result.get("A");
        assertEquals(3, stats.count());
        assertEquals(100L, stats.minTimestamp());
        assertEquals(300L, stats.maxTimestamp());
        assertEquals(20.0, stats.avgValue());
    }

    @Test
    @DisplayName("Test case 4: Ignore duplicate timestamps")
    void testDuplicateTimeStamp(){
        List<Event> eventList = List.of(
                new Event("A", 100L, 10.0),
                new Event("A", 100L, 999.0)
        );
        Map<String, Statistic> result = eventList.stream().collect(ProcessEvents.collect());
        Statistic stats = result.get("A");
        assertEquals(1, stats.count());
        assertEquals(100L, stats.minTimestamp());
        assertEquals(100L, stats.maxTimestamp());
        assertEquals(10.0, stats.avgValue()); // first value should win
    }

    @Test
    @DisplayName("Test case 5: Sequential and parallel results should be same")
    void testParallelConsistency(){
        List<Event> eventList = List.of(
                new Event("A", 100L, 10.0),
                new Event("A", 200L, 20.0),
                new Event("B", 300L, 30.0),
                new Event("B", 400L, 40.0)
        );
        Map<String, Statistic> sequential = eventList.stream().collect(ProcessEvents.collect());
        Map<String, Statistic> parallel = eventList.parallelStream().collect(ProcessEvents.collect());

        assertEquals(sequential, parallel);
    }

    @Test
    @DisplayName("Test case 6: Large data set parallel stress test")
    void testLargeParallelDataSet(){
        List<Event> eventList = IntStream.range(0, 100_000).mapToObj(n -> new Event("A", n, 1.0)).toList();

        Map<String, Statistic> result = eventList.parallelStream().collect(ProcessEvents.collect());
        Statistic stats = result.get("A");
        assertEquals(100_000, stats.count());
        assertEquals(0L, stats.minTimestamp());
        assertEquals(99_999L, stats.maxTimestamp());
        assertEquals(1.0, stats.avgValue());
    }

}
