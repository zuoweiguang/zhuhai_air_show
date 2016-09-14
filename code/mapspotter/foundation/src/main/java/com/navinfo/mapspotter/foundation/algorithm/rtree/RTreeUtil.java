package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.util.LinkedList;
import java.util.List;

/**
 * RTree 类
 * Created by ZhangJin1207 on 2016/4/6.
 */
public class RTreeUtil<T> {

    private int minEnties;
    private int maxEnties;
    private int size;
    private RTreeBaseNode root = new RTreeLeafNode(maxEnties);

    public RTreeUtil(){

    }

    public RTreeUtil(int minentries , int maxentries){
        minEnties = minentries;
        maxEnties = maxentries;

        root = new RTreeLeafNode(maxEnties);
    }

    public int getSize(){
        return size;
    }

    /**
     * 增加节点
     * @param envelope 节点外接矩形
     * @param object    节点对象
     */
    public void Add(Envelope envelope , T object){
        LinkedList<RTreeBranchNode<T>> path = new LinkedList<RTreeBranchNode<T>>();
        RTreeLeafNode<T> leafNode = ChooseLeafNode(envelope , root , path);
        if (leafNode.getSize() == maxEnties) {
            //////调整树
            List<RTreeBaseNode<T>> newNodes = leafNode.Split(envelope , object);
            Adjust(path , leafNode , newNodes);

        }else{
            leafNode.Add(envelope , object);
        }

        size++;
    }

    /**
     * 计算剩余空间面积
     * @param envelope 新增点外接矩形
     * @param node     期望加入节点
     * @return
     */
    private double getRequiredExpansion(Envelope envelope , RTreeBaseNode<T> node){
        double retExpansion = 0.0;

        double dminx1 = envelope.getMinX();
        double dminy1 = envelope.getMinY();
        double dmaxx1 = envelope.getMaxX();
        double dmaxy1 = envelope.getMaxY();

        double dminx2 = node.getMinX();
        double dminy2 = node.getMinY();
        double dmaxx2 = node.getMaxX();
        double dmaxy2 = node.getMaxY();

        double dwidth = Math.max(dmaxx1 , dmaxx2) - Math.min(dminx1 , dminx2);
        double dheight = Math.max(dmaxy1 , dmaxy2) - Math.min(dminy1 , dminy2);

        if (dminx2 > dminx1){
            retExpansion += (dminx2 - dminx1) * dheight;
        }
        if (dmaxx1 > dmaxx2){
            retExpansion += (dmaxx1 - dmaxx2) * dheight;
        }

        if (dminy2 > dminy1){
            retExpansion += (dminy2 - dminy1) * dwidth;
        }

        if (dmaxy1 > dmaxy2){
            retExpansion += (dmaxy1 - dmaxy2) * dwidth;
        }

        return retExpansion;
    }

    /**
     * 选择叶子节点
     * @param envelope
     * @param node
     * @param path
     * @return
     */

    private RTreeLeafNode<T> ChooseLeafNode(Envelope envelope , RTreeBaseNode<T> node , LinkedList<RTreeBranchNode<T>> path){
        if (node instanceof RTreeLeafNode){
            return (RTreeLeafNode<T>)node;
        }

        RTreeBranchNode<T> branch = (RTreeBranchNode<T>)node;
        branch.expandToInclude(envelope);
        path.add(branch);

        double minExpansion = Float.MAX_VALUE;
        RTreeBaseNode<T> next = null;

        for (RTreeBaseNode<T> child : branch.getNodes()){
            double dExpansion = getRequiredExpansion(envelope , child);
            if (dExpansion < minExpansion){
                minExpansion = dExpansion;
                next = child;
            }else if (dExpansion == minExpansion){
                double narea = next.getArea();
                double carea = child.getArea();

                if (carea < narea){
                    next = child;
                }
            }
        }

        return ChooseLeafNode(envelope , next , path);
    }

    /**
     * 调整树结构
     * @param path
     * @param oldNode
     * @param newNodes
     */

    private void Adjust(LinkedList<RTreeBranchNode<T>> path , RTreeBaseNode<T> oldNode , List<RTreeBaseNode<T>> newNodes){
        if (path.isEmpty()){
            root = new RTreeBranchNode<T>(maxEnties , newNodes);
        }else{
            RTreeBranchNode<T> parentNode = path.removeLast();
            if (parentNode.getSize() + newNodes.size() - 1 >= maxEnties){
                List<RTreeBaseNode<T>> newSNodes = parentNode.Split(oldNode , newNodes);
                Adjust(path , parentNode , newSNodes);
            }else{
                parentNode.Adjust(oldNode , newNodes);
            }
        }
    }

    /**
     * 空间关系计算
     * @param envelope 查询对象外界矩形
     * @param geometry 查询对象几何
     * @param filter   查询过滤器
     * @return         空间查询所得对象
     */

    public List<T> Relate(Envelope envelope , Geometry geometry , RelateFilter<T> filter){
        return root.Relate(envelope ,geometry , filter);
    }
    /**
     * 空间关系计算
     * @param coordinate 查询点坐标
     * @param geometry 查询对象几何
     * @param filter   查询过滤器
     * @return         空间查询所得对象
     */
    public List<T> Relate(Coordinate coordinate , Geometry geometry , RelateFilter<T> filter){
        return root.Relate(coordinate , geometry , filter);
    }
}
