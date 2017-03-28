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

	// a. insert,update,delete ����
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

	// b. ��ѯһ����¼�����ض�Ӧ�Ķ���
	public <T> T get(Class<T> clazz, String sql, Object... args) {

		List<T> list = getForList(clazz, sql, args);
		if (list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	//c. ����ĳ����¼��ĳһ���ֶε�ֵ �� һ��ͳ�Ƶ�ֵ��һ����������¼��
	public <E> E getForValue(String sql, Object... args) {

		// �õ���������ý����Ӧ��ֻ��һ�У���ֻ��һ��
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// �õ������
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

	// d. ��ѯ������¼�����ض�Ӧ�Ķ���ļ���
	public <T> List<T> getForList(Class<T> clazz, String sql, Object... args) {

		List<T> list = new ArrayList<T>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// 1.�õ������
			connection = JDBCUtils.getConnection();
			preparedStatement = connection.prepareStatement(sql);

			for (int i = 0; i < args.length; i++) {
				preparedStatement.setObject(i + 1, args[i]);
			}

			resultSet = preparedStatement.executeQuery();
			// 2.�����������õ�Map��List������һ��Map�������һ����¼
			List<Map<String, Object>> values = handleResultSetToMapList(resultSet);

			// 3.��Map��ListתΪclazz��Ӧ��List,����Map��Key��Ϊclazz
			// ��Ӧ�Ķ����propertyName,Map��value��Ϊclazz��Ӧ�Ķ����propertyValue
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
		// �ж�list�Ƿ�Ϊ�ռ��ϣ�����Ϊ�գ�����List���õ�һ����Map�����ٰ�һ��Map����תΪһ��Class
		// �������͵�Object����
		if (values.size() > 0) {
			for (Map<String, Object> m : values) {
				// �˴�Ҫʵ����
				bean = clazz.newInstance();
				for (Map.Entry<String, Object> entry : m.entrySet()) {
					String propertyName = entry.getKey();
					Object value = entry.getValue();

					// ûʵ��clazz�����׳�java.lang.IllegalArgumentException: No bean specified
					BeanUtils.setProperty(bean, propertyName, value);
				}
				//��Object������뵽List��
				result.add(bean);
			}
		}
		return result;
	}

	private List<Map<String, Object>> handleResultSetToMapList(
			ResultSet resultSet) throws SQLException {

		// ׼��һ��List<Map<String,Object>>һ��Map��Ӧһ����¼
		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
		List<String> columnLabels = getColumnLabels(resultSet);
		Map<String, Object> map = null;

		// ����ResultSet,ʹ��whileѭ��
		while (resultSet.next()) {
			map = new HashMap<String, Object>();

			for (String columnLabel : columnLabels) {
				Object value = resultSet.getObject(columnLabel);
				map.put(columnLabel, value);
			}
			// �����õ�Map�������׼���õ�List
			values.add(map);
		}
		return values;
	}


	// ��ȡ�������ColumnLabel��Ӧ��List
	private List<String> getColumnLabels(ResultSet rs) throws SQLException {

		List<String> labels = new ArrayList<String>();

		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			labels.add(rsmd.getColumnLabel(i + 1));
		}
		return labels;
	}

}
