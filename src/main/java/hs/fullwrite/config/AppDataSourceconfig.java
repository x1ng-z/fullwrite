package hs.fullwrite.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/10/8 11:15
 */
@Configuration
public class AppDataSourceconfig {

    @Bean(value="mysqlresource")
//    @ConfigurationProperties(prefix = "c3p0")
    public DataSource createMysqlDataSource(){
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        return dataSource;
    }

//    @Bean("oracleresource")
////    @ConfigurationProperties(prefix = "c3p0")
//    public DataSource createOrcleDataSource(){
//        ComboPooledDataSource dataSource = new ComboPooledDataSource("mesoracle");
//        return dataSource;
//    }


}
