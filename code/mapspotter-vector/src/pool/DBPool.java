package pool;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.ResultSet;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBPool {
	private static DBPool dbPool;
	private ComboPooledDataSource dataSource;

	static {
		dbPool = new DBPool();
	}

	public DBPool() {
		try {
			dataSource = new ComboPooledDataSource();
			dataSource.setUser("scott");
			dataSource.setPassword("tiger");
			dataSource
					.setJdbcUrl("jdbc:oracle:thin:@192.168.4.131:1521:orcl");
			dataSource.setDriverClass("oracle.jdbc.driver.OracleDriver");
			dataSource.setInitialPoolSize(20);
			dataSource.setMinPoolSize(1);
			dataSource.setMaxPoolSize(100);
			dataSource.setMaxStatements(50);
			dataSource.setMaxIdleTime(60);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
	}

	public final static DBPool getInstance() {
		return dbPool;
	}

	public final Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("无法从数据源获取连接 ", e);
		}
	}

	public static void main(String[] args) throws SQLException {
		Connection con = null;
		try {
			con = DBPool.getInstance().getConnection();
			
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("select * from emp");
			
			while(rs.next()){
				System.out.println(rs.getString("ename"));
			}
		} catch (Exception e) {
		} finally {
			if (con != null)
				con.close();
		}
		
		int a = 15 * 15 + (14 * 14);
		
		System.out.println(Math.sqrt(a));
		
	}
}