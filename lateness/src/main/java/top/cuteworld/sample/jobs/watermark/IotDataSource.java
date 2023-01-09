package top.cuteworld.sample.jobs.watermark;

//import lombok.extern.slf4j.Slf4j;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.ArrayList;
import java.util.List;

//@Slf4j
public class IotDataSource implements SourceFunction<IotData> {

    public List<IotData> generate() {
        Long start = System.currentTimeMillis();
        List<IotData> dataList = new ArrayList<IotData>();

        String info = "hey, device";
//        dataList.add(data("d1", start, info)); // 第0秒

        dataList.add(data("d1", start + 1000, "1"));
        p1();
        dataList.add(data("d1", start + 2400, "2"));
        dataList.add(data("d1", start + 4500, "3"));
        //后面两个未能在5s内进入的 EventTime是在5s内， 但是Ingest Time未能在5S内， 因为网络等原因延迟了
        dataList.add(data("d1", start + 5200, "4"));
        dataList.add(data("d1", start + 6700, "5"));

        return dataList;
    }

    private void p1() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IotData data(String deviceId, Long time, String info) {
        return new IotData(deviceId, time, info);
    }

    @Override
    public void run(SourceContext<IotData> ctx) throws Exception {
        String info = "hey, device";
        Long start = System.currentTimeMillis();
        ctx.collect(data("d1", start, info)); // 第0秒

        ctx.collect(data("d1", start + 1000, "1"));
        ctx.collect(data("d1", start + 2400, "2"));
        ctx.collect(data("d1", start + 4500, "3"));
        //后面两个未能在5s内进入的 EventTime是在5s内， 但是Ingest Time未能在5S内， 因为网络等原因延迟了
        ctx.collect(data("d1", start + 5200, "4"));
        ctx.collect(data("d1", start + 6700, "5"));
//        ctx.collect();
    }

    @Override
    public void cancel() {

    }
}
