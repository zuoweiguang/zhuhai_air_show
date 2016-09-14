package com.navinfo.mapspotter.foundation.io.util;

/**
 * Created by gaojian on 2016/1/14.
 */
public class SolrQueryClause {
    private static final String NEGATIVE = "-";

    protected String sign = ""; //符号：加号或者减号，默认空
    protected String field = ""; //字段名，为空表示预设字段
    protected String value = ""; //查询关键字，可以有通配符；如果toValue不为空，则表示范围的开始值
    protected String toValue = ""; //范围的结束值，可以为空

    public SolrQueryClause(String keyword) {
        value = keyword;
    }

    public SolrQueryClause(String field, String keyword) {
        this(field, keyword, false);
    }

    public SolrQueryClause(String field, String keyword, boolean negative) {
        this.field = field;
        this.value = keyword;
        if (negative) {
            sign = NEGATIVE;
        }
    }

    public SolrQueryClause(String field, String fromValue, String toValue) {
        this(field, fromValue, toValue, false);
    }

    public SolrQueryClause(String field, String fromValue, String toValue, boolean negative) {
        this.field = field;
        this.value = fromValue;
        this.toValue = toValue;
        if (negative) {
            sign = NEGATIVE;
        }
    }

    @Override
    public String toString() {
        String str = sign;

        if (field != null && !field.isEmpty()) {
            str = str + field + ":";
        }

        //关键字
        if (value == null || value.isEmpty()) {
            value = "*";
        }
        if (toValue == null || toValue.isEmpty()) {
            str = str + value;
        } else {
            str = str + "[" + value + " TO " + toValue + "]";
        }

        return str;
    }
}
