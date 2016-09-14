package com.navinfo.mapspotter.foundation.algorithm.string;

import java.util.*;

/**
 * 简单的数量字符串聚类
 * Created by SongHuiXing on 2016/1/6.
 */
public class SimpleCountCluster {
    public static boolean ShouldIgnoreZero = true;

    private static Comparator<Map.Entry<Integer,Integer>>
            hitComparator = new Comparator<Map.Entry<Integer,Integer>>(){
                                public int compare(Map.Entry<Integer,Integer> hit1,
                                                   Map.Entry<Integer,Integer> hit2) {
                                    return hit1.getValue() - hit2.getValue();
                                }
                            };

    private int flagHit = 255;
    private boolean m_hadHitFlag = false;

    public SimpleCountCluster(int flag){
        flagHit = flag;
    }

    public SimpleCountCluster(String original){
        for(char c : original.toCharArray()){
            insertHit(Integer.parseInt(String.valueOf(c)));
        }
    }

    public SimpleCountCluster(String original, char flag){
        flagHit = Integer.parseInt(String.valueOf(flag));

        for(char c : original.toCharArray()){
            insertHit(Integer.parseInt(String.valueOf(c)));
        }
    }

    public SimpleCountCluster(int[] original, int flag){
        flagHit = flag;

        for(int hit : original){
            insertHit(hit);
        }
    }

    private ArrayDeque<Map.Entry<Integer,Integer>> m_trackqueue = new ArrayDeque<>();

    public void insertHit(int hit){
        if(ShouldIgnoreZero && hit == 0)
            return;

        Map.Entry<Integer, Integer> last = m_trackqueue.peekLast();

        if(null != last && last.getKey() == hit){
            last.setValue(last.getValue()+1);
        }else{
            m_trackqueue.addLast(new AbstractMap.SimpleEntry<>(hit, 1));
        }
    }

    public void insertHit(int hit , int val){
        if(ShouldIgnoreZero && hit == 0)
            return;

        Map.Entry<Integer, Integer> last = m_trackqueue.peekLast();

        if(null != last && last.getKey() == hit){
            last.setValue(last.getValue()+val);
        }else{
            m_trackqueue.addLast(new AbstractMap.SimpleEntry<>(hit, val));
        }
    }

    private Map.Entry<Integer,Integer> m_leftHit = null;
    private Map.Entry<Integer,Integer> m_rightHit = null;

    /**
     * 统计字符串在flag左右两边的出现次数
     * @return
     */
    private boolean satisticLeftAndRight(){
        if(0 == m_trackqueue.size())
            return false;

        m_leftHit = getBiggestHit(true, flagHit);

        if(m_hadHitFlag){
            m_rightHit = getBiggestHit(false, flagHit);
        } else{
            m_rightHit = getBiggestHit(false, null != m_leftHit ? m_leftHit.getKey() : flagHit);
        }

        return true;
    }

    /**
     * 获取总击中次数最多的记录
     * @param isLeft 路口左侧击中点??
     * @param flag 停止查找的标志
     * @return
     */
    private Map.Entry<Integer,Integer> getBiggestHit(boolean isLeft, int flag){
        Map<Integer,Integer> hitCount = new HashMap<>();

        Iterator<Map.Entry<Integer,Integer>> desIter;
        if(isLeft)
            desIter = m_trackqueue.iterator();
        else
            desIter = m_trackqueue.descendingIterator();

        while(desIter.hasNext()){
            Map.Entry<Integer,Integer> hitSituation = desIter.next();

            int hit = hitSituation.getKey();
            if(hit == flag) {
                if(flag == flagHit)
                    m_hadHitFlag = true;
                break;
            }

            if(hit == 0)
                continue;

            if(hitCount.containsKey(hit))
                hitCount.put(hit, hitCount.get(hit)+hitSituation.getValue());
            else
                hitCount.put(hit, hitSituation.getValue());
        }

        if(hitCount.isEmpty())
            return null;

        ArrayList<Map.Entry<Integer,Integer>> allhits = new ArrayList<>(hitCount.entrySet());

        return Collections.max(allhits, hitComparator);
    }

    /**
     * 获取抽象轨迹,数数法
     * @return '4N5'
     */
    public String getSimpleTrack(){
        //首先消除3个以下出现的噪音
        for (int i = 1; i < 3; i++) {
            filter(i);
        }

        satisticLeftAndRight();

        StringBuilder codeStr = new StringBuilder("");

        if(null != m_leftHit)
            codeStr.append(String.format("%d", m_leftHit.getKey()));

        codeStr.append(',');

        if(m_hadHitFlag)
            codeStr.append('N');

        codeStr.append(',');

        if(null != m_rightHit)
            codeStr.append(String.format("%d", m_rightHit.getKey()));

        String res = codeStr.toString();

        return res.length() > 2 ? res : "";
    }

