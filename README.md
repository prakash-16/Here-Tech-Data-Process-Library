### Project Overview

This project implements a reusable library method that accepts the input stream of Events and returns
an aggregated statistic object per id

Event Structure
1. id
2. timestamp (in epoch millis)
3. value

Statistic Structure
1. count of valid events
2. Minimum timestamp
3. Maximum timestamp
4. Average of value


### Build and Run test cases

Using MAVEN as build and dependency management tool, All unit tests are implemented using JUnit 6.
Command: mvn clean test


### Design

#### 1. Custom Collector
        1. A Custom collector is implemented instead of using default methods to create map such as Collectors.groupingBy()
           to have completed control over final statistics.
        2. Explicit accumulator and combiner logic is defined to handle deduplication and event validation for parallel streams.

#### 2. Parallel Stream
        1. Each thread will receive it's own hashmap<>() that is seperate heap objects that will not be shared with other threads,
            to avoid race condition with help of supplier().
        2. Accumulator apply de-duplication logic at worker thread's local memory and doesn't modify other threads, so
            this way we avoid synchronization, ConcurrentHashMap or locks.
        3. There are no mutable state shared across threads.
        4. Those threads whose accumulation is completed reach combiner and since no other thread is available to mutated them afterwards,
            therefore it is thread safe to merge them into one with and return it.

#### 3. Deduplication Strategy
        1. Duplicate timestamps are detected with help of putIfAbsent().
        2. If duplicate timestamp exist for same ID, first one arrived wins rest are ignored.

#### 4. Memory Considerations
        1. We avoid storing full Event objects instead for intermidiate operation we store data in Map<String, Map<Long, Double>>/
        2. Memory usage scales with number of unique (id, timestamp) pairs.
        3. But in final result, we are not storing list of timestamp and memory usage still increases with data size.
            so need to think more about using SET to store unique pairs.


### Assumptions
1. Id in event is not null
2. First occurrence of duplicate timestamp is preserved
3. Collector.Characteristics.Concurrent is not used inside characteristics(), because hashmap is not thread safe and 
    parallel safety is guaranteed if each thread have it's own hashmap and 
    results are merged consistently.
