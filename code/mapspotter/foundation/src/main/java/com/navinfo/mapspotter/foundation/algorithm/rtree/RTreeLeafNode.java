package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 叶子节点类
 * Created by ZhangJin1207 on 2016/4/5.
 */
public class RTreeLeafNode<T> extends RTreeBaseNode<T> {

    private Envelope[] envelopes;

    private Object[] objects;

    private int size = 0;

    public RTreeLeafNode(){};

    public RTreeLeafNode(int size){
        envelopes = new Envelope[size];
        objects = new Object[size];
    }

    public T getObject(int index){
        return (T)objects[index];
    }

    public int getSize(){
        return size;
    }

    /**
     * 增加叶子节点
     * @param envelope
     * @param object
     */
    public void Add(Envelope envelope , T object){
        envelopes[size] = envelope;
        objects[size] = object;
        size++;
        expandToInclude(envelope);
    }

    /**
     * 节点分裂
     * @param envelope
     * @param object
     * @return
     */
    public List<RTreeBaseNode<T>> Split(Envelope envelope , T object){
        RTreeLeafNode<T> leafNode1 = new RTreeLeafNode<T>(objects.length);
        RTreeLeafNode<T> leafNode2 = new RTreeLeafNode<T>(objects.length);

        int midPoint = (int)Math.ceil(size / 2.0);

        for (int i = 0 ; i <= midPoint ; i++){
            leafNode1.Add(envelopes[i] , getObject(i));
        }

        for (int j = midPoint + 1 ; j < size ; j++){
            leafNode2.Add(envelopes[j] , getObject(j));
        }

        leafNode2.Add(envelope , object);

        return Arrays.<RTreeBaseNode<T>>asList(leafNode1 , leafNode2);
    }

    /**
     * 更新节点外接矩形
     */
    @Override
    public void UpdateEnvelop(){
        init();
        for (int i = 0 ; i < size ; i++){
            Envelope envelope = envelopes[i];
            expandToInclude(envelope);
        }
    };

    /**
     * 空间查询
     * @param envelope
     * @param geometry
     * @param filter
     * @return
     */
    @Override
    public List<T> Relate(Envelope envelope , Geometry geometry , RelateFilter<T> filter){
        List<T> rList = new ArrayList<T>();
        for (int i = 0 ; i < size ; i++) {
            if (envelope.intersects(envelopes[i])){
                T object = getObject(i);
                if (filter.accept(object , geometry)){
                    rList.add(object);
                }
            }
        }
        return rList;
    };

    @Override
    public List<T> Relate(Coordinate coordinate ,Geometry geometry , RelateFilter<T> filter){
        List<T> rList = new ArrayList<T>();
        for (int i = 0 ; i < size ; i++) {
            if (envelopes[i].intersects(coordinate)){
                T object = getObject(i);
                if (filter.accept(object , geometry)){
                    rList.add(object);
                }
            }
        }
        return rList;
    }
}