    /**
     * 过滤掉指定频率的噪点
     * @param rate
     */
    private void filter(int rate){
        ArrayDeque<Map.Entry<Integer,Integer>> result = new ArrayDeque<>();

        HashMap<Integer, Integer> open = new HashMap<>();

        while (!m_trackqueue.isEmpty()){
            Map.Entry<Integer,Integer> hit = m_trackqueue.pop();

            int hitPt = hit.getKey();
            int hitCount = hit.getValue();

            if(hitCount <= rate){
                if(open.containsKey(hitPt)){
                    hitCount += open.get(hitPt);
                }

                open.put(hitPt, hitCount);

                continue;
            }

            //出现频次大于过滤阈值
            if(open.isEmpty()){
                result.push(hit);
                continue;
            }

            ArrayList<Map.Entry<Integer,Integer>> wait = new ArrayList<>(open.entrySet());

            Map.Entry<Integer,Integer> selected = Collections.max(wait, hitComparator);

            if(open.containsKey(hitPt) &&
                (open.get(hitPt) + hitCount) > selected.getValue()){

                selected = new AbstractMap.SimpleEntry<>(hitPt, open.get(hitPt) + hitCount);
                result.push(selected);
            } else {
                result.push(selected);
                result.push(hit);
            }

            open.clear();
        }

        m_trackqueue = result;
    }

    private void filter_ex(int rate){
        ArrayDeque<Map.Entry<Integer,Integer>> result = new ArrayDeque<>();

        HashMap<Integer, Integer> open = new HashMap<>();

        while (!m_trackqueue.isEmpty()){
            Map.Entry<Integer,Integer> hit = m_trackqueue.pop();

            int hitPt = hit.getKey();
            int hitCount = hit.getValue();

            if(hitCount <= rate){
                if(open.containsKey(hitPt)){
                    hitCount += open.get(hitPt);
                }

                open.put(hitPt, hitCount);

                continue;
            }

            //出现频次大于过滤阈值
            if(open.isEmpty()){
                result.addLast(hit);
                continue;
            }

            ArrayList<Map.Entry<Integer,Integer>> wait = new ArrayList<>(open.entrySet());

            Map.Entry<Integer,Integer> selected = Collections.max(wait, hitComparator);

            if(open.containsKey(hitPt) &&
                    (open.get(hitPt) + hitCount) > selected.getValue()){

                selected = new AbstractMap.SimpleEntry<>(hitPt, open.get(hitPt) + hitCount);
                result.addLast(selected);
            } else {
                result.addLast(selected);
                result.addLast(hit);
            }

            open.clear();
        }

        m_trackqueue = result;
    }

    /**
     * 获取二维行程编码
     * @return "{"4":9,"3":1,"255":10,"5":21...}"
     */
    public String get2DRunningCode(){
        //首先消除3个以下出现的噪音
        for (int i = 1; i < 3; i++) {
            filter(i);
        }

        if(m_trackqueue.isEmpty())
            return "";

        StringBuilder codeBuild = new StringBuilder();

        while(!m_trackqueue.isEmpty()){
            Map.Entry<Integer,Integer> hit = m_trackqueue.pop();

            codeBuild.append(String.format("\"%d\":%d,", hit.getKey(), hit.getValue()));
        }

        int strLen = codeBuild.length() - 1;
        if(codeBuild.lastIndexOf(",") == strLen)
            codeBuild.deleteCharAt(strLen);//remove the last ','

        return String.format("{%s}", codeBuild.toString());
    }

    public String get2DRunningCode(int filterval){
        //首先消除3个以下出现的噪音
        for (int i = 1; i < filterval; i++) {
            filter(i);
        }

        if(m_trackqueue.isEmpty())
            return "";

        StringBuilder codeBuild = new StringBuilder();

        while(!m_trackqueue.isEmpty()){
            Map.Entry<Integer,Integer> hit = m_trackqueue.pop();

            codeBuild.append(String.format("\"%d\":%d,", hit.getKey(), hit.getValue()));
        }

        int strLen = codeBuild.length() - 1;
        if(codeBuild.lastIndexOf(",") == strLen)
            codeBuild.deleteCharAt(strLen);//remove the last ','

        return String.format("{%s}", codeBuild.toString());
    }
    /**
     * 获取简单行程编码
     * @return link数组
     */
    public List<Integer> getRunningCode(int filterval){
        //首先消除3个以下出现的噪音
        for (int i = 1; i < filterval; i++) {
            filter_ex(i);
        }

        if (m_trackqueue.isEmpty())
            return null;

        List<Integer> result = new ArrayList<>();

        while (!m_trackqueue.isEmpty()) {
            Map.Entry<Integer,Integer> hit = m_trackqueue.pop();

            result.add(hit.getKey());
        }

        return result;
    }
}
