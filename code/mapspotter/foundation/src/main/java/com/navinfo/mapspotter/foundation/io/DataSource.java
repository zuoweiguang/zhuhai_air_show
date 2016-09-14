package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.util.DataSourceParams;

/**
 * 数据库访问入口类.
 */
public abstract class DataSource implements AutoCloseable {
    protected DataSource() {

    }

    public static DataSource getDataSource(DataSourceParams params) {
        DataSource dataSource = null;

        switch (params.getType()) {
            case Oracle:
                dataSource = new OracleDatabase();
                break;
            case MySql:
                dataSource = new MysqlDatabase();
                break;
            case Sqlite:
                break;
            case MongoDB:
                dataSource = new MongoDB();
                break;
            case Redis:
                dataSource = new Redis();
                break;
            case HBase:
                dataSource = new Hbase();
                break;
            case HDFS:
                dataSource = new Hdfs();
                break;
            case Solr:
                dataSource = new Solr();
                break;
            case Hive:
                dataSource = new Hive();
                break;
            case PostGIS:
                dataSource = new PostGISDatabase();
                break;
        }

        if (dataSource.open(params) != 0) {
            dataSource.close();
            return null;
        }

        return dataSource;
    }

    /**
     * 打开数据源
     * @param params 数据源参数
     * @return <code>0</code>打开成功;
     *         <code>-1</code>打开失败.
     */
    protected abstract int open(DataSourceParams params);

    /**
     * 关闭数据库
     * @return
     */
    public abstract void close();
}
