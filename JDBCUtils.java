import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
public class JDBCUtils {
	//	共享资源	
	private static DataSource dataSource = null;
	
	static{
		dataSource = new ComboPooledDataSource("c3p0");
	}
	
	public static  Connection getConnection() throws SQLException{
		return dataSource.getConnection();
	}
	
	//	释放资源
	public static void releaseConnection(ResultSet resultSet,Statement statement,Connection connection){
		try {
			if(resultSet!=null){
				resultSet.close();
			}
			if(statement!=null){
				statement.close();
			}
			if(connection!=null){
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void releaseConnection(Statement statement,Connection connection){
		try {
			if(statement!=null){
				statement.close();
			}
			if(connection!=null){
				//数据库连接池的Connection 对象进行close()
				//并不是真的进行关闭，而是归还到数据库连接池
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//提交事务
	public static void commit(Connection connection){
		if(connection != null){
			try {
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	//使用事务，应当保证同一个数据库连接进行操作
	//回滚事务
	public static void rollbackTransaction(Connection connection){
		if(connection != null){
			try {
				connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	//开始事务
	public static void beginTransaction(Connection connection){
		if(connection != null){
			try {
				connection.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
