package top.cuteworld.sample.jobs.watermark;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;

public class CountWindowAmount extends RichMapFunction<IotData, Long> {

    private transient ValueState<Long> sum;


    //State有效期配置
    StateTtlConfig ttlConfig = StateTtlConfig
            .newBuilder(Time.seconds(2))
            .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
            .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
            .build();


    @Override
    public Long map(IotData value) throws Exception {
        Long currentSum = sum.value();
        Long newValue = currentSum + 1;
        sum.update(newValue);
        return newValue;
    }

    @Override
    public void open(Configuration config) {
        ValueStateDescriptor<Long> descriptor =
                new ValueStateDescriptor<>(
                        "average", // the state name
                        TypeInformation.of(new TypeHint<Long>() {
                        }), // type information
                        0l); // default value of the state, if nothing was set
//        descriptor.enableTimeToLive(ttlConfig);
        sum = getRuntimeContext().getState(descriptor);
    }


}
