package com.redhat.thermostat.server.core.web.setup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;

public class TimedTestSetup {
    protected static final Map<String, List<Long>> times = new HashMap<>();

    @AfterClass
    public static void afterClassTimedTestSetup() throws Exception {
        if (times.size() > 0) {
            for (Map.Entry<String, List<Long>> time : times.entrySet()) {
                List<Long> values = time.getValue();
                double sum = 0;
                long max = values.get(0);
                long min = max;
                for (long t : values) {
                    sum += t;
                    if (max < t) {
                        max = t;
                    }
                    if (min > t) {
                        min = t;
                    }
                }
                double average = (sum - max - min) / values.size();

                System.out.println(time.getKey());
                System.out.println("Average: " + (long) average);
                System.out.println("Max: " + max);
                System.out.println("Min: " + min);
                System.out.println("Sum: " + (long) sum);
                System.out.println();
            }
        }

    }
}
