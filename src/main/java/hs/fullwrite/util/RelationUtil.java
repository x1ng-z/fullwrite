package hs.fullwrite.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/15 16:52
 */
public class RelationUtil {

    /***
     * a,b的集合
     * */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    /**
     * a,b集合的交集
     * **/
    public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<T>(a);
        result.retainAll(b);
        return result;
    }


    /***
     * a去除与b的交集
     * */
    public static <T> Set<T> different(Set<T> superset, Set<T> subset) {
        Set<T> result = new HashSet<T>(superset);
        result.removeAll(subset);
        return result;
    }


    /**
     *ab集合
     * */
    public static <T> Set<T> complement(Set<T> a, Set<T> b) {
        return different(union(a, b), intersection(a, b));
    }

}
