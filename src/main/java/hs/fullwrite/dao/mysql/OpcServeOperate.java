package hs.fullwrite.dao.mysql;

import hs.fullwrite.bean.OpcServeInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 14:34
 */
@Repository
public interface OpcServeOperate {
    int inserOpcServe(@Param("opcserve") OpcServeInfo opcserve);

    int deleteOpcServe(@Param("serveid") int serveid);

    int updateOpcServe(@Param("opcserve") OpcServeInfo opcserve);

    OpcServeInfo findOpcServebyid(@Param("serveid") int serveid);

    List<OpcServeInfo> findAllOpcServe();
}
