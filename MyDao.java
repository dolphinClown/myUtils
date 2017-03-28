import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import cn.edu.xauat.computer.utils.JDBCUtils;

public class Dao<T> {

	// a. insert,update,delete 操作
	public void update(String sql, Object... args) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = JDBCUtils.getConnection();
			preparedStatement = (PreparedStatement) connection
					.prepareStatement(sql);

			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i + 1, args[i]);
			}
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.releaseConnection(preparedStatement, connection);
		}
	}

	// b. 查询一条记录，返回对应的对象
	public <T> T get(Class<T> clazz, String sql, Object... args) {

		List<T> list = getForList(clazz, sql, args);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	//c. 返回某条记录的某一个字段的值 或 一个统计的值（一共对少条记录）
	public <E> E getForValue(String sql, Object... args) {

		// 得到结果集：该结果集应该只有一行，且只有一列
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// 得到结果集
			connection = JDBCUtils.getConnection();
			preparedStatement = connection.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i + 1, args[i]);
			}

			resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return (E) resultSet.getObject(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.releaseConnection(resultSet, preparedStatement,
					connection);
		}
		return null;
	}

	// d. 查询多条记录，返回对应的对象的集合
	public <T> List<T> getForList(Class<T> clazz, String sql, Object... args) {

		List<T> list = new ArrayList<T>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// 1.得到结果集
			connection = JDBCUtils.getConnection();
			preparedStatement = connection.prepareStatement(sql);

			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i + 1, args[i]);
			}

			resultSet = preparedStatement.executeQuery();
			// 2.处理结果集，得到Map的List，其中一个Map对象就是一条记录
			List<Map<String, Object>> values = handleResultSetToMapList(resultSet);

			// 3.把Map的List转为clazz对应的List,其中Map的Key即为clazz
			// 对应的对象的propertyName,Map的value即为clazz对应的对象的propertyValue
			list = transferMapListToBeanList(clazz, values);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JDBCUtils.releaseConnection(resultSet, preparedStatement,
					connection);
		}

		return list;
	}

	private <T> List<T> transferMapListToBeanList(Class<T> clazz,
			List<Map<String, Object>> values) throws IllegalAccessException,
			InvocationTargetException, InstantiationException {

		List<T> result = new ArrayList<T>();
		T bean = null;
		// 判断list是否为空集合，若不为空，遍历List，得到一个个Map对象，再把一个Map对象转为一个Class
		// 参数类型的Object对象
		if (values.size() > 0) {
			for (Map<String, Object> m : values) {
				// 此处要实例化
				bean = clazz.newInstance();
				for (Map.Entry<String, Object> entry : m.entrySet()) {
					String propertyName = entry.getKey();
					Object value = entry.getValue();

					// 没实例clazz化会抛出java.lang.IllegalArgumentException: No bean specified
					BeanUtils.setProperty(bean, propertyName, value);
				}
				//把Object对象放入到List中
				result.add(bean);
			}
		}
		return result;
	}

	private List<Map<String, Object>> handleResultSetToMapList(
			ResultSet resultSet) throws SQLException {

		// 准备一个List<Map<String,Object>>一个Map对应一条记录
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
		List<String> columnLabels = getColumnLabels(resultSet);
		Map<String, Object> map = null;

		// 处理ResultSet,使用while循环
		while (resultSet.next()) {
			map = new HashMap<String, Object>();

			for (String columnLabel : columnLabels) {
				Object value = resultSet.getObject(columnLabel);
				map.put(columnLabel, value);
			}
			// 把填充好的Map对象放入准备好的List
			values.add(map);
		}
		return values;
	}


	// 获取结果集的ColumnLabel对应的List
	private List<String> getColumnLabels(ResultSet rs) throws SQLException {

		List<String> labels = new ArrayList<String>();

		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			labels.add(rsmd.getColumnLabel(i + 1));
		}
		return labels;
	}

}
