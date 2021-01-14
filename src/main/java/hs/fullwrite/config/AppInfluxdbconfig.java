package hs.fullwrite.config;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/9/16 14:40
 */
@Configuration
public class AppInfluxdbconfig {

    @Value("${spring.influx.database}")
    private String influxdatabase;
    @Value("${spring.influx.url}")
    private String influxurl;
    @Value("${spring.influx.user}")
    private String influxuser;
    @Value("${spring.influx.password}")
    private String influxpassword;
    @Value("${spring.influx.retentionPolicy}")
    private String influxretentionPolicy;


    @Bean(destroyMethod = "close")
    public InfluxDB influxDb() {
        InfluxDB influxDB = InfluxDBFactory.connect(influxurl, influxuser, influxpassword);
        influxDB.setDatabase(influxdatabase);
        influxDB.setRetentionPolicy(influxretentionPolicy);
        influxDB.enableBatch(BatchOptions.DEFAULTS);
        return influxDB;
    }







}
