package hs.fullwrite.dao.service;

import hs.fullwrite.bean.Point;
import hs.fullwrite.dao.mysql.OpcPointOperate;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/4 15:41
 */
@Service
public class OpcPointOperateService {
    @Autowired
    private OpcPointOperate opcPointOperate;

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")
    public  int inserOpcPoint( Point point){
        return opcPointOperate.inserOpcPoint(point);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")
    public int deleteOpcPoint(int pointid){
        return opcPointOperate.deleteOpcPoint(pointid);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")
    public  int updateOpcPoint(Point point){
        return opcPointOperate.updateOpcPoint(point);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")

    public Point findOpcPointbyid(int pointid){
        return opcPointOperate.findOpcPointbyid(pointid);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")


    public List<Point> findAllPoints(){
        return opcPointOperate.findAllTypePoints();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")

    public List<Point> findAllOpcPointsByServeid(int serveid){
        return opcPointOperate.findAllOpcPointsByServeid(serveid);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")
    public Map<String,Point> findAllMesPoints(){
        return opcPointOperate.findAllMesPoints();
    }
    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")

    public int insertMesPoints( Point point){
        return opcPointOperate.insertMesPoints(point);

    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")
    public Point findMesPointsByid(int pointid){
        return opcPointOperate.findMesPointsByid(pointid);

    }
    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")

    public int deleteMesPointsByid(int pointid){
        return opcPointOperate.deleteMesPointsByid(pointid);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,transactionManager="mysqlTransactionManager")
    public int updateMesPoints( Point point){
        return opcPointOperate.updateMesPoints(point);
    }


}
