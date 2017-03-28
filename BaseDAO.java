import cn.edu.xauat.computer.dao.Dao;
import cn.edu.xauat.computer.utils.JDBCUtils;
import cn.edu.xauat.computer.utils.ReflectionUtils;
import cn.edu.xauat.computer.web.ConnectionContext;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

/**
 * DBUtils解决方案
 * @param <T>
 */
public class BaseDAO<T> implements Dao<T> {

    private QueryRunner queryRunner = new QueryRunner();

    private Class<T> clazz;

    public BaseDAO() {
        //getClass()返回值为继承 BaseDAO 的 DAOImpl 的运行时类对象。 向上转型 BaseDAO baseDao = new BookDAOImpl();
        //clazz得到 DAOImpl 的父类 BaseDAO<T> 中的泛型类型参数类型。 如:BaseDAO<Book> 那么该类型为Book.class
        clazz = ReflectionUtils.getSuperGenericType(getClass());
    }

    @Override
    public long insert(String sql, Object... args) {

        long id = 0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectionContext.getInstance().get();
            //a flag indicating whether auto-generated keys 【should be returned】;
            // one of Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    preparedStatement.setObject(i + 1, args[i]);
                }
            }

            preparedStatement.executeUpdate();

            //获取生成的主键值
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                id = resultSet.getLong(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.release(resultSet, preparedStatement);
        }

        return id;
    }

    @Override
    public void update(String sql, Object... args) {
        Connection connection = null;

        try {
            connection = ConnectionContext.getInstance().get();
            queryRunner.update(connection, sql, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public T query(String sql, Object... args) {

        Connection connection = null;

        try {
            connection = ConnectionContext.getInstance().get();
            return queryRunner.query(connection, sql, new BeanHandler<T>(clazz), args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<T> queryForList(String sql, Object... args) {
        Connection connection = null;

        try {
            connection = ConnectionContext.getInstance().get();
            return queryRunner.query(connection, sql, new BeanListHandler<T>(clazz), args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <V> V getSingleVal(String sql, Object... args) {
        Connection connection = null;

        try {
            connection = ConnectionContext.getInstance().get();
            return (V) queryRunner.query(connection, sql, new ScalarHandler(), args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void batch(String sql, Object[]... params) {
        Connection connection = null;

        try {
            connection = ConnectionContext.getInstance().get();
            queryRunner.batch(connection, sql, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
