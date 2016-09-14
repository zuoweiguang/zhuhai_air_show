package com.navinfo.mapspotter.foundation.algorithm.rtree;

import com.navinfo.mapspotter.foundation.util.SpatialUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 非叶子节点
 * Created by SongHuiXing on 2016/3/4.
 */
public class NonLeafNode extends TreeNode {

    public NonLeafNode(double[] env, short level){
        super(env);

        this.levelInx = level;
    }

    private short levelInx = 0;

    private TreeNode lb_node = null;
    private TreeNode lt_node = null;
    private TreeNode rb_node = null;
    private TreeNode rt_node = null;

    @Override
    public void insert(EnvlopeIndentifiedObject obj){

        double[] lb;
        double[] lt;
        double[] rb;
        double[] rt;

        if(null != lb_node) {
            lb = lb_node.getNodeEnv();
            lt = lt_node.getNodeEnv();
            rb = rb_node.getNodeEnv();
            rt = rt_node.getNodeEnv();
        } else {
            double midx = (nodeEnv[0] + nodeEnv[2]) / 2;
            double midy = (nodeEnv[1] + nodeEnv[3]) / 2;
            lb = new double[]{nodeEnv[0], nodeEnv[1], midx, midy};
            lt = new double[]{nodeEnv[0], midy, midx, nodeEnv[3]};
            rb = new double[]{midx, nodeEnv[1], nodeEnv[2], midy};
            rt = new double[]{midx, midy, nodeEnv[2], nodeEnv[3]};

            if(levelInx != 1){
                short childLevel = (short)(levelInx-1);
                lb_node = new NonLeafNode(lb, childLevel);
                lt_node = new NonLeafNode(lt, childLevel);
                rb_node = new NonLeafNode(rb, childLevel);
                rt_node = new NonLeafNode(rt, childLevel);
            } else {    //连接到叶子
                lb_node = new TreeNode(lb);
                lt_node = new TreeNode(lt);
                rb_node = new TreeNode(rb);
                rt_node = new TreeNode(rt);
            }
        }

        double[] objEnv = obj.getEnvelope();

        if(SpatialUtil.isEnvelopeIntesect(objEnv, lb)){
            lb_node.insert(obj);
        } else if(SpatialUtil.isEnvelopeIntesect(objEnv, lt)){
            lt_node.insert(obj);
        } else if(SpatialUtil.isEnvelopeIntesect(objEnv, rb)){
            rb_node.insert(obj);
        } else if(SpatialUtil.isEnvelopeIntesect(objEnv, rt)){
            rt_node.insert(obj);
        }
    }

    @Override
    public Map<Long, EnvlopeIndentifiedObject> find(double[] targetEnv){
        HashMap<Long,EnvlopeIndentifiedObject> result = new HashMap<>();

        if(null == lb_node)
            return result;

        if(lb_node.isIntersect(targetEnv)){
            result.putAll(lb_node.find(targetEnv));
        }

        if(lt_node.isIntersect(targetEnv)){
            result.putAll(lt_node.find(targetEnv));
        }

        if(rb_node.isIntersect(targetEnv)){
            result.putAll(rb_node.find(targetEnv));
        }

        if(rt_node.isIntersect(targetEnv)){
            result.putAll(rt_node.find(targetEnv));
        }

        return result;
    }
}
