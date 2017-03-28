import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
public class JDBCUtils {
	//	������Դ	
	private static DataSource dataSource = null;
	
	static{
		dataSource = new ComboPooledDataSource("c3p0");
	}
	
	public static  Connection getConnection() throws SQLException{
		return dataSource.getConnection();
	}
	
	//	�ͷ���Դ
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
				//���ݿ����ӳص�Connection �������close()
				//��������Ľ��йرգ����ǹ黹�����ݿ����ӳ�
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//�ύ����
	public static void commit(Connection connection){
		if(connection != null){
			try {
				connection.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	//ʹ������Ӧ����֤ͬһ�����ݿ����ӽ��в���
	//�ع�����
	public static void rollbackTransaction(Connection connection){
		if(connection != null){
			try {
				connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	//��ʼ����
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
