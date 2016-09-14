package com.navinfo.pool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.hbase.async.HBaseClient;

public class HBasePool {
	
	private static Properties props = new Properties();
	
	private static HBaseClient client;
	
	static{
		try {
			
			props.load(new FileInputStream("../webapps/demo1/WEB-INF/classes/config.properties"));
			
			client = new HBaseClient(props.getProperty("hbase.zookeeper.quorum"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			
			System.exit(-1);
		}
	}
	
	public static HBaseClient getClient(){
		return client;
	}

}
