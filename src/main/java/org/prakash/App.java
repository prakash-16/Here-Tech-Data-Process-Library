package org.prakash;

import org.prakash.collector.ProcessEvents;
import org.prakash.model.Event;
import org.prakash.model.Statistic;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        List<Event> events = List.of(
                new Event("A", 1000L, 10.0),
                new Event("A", 1001L, 20.0),
                new Event("A", 1001L, 99.0),     // duplicate timestamp (should keep first)
                new Event("A", 1002L, -5.0),     // negative (invalid)
                new Event("A", 1003L, Double.NaN), // NaN (invalid)

                new Event("B", 2000L, 5.0),
                new Event("B", 2001L, 15.0),
                new Event("B", 2001L, 8.0),
                new Event("B", 2002L, 25.0),

                new Event("C", 3000L, 50.0),     // single event id

                new Event("D", 4000L, -10.0),    // all invalid id
                new Event("D", 4001L, Double.NaN)
        );
        Map<String, Statistic> result = events.stream().collect(ProcessEvents.collect());
        for(Map.Entry<String, Statistic> entry : result.entrySet()){
            System.out.println(entry.getKey());
            System.out.println(entry.getValue().toString());
        }
    }
}
