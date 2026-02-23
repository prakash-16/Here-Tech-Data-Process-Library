package org.prakash.collector;

import org.prakash.model.Event;
import org.prakash.model.Statistic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ProcessEvents implements Collector<Event, Map<String, Map<Long, Double>>, Map<String, Statistic>> {
    @Override
    public Supplier<Map<String, Map<Long, Double>>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<Map<String, Map<Long, Double>>, Event> accumulator() {
        return (worker, event) -> {
            // Remove Invalid events
            if (event != null && !Double.isNaN(event.value()) && event.value() >= 0){
                worker
                        .computeIfAbsent(event.id(), n -> new HashMap<>())
                        .putIfAbsent(event.timestamp(), event.value());
            }
        };
    }

    @Override
    public BinaryOperator<Map<String, Map<Long, Double>>> combiner() {
        return (leftThread, rightThread) -> {
            for(Map.Entry<String, Map<Long, Double>> entry: rightThread.entrySet()){
                leftThread.merge(
                        entry.getKey(), entry.getValue(),
                        (leftInner, rightInner) -> {
                            rightInner.forEach(leftInner::putIfAbsent);
                            return leftInner;
                        }
                );
            }
            return leftThread;
        };
    }

    @Override
    public Function<Map<String, Map<Long, Double>>, Map<String, Statistic>> finisher() {
        return mainMapping -> {
            Map<String, Statistic> resultSet = new HashMap<>();

            for(Map.Entry<String, Map<Long, Double>> outerMapEntry: mainMapping.entrySet()){
                long count = 0;
                long minTs = Long.MAX_VALUE;
                long maxTs = Long.MIN_VALUE;
                double sum = 0;

                for(Map.Entry<Long, Double> innerMapEntry: outerMapEntry.getValue().entrySet()){
                    long ts = innerMapEntry.getKey();
                    minTs = Math.min(minTs, ts);
                    maxTs = Math.max(maxTs, ts);
                    sum += innerMapEntry.getValue();
                    count++;
                }

                double average = count == 0 ? 0.0 : sum / count;

                resultSet.put(outerMapEntry.getKey(), new Statistic(count, minTs, maxTs, average));
            }
            return resultSet;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    public static Collector<Event, Map<String, Map<Long, Double>>, Map<String, Statistic>> collect(){
        return new ProcessEvents();
    }
}
