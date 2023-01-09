package top.cuteworld.sample.jobs.userbehaivor.clickcount;

import org.apache.flink.api.common.functions.AggregateFunction;

public class CountAggregate<IN, ACC, OUT> implements AggregateFunction<UserBehaviorItem, Long, Long> {

    @Override
    public Long createAccumulator() {
        return 0l;
    }

    @Override
    public Long add(UserBehaviorItem value, Long accumulator) {
        System.out.println("The acc is " + accumulator + " with element " + value);
        return accumulator + 1;
    }

    @Override
    public Long getResult(Long accumulator) {
        return accumulator;
    }

    @Override
    public Long merge(Long a, Long b) {
        return a + b;
    }
}
