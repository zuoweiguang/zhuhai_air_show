package com.navinfo.mapspotter.foundation.algorithm.string;

import org.junit.Test;

import java.util.*;

/**
 * Created by SongHuiXing on 2016/3/23.
 */
public class SimpleCountClusterTest {

    @Test
    public void testGetSimpleTrack() throws Exception {
        SimpleCountCluster cluster = new SimpleCountCluster(255);

        Comparator<Map.Entry<Integer,Integer>>
                hitComparator = new Comparator<Map.Entry<Integer,Integer>>(){
            public int compare(Map.Entry<Integer,Integer> hit1,
                               Map.Entry<Integer,Integer> hit2) {
                return hit1.getValue() - hit2.getValue();
            }
        };

        ArrayList<Map.Entry<Integer, Integer>> coll = new ArrayList<>();
        coll.add(new AbstractMap.SimpleEntry<>(2, 5));
        coll.add(new AbstractMap.SimpleEntry<>(1, 1));
        coll.add(new AbstractMap.SimpleEntry<>(3, 3));
        coll.add(new AbstractMap.SimpleEntry<>(4, 10));

        Collections.sort(coll, hitComparator);
        Map.Entry<Integer, Integer> maxEntry = Collections.max(coll, hitComparator);
        System.out.println(String.format("%d : %d", maxEntry.getKey(), maxEntry.getValue()));

        for (Map.Entry<Integer, Integer> kv : coll){
            System.out.println(String.format("%d : %d", kv.getKey(), kv.getValue()));
        }
    }
}