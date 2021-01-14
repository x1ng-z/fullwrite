package hs.fullwrite.dao.service;

import hs.fullwrite.bean.OpcServeInfo;
import hs.fullwrite.dao.mysql.OpcServeOperate;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 15:28
 */
@Service
public class OpcServeOperateService {

    @Autowired
    private OpcServeOperate opcServeOperate;

    @Transactional(isolation = Isolation.READ_COMMITTED, transactionManager = "mysqlTransactionManager")
    public int inserOpcServe(OpcServeInfo opcserve) {
        return opcServeOperate.inserOpcServe(opcserve);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, transactionManager = "mysqlTransactionManager")
    public int deleteOpcServe(int serveid) {
        return opcServeOperate.deleteOpcServe(serveid);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, transactionManager = "mysqlTransactionManager")
    public int updateOpcServe(OpcServeInfo opcserve) {
        return opcServeOperate.updateOpcServe(opcserve);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, transactionManager = "mysqlTransactionManager")
    public OpcServeInfo findOpcServebyid(@Param("serveid") int serveid) {
        return opcServeOperate.findOpcServebyid(serveid);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, transactionManager = "mysqlTransactionManager")
    public List<OpcServeInfo> findAllOpcServe() {
        return opcServeOperate.findAllOpcServe();
    }

}
