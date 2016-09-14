package com.navinfo.mapspotter.foundation.io;

import com.navinfo.mapspotter.foundation.io.mongo.MongoDBClient;
import com.navinfo.mapspotter.foundation.io.mysql.MysqlDataBase;
import com.navinfo.mapspotter.foundation.io.oracle.OracleDatabase;

/**
 * 数据库访问入口类.
 */
public class DataBase {

    public enum DataBaseType{
        Oracle,
        MySql,
        Sqlite,
        Mongo,
    }

    protected DataBase(){

    }

    public static DataBase getDatabase(DataBaseType type){

        switch (type) {
            case Oracle:
                return new OracleDatabase();
            case MySql:
                return new MysqlDataBase();
            case Sqlite:
                break;
            case Mongo:
                return new MongoDBClient();
        }

        return null;
    }

    /**
     * 打开数据库
     * @param connProperty 数据库属性
     * @return <code>0</code>打开成功;
     *         <code>-1</code>打开失败.
     */
    public int open(ConnectParams connProperty){
        return -1;
    }

    /**
     * 获取数据库连接
     * @param user      用户名
     * @param password  密码
     * @return
     */
    public DBConnection getConnection(String user, String password){
        return null;
    }

    /**
     * 关闭数据库
     * @return
     */
    public int close(){
        return 0;
    }
}
