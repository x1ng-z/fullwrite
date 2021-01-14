package hs.fullwrite.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 18:52
 */
public class test4string {
    public static void main(String[] args) {


        System.out.println(System.currentTimeMillis());

        JSONArray jsonArray=new JSONArray();
        JSONObject res= new JSONObject();
        res.put("a","1");
        jsonArray.add(res);


        JSONObject res2= new JSONObject();
        res2.put("b","2");
        jsonArray.add(res2);

        System.out.println(jsonArray.toJSONString());


        List<String> po=new ArrayList<>();
        po.add("1");
        po.add("2");



        res.put("points",po);
        System.out.println(res.toJSONString());
        String aa="a";
        System.out.println(aa.length());
    }
}
