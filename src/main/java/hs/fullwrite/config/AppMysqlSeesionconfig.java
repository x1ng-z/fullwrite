package hs.fullwrite.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/10/8 11:30
 */
@Configuration
@MapperScan(basePackages = {"hs.fullwrite.dao.mysql"},annotationClass = Repository.class,sqlSessionFactoryRef = "mysqlsessionfactory" )
public class AppMysqlSeesionconfig {
    @Value("${mybatis.mapper-locations-mysql}")
    private String mapperXMLConfigPath;
    @Value("${mybatis.type-aliases-package}")
    private String mapperPackagePath;
    @Autowired
    @Qualifier("mysqlresource")
    private DataSource dataSource;

    @Bean("mysqlsessionfactory")
    @Primary
    public SqlSessionFactoryBean createSqlSessionFactory() throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        String packageXMLConfigPath = PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + mapperXMLConfigPath;

        // 设置 mapper 对应的 XML 文件的路径
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(mapperXMLConfigPath));
        // 设置数据源
        sqlSessionFactoryBean.setDataSource(dataSource);
        // 设置 mapper 接口所在的包
        sqlSessionFactoryBean.setTypeAliasesPackage(mapperPackagePath);

        return sqlSessionFactoryBean;
    }



    @Bean(name = "mysqlTransactionManager")
    @Primary
    public DataSourceTransactionManager masterTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}
