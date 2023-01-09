package top.cuteworld.sample.jobs.userbehaivor.clickcount;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class UADeserializationSchema implements DeserializationSchema<UserBehaviorItem> {
    final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public UserBehaviorItem deserialize(byte[] message) throws IOException {
//        System.out.println("---- " + new String(message));
        return objectMapper.readValue(message, UserBehaviorItem.class);
    }

    @Override
    public boolean isEndOfStream(UserBehaviorItem nextElement) {
        return false;
    }

    @Override
    public TypeInformation<UserBehaviorItem> getProducedType() {
        return TypeInformation.of(UserBehaviorItem.class);
    }
}
