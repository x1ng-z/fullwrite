package hs.fullwrite.job;

import com.alibaba.fastjson.JSONObject;
import hs.fullwrite.dao.service.InfluxdbOperateService;
import hs.fullwrite.longTimeServe.InfluxdbWrite;
import hs.fullwrite.longTimeServe.event.InfluxWriteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/2/4 8:26
 */
@Component
public class ThemorDataCollector {
    private Logger logger = LoggerFactory.getLogger(ThemorDataCollector.class);

    @Value("${Themor_DRIVER}")
    private String DRIVER;

    @Value("${Themor_URL}")
    private String URL;
    @Value("${Themor_USER_NAME}")
    private String USER_NAME;

    @Value("${Themor_PASSWORD}")
    private String PASSWORD;

    @Value("${themor_isrun}")
    private boolean themor_isrun;



    @Autowired
    private InfluxdbWrite influxdbWrite;


    @Scheduled(fixedRate = 1000 * 60 * 1, initialDelay = 1000 * 60 * 3)
    @Async
    public void themordata_CustomerTable() {
        if(!themor_isrun){
            return;
        }
        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        ResultSetMetaData data = null;
        try {

            Map<String, String> k_column_v_class = new HashMap<>();
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            //新建一个查询
            stmt = conn.createStatement();
            //执行查询-->>返回一个结果集
            rs = stmt.executeQuery("select TOP 1 * from CustomerTable order by EndTime DESC");    //括号里可以写相关的SQL语句，并把查询到的所有，放到一个rs集合里


//            ResultSetMetaData rss = rs.getMetaData();
//            data = rs.getMetaData();
//            for (int i = 1; i <= data.getColumnCount(); i++) {
//
//                // 获得所有列的数目及实际列数
//                int columnCount = data.getColumnCount();
//// 获得指定列的列名
//                String columnName = data.getColumnName(i);
//// 获得指定列的列值
//                int columnType = data.getColumnType(i);
//// 获得指定列的数据类型名
//                String columnTypeName = data.getColumnTypeName(i);
//// 所在的Catalog名字
//                String catalogName = data.getCatalogName(i);
//// 对应数据类型的类
//                String columnClassName = data.getColumnClassName(i);
//// 在数据库中类型的最大字符个数
//                int columnDisplaySize = data.getColumnDisplaySize(i);
//// 默认的列的标题
//                String columnLabel = data.getColumnLabel(i);
//// 获得列的模式
//                String schemaName = data.getSchemaName(i);
//// 某列类型的精确度(类型的长度)
//                int precision = data.getPrecision(i);
//// 小数点后的位数
//                int scale = data.getScale(i);
//// 获取某列对应的表名
//                String tableName = data.getTableName(i);
//// 是否自动递增
//                boolean isAutoInctement = data.isAutoIncrement(i);
//// 在数据库中是否为货币型
//                boolean isCurrency = data.isCurrency(i);
//// 是否为空
//                int isNullable = data.isNullable(i);
//// 是否为只读
//                boolean isReadOnly = data.isReadOnly(i);
//// 能否出现在where中
//                boolean isSearchable = data.isSearchable(i);
//                System.out.println(columnCount);
//                System.out.println("获得列" + i + "的字段名称:" + columnName);
//                System.out.println("获得列" + i + "的类型,返回SqlType中的编号:" + columnType);
//                System.out.println("获得列" + i + "的数据类型名:" + columnTypeName);
//                System.out.println("获得列" + i + "所在的Catalog名字:" + catalogName);
//                System.out.println("获得列" + i + "对应数据类型的类:" + columnClassName);
//                System.out.println("获得列" + i + "在数据库中类型的最大字符个数:" + columnDisplaySize);
//                System.out.println("获得列" + i + "的默认的列的标题:" + columnLabel);
//                System.out.println("获得列" + i + "的模式:" + schemaName);
//                System.out.println("获得列" + i + "类型的精确度(类型的长度):" + precision);
//                System.out.println("获得列" + i + "小数点后的位数:" + scale);
//                System.out.println("获得列" + i + "对应的表名:" + tableName);
//                System.out.println("获得列" + i + "是否自动递增:" + isAutoInctement);
//                System.out.println("获得列" + i + "在数据库中是否为货币型:" + isCurrency);
//                System.out.println("获得列" + i + "是否为空:" + isNullable);
//                System.out.println("获得列" + i + "是否为只读:" + isReadOnly);
//                System.out.println("获得列" + i + "能否出现在where中:" + isSearchable);
//
//                k_column_v_class.put(columnName, columnClassName);
//            }

            while (rs.next()) {//rs.next()返回的是一个boolean值，这是一个指针，表示查询表头部的的下一条数据，加载第二次就是头部的下一条的下一条，以此类推//如果所需要查询的那一条有数据，就会返回true,没有就返回false


                for (Map.Entry<String, String> entry : k_column_v_class.entrySet()) {
                    String columname = entry.getKey();
                    String classname = entry.getValue();

                    Object value = rs.getObject(columname);
                    System.out.println(value.getClass());
//                    Constructor con=Class.forName(classname).getConstructor(Double.class);
//                    con.newInstance(rs.getObject(columname));
//                    System.out.println(o.toString());
                }
                JSONObject writecontext = new JSONObject();

                float CaO = rs.getFloat("CaO");
                writecontext.put("CustomerTable_CaO", CaO);

                float RecipeSetPointIM = rs.getFloat("RecipeSetPointIM");
                writecontext.put("CustomerTable_RecipeSetPointIM", RecipeSetPointIM);
//                System.out.println("CustomerTable_RecipeSetPointIM"+RecipeSetPointIM);

                float PredictedPointIM = rs.getFloat("PredictedPointIM");
                writecontext.put("CustomerTable_PredictedPointIM", PredictedPointIM);

                float PredictedSetPointSM = rs.getFloat("PredictedSetPointSM");
                writecontext.put("CustomerTable_PredictedSetPointSM", PredictedSetPointSM);

                float RecipeSetPointSM = rs.getFloat("RecipeSetPointSM");
                writecontext.put("CustomerTable_RecipeSetPointSM", RecipeSetPointSM);

                float RecipeSetPointKH = rs.getFloat("RecipeSetPointKH");
                writecontext.put("CustomerTable_RecipeSetPointKH", RecipeSetPointKH);

                float PredictedSetPointKH = rs.getFloat("PredictedSetPointKH");
                writecontext.put("CustomerTable_PredictedSetPointKH", PredictedSetPointKH);

                float RecipeSetPointFe2O3 = rs.getFloat("RecipeSetPointFe2O3");
                writecontext.put("CustomerTable_RecipeSetPointFe2O3", RecipeSetPointFe2O3);

                float PredictedSetPointFe2O3 = rs.getFloat("PredictedSetPointFe2O3");
                writecontext.put("CustomerTable_PredictedSetPointFe2O3", PredictedSetPointFe2O3);

                Timestamp datetime = rs.getTimestamp("EndTime");


                InfluxWriteEvent writeEvent = new InfluxWriteEvent();


                writeEvent.setMeasurement(InfluxdbOperateService.MESMEASUERMENT);
                writeEvent.setTimestamp(datetime.toInstant().toEpochMilli());
                writeEvent.setData(writecontext);
                influxdbWrite.addEvent(writeEvent);
            }
//            if(sum==0){System.out.println("查找正常，没有记录");}//这这是一个逻辑需求，如果数据库没有需要查找的内容，那么这句话怎么说都比白屏好看

        } catch (ClassNotFoundException e) {
            logger.error("驱动问题" + e.getMessage());
        } catch (SQLException e) {
            logger.error("发生异常:" + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();//这三行是关闭连接的意思，这非常重要，如果没写关闭连接
                }
                if (conn != null) {
                    conn.close();//程序多人打开或多人访问，就会出现卡顿，重启或奔溃
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }


    @Scheduled(fixedRate = 1000 * 60 * 1, initialDelay = 1000 * 60 * 3)
    @Async
    public void themordata_CementAnalysis() {
        if(!themor_isrun){
            return;
        }

        ResultSet rs = null;
        Statement stmt = null;
        Connection conn = null;
        ResultSetMetaData data = null;
        try {

            Map<String, String> k_column_v_class = new HashMap<>();
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            //新建一个查询
            stmt = conn.createStatement();
            //执行查询-->>返回一个结果集
            rs = stmt.executeQuery("select TOP 1 * from CementAnalysis order by DateEnd DESC");    //括号里可以写相关的SQL语句，并把查询到的所有，放到一个rs集合里

            while (rs.next()) {//rs.next()返回的是一个boolean值，这是一个指针，表示查询表头部的的下一条数据，加载第二次就是头部的下一条的下一条，以此类推//如果所需要查询的那一条有数据，就会返回true,没有就返回false


                for (Map.Entry<String, String> entry : k_column_v_class.entrySet()) {
                    String columname = entry.getKey();
                    String classname = entry.getValue();

                    Object value = rs.getObject(columname);
                    System.out.println(value.getClass());
                }
                JSONObject writecontext = new JSONObject();

                float Tons = rs.getFloat("Tons");
                writecontext.put("CementAnalysis_Tons", Tons);

                float Tph = rs.getFloat("Tph");
                writecontext.put("CementAnalysis_Tph", Tph);

                float Kgpm = rs.getFloat("Kgpm");
                writecontext.put("CementAnalysis_Kgpm", Kgpm);

                float SiO2 = rs.getFloat("SiO2");
                writecontext.put("CementAnalysis_SiO2", SiO2);

                float Al2O3 = rs.getFloat("Al2O3");
                writecontext.put("CementAnalysis_Al2O3", Al2O3);

                float Fe2O3 = rs.getFloat("Fe2O3");
                writecontext.put("CementAnalysis_Fe2O3", Fe2O3);

                float CaO = rs.getFloat("CaO");
                writecontext.put("CementAnalysis_CaO", CaO);

                float K2O = rs.getFloat("K2O");
                writecontext.put("CementAnalysis_K2O", K2O);

                float SO3 = rs.getFloat("SO3");
                writecontext.put("CementAnalysis_SO3", SO3);

                float MgO = rs.getFloat("MgO");
                writecontext.put("CementAnalysis_MgO", MgO);

                float Na2O = rs.getFloat("Na2O");
                writecontext.put("CementAnalysis_Na2O", Na2O);


                float Mn2O3 = rs.getFloat("Mn2O3");
                writecontext.put("CementAnalysis_Mn2O3", Mn2O3);

                float TiO2 = rs.getFloat("TiO2");
                writecontext.put("CementAnalysis_TiO2", TiO2);

                float Cl = rs.getFloat("Cl");
                writecontext.put("CementAnalysis_Cl", Cl);

                float Moisture = rs.getFloat("Moisture");
                writecontext.put("CementAnalysis_Moisture", Moisture);


                float LOI = rs.getFloat("LOI");
                writecontext.put("CementAnalysis_LOI", LOI);


                Timestamp datetime = rs.getTimestamp("DateEnd");


                InfluxWriteEvent writeEvent = new InfluxWriteEvent();


                writeEvent.setMeasurement(InfluxdbOperateService.MESMEASUERMENT);
                writeEvent.setTimestamp(datetime.toInstant().toEpochMilli());
                writeEvent.setData(writecontext);
                influxdbWrite.addEvent(writeEvent);
            }
//            if(sum==0){System.out.println("查找正常，没有记录");}//这这是一个逻辑需求，如果数据库没有需要查找的内容，那么这句话怎么说都比白屏好看

        } catch (ClassNotFoundException e) {
            logger.error("驱动问题" + e.getMessage());
        } catch (SQLException e) {
            logger.error("发生异常:" + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();//这三行是关闭连接的意思，这非常重要，如果没写关闭连接
                }
                if (conn != null) {
                    conn.close();//程序多人打开或多人访问，就会出现卡顿，重启或奔溃
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }


}
