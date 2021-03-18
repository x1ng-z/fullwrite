package hs.fullwrite.dao.mysql;

import hs.fullwrite.bean.Point;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:57
 */
@Repository
public interface OpcPointOperate {
    int inserOpcPoint(@Param("point") Point point);

    int deleteOpcPoint(@Param("pointid") int pointid);

    int updateOpcPoint(@Param("point") Point point);

    Point findOpcPointbyid(@Param("pointid") int pointid);

    List<Point> findAllTypePoints();


    List<Point> findAllOpcPointsByServeid(@Param("serveid") int serveid);


    @MapKey("tag")
    Map<String,Point> findAllMesPoints();

    int insertMesPoints(@Param("point") Point point);

   Point findMesPointsByid(@Param("pointid")int pointid);

    int deleteMesPointsByid(@Param("pointid")int pointid);

    int updateMesPoints(@Param("point") Point point);

}
