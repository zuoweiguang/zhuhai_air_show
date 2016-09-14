package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 非叶子节点类
 * Created by ZhangJin1207 on 2016/4/5.
 */
public class RTreeBranchNode<T> extends RTreeBaseNode<T>{

    private RTreeBaseNode<T>[] nodes;
    private int size = 0;

    public RTreeBranchNode(){

    }

    public RTreeBranchNode(int isize){
        nodes = (RTreeBaseNode<T>[]) Array.newInstance(RTreeBaseNode.class , isize);
    }

    public RTreeBranchNode(int isize , List<RTreeBaseNode<T>> list){
        this(isize);
        for (RTreeBaseNode<T> object : list){
            Add(object);
        }
    }

    /**
     * 添加节点
     * @param node
     */
    public void Add(RTreeBaseNode<T> node){
        nodes[size] = node;
        size++;
        expandToInclude(node);
    }

    public List<RTreeBaseNode<T>> getNodes(){
        List<RTreeBaseNode<T>> rlist = new ArrayList<RTreeBaseNode<T>>();
        for (int i = 0 ; i < size ; i ++){
            rlist.add(nodes[i]);
        }
        return rlist;
    }

    public  int getSize(){
        return size;
    }

    /**
     * 节点分裂
     * @param node
     * @param newNodes
     * @return
     */
    public List<RTreeBaseNode<T>> Split(RTreeBaseNode<T> node , List<RTreeBaseNode<T>> newNodes){
        RTreeBranchNode<T> branch1 = new RTreeBranchNode<T>(nodes.length);
        RTreeBranchNode<T> branch2 = new RTreeBranchNode<T>(nodes.length);

        int midPoint = (int)Math.ceil(size / 2.0);
        for (int i = 0 ; i <= midPoint ; i++){
            if (node == nodes[i]){
                branch1.Add(newNodes.get(0));
            }else{
                branch1.Add(nodes[i]);
            }
        }

        for (int j = midPoint + 1 ; j < size ; j++){
            if (node == nodes[j]){
                branch1.Add(newNodes.get(0));
            }else{
                branch2.Add(nodes[j]);
            }
        }

        branch2.Add(newNodes.get(1));
        return Arrays.<RTreeBaseNode<T>>asList(branch1 , branch2);
    }

    /**
     * 调整节点
     * @param node
     * @param newNodes
     */
    public void Adjust(RTreeBaseNode<T> node , List<RTreeBaseNode<T>> newNodes){
        for (int i = 1 ; i < newNodes.size() ; i++){
            Add(newNodes.get(i));
        }

        for (int j = 0 ; j < size - newNodes.size() + 1 ; j++){
            if (node == nodes[j]){
                nodes[j] = newNodes.get(0);
                return;
            }
        }
    }

    /**
     * 更新节点外接矩形
     */
    @Override
    public void UpdateEnvelop(){
        init();
        for (int i = 0 ; i < size ; i ++){
            expandToInclude(nodes[i]);
        }
    }

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
        for (int i = 0 ; i < size ; i++){
            RTreeBaseNode<T> node = nodes[i];
            if (envelope.intersects(node)){
                List<T> tList = node.Relate(envelope ,geometry , filter);
                rList.addAll(rList.size() , tList);
            }
        }
        return rList;
    }
    @Override
    public List<T> Relate(Coordinate coordinate, Geometry geometry , RelateFilter<T> filter){
        List<T> rList = new ArrayList<T>();
        for (int i = 0 ; i < size ; i++){
            RTreeBaseNode<T> node = nodes[i];
            if (node.intersects(coordinate)){
                List<T> tList = node.Relate(coordinate ,geometry , filter);
                rList.addAll(rList.size() , tList);
            }
        }
        return rList;
    }
}
