package com.navinfo.mapspotter.foundation.algorithm.string;

import java.util.*;

/**
 * Created by SongHuiXing on 2016/1/6.
 */
public class SimpleCountCluster {
    private int flagHit = 255;

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

    private boolean m_hadHitFlag = false;

    private void insertHit(int hit){
        Map.Entry<Integer, Integer> last = m_trackqueue.peekLast();

        if(null != last && last.getKey() == hit){
            last.setValue(last.getValue()+1);
        }else{
            m_trackqueue.addLast(new AbstractMap.SimpleEntry<>(hit, 1));
        }

        if(!m_hadHitFlag && hit == flagHit)
            m_hadHitFlag = true;
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

        ArrayList<Map.Entry<Integer,Integer>> allhits = getTotalHitCount(true);

        if(allhits.size() > 0){
            m_leftHit = allhits.get(0);
        }

        if(!m_hadHitFlag){
            m_rightHit = allhits.size() >= 2 ? allhits.get(1) : null;
        } else{
            ArrayList<Map.Entry<Integer,Integer>> righthits = getTotalHitCount(false);

            if(righthits.size() > 0){
                m_rightHit = righthits.get(0);
            }
        }

        return true;
    }

    private ArrayList<Map.Entry<Integer,Integer>> getTotalHitCount(boolean isLeft){
        Map<Integer,Integer> hitCount = new HashMap<>();

        Iterator<Map.Entry<Integer,Integer>> desIter;
        if(isLeft)
            desIter = m_trackqueue.iterator();
        else
            desIter = m_trackqueue.descendingIterator();

        while(desIter.hasNext()){
            Map.Entry<Integer,Integer> hitSituation = desIter.next();

            int hit = hitSituation.getKey();
            if(hit == flagHit)
                break;

            if(hit == 0)
                continue;

            if(hitCount.containsKey(hit))
                hitCount.put(hit, hitCount.get(hit)+hitSituation.getValue());
            else
                hitCount.put(hit, hitSituation.getValue());
        }

        ArrayList<Map.Entry<Integer,Integer>> allhits = new ArrayList<>();
        allhits.addAll(hitCount.entrySet());

        Collections.sort(allhits,
                new Comparator<Map.Entry<Integer,Integer>>(){
                    public int compare(Map.Entry<Integer,Integer> hit1,
                                       Map.Entry<Integer,Integer> hit2) {
                        return hit1.getValue() - hit2.getValue();
                    }
                });

        return allhits;
    }

    /**
     * 获取抽象轨迹,数数法
     * @return '4N5'
     */
    public String getSimpleTrack(){
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
     * 获取一次简化后的字符串情况
     * @return "4:9,3:1,255:10,5:21..."
     */
    public String situation(){
        StringBuilder codeBuild = new StringBuilder();

        codeBuild.append('{');

        while(!m_trackqueue.isEmpty()){
            Map.Entry<Integer,Integer> hit = m_trackqueue.pop();

            codeBuild.append(String.format("\"%d\":%d,", hit.getKey(), hit.getValue()));
        }

        int strLen = codeBuild.length();
        if(codeBuild.lastIndexOf(",") == (strLen - 1))
            codeBuild.deleteCharAt(strLen - 1);//remove the last ','

        codeBuild.append('}');

        return codeBuild.toString();
    }
}
