package top.cuteworld.flink.sample.table;

import org.apache.commons.io.IOUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

/**
 * 官方文档
 * https://nightlies.apache.org/flink/flink-docs-release-1.13/zh/docs/try-flink/table_api/
 */
public class RealtimeTableReportJob {

    public static void main(String[] args) {

        Configuration configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8082);
        EnvironmentSettings settings = EnvironmentSettings.newInstance().withConfiguration(configuration).build();
        TableEnvironment tEnv = TableEnvironment.create(settings);
//        readSQL();

        tEnv.executeSql(readSQL("source_table.sql"));

        tEnv.executeSql(readSQL("report_table.sql"));

        Table transactions = tEnv.from("transactions");
        report(transactions).executeInsert("spend_report");
    }

    private static String readSQL(String sqlFile) {
        try {
            Optional<String> reduce = IOUtils.readLines(RealtimeTableReportJob.class.getClassLoader().getResourceAsStream(sqlFile), Charset.defaultCharset()).stream().reduce(new BinaryOperator<String>() {
                @Override
                public String apply(String s, String s2) {
                    return s + "\r\n" + s2;
                }
            });
            return reduce.get();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read sql from file: " + sqlFile, e);
        }
    }

    public static Table report(Table transactions) {
        return transactions.select(
                        $("account_id"),
                        call(MyFloor.class, $("transaction_time")).as("log_ts"),
                        $("amount"))
                .groupBy($("account_id"), $("log_ts"))
                .select(
                        $("account_id"),
                        $("log_ts"),
                        $("amount").sum().as("amount"));
    }

}
