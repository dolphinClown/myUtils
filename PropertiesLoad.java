import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoad {
	public static float memRentFee;
	public static float temRentFee;
	public static float overDayFee;

	static {
		// 使用类加载器加载
		InputStream in = null;
		Properties pros = new Properties();
		try {

			in = PropertiesLoad.class.getClassLoader().getResourceAsStream
			("cn/edu/xauat/computer/utils/cost.properties");
			pros.load(in);

			memRentFee = Float.parseFloat(pros.getProperty("memRentFee"));
			temRentFee = Float.parseFloat(pros.getProperty("temRentFee"));
			overDayFee = Float.parseFloat(pros.getProperty("overDayFee"));	
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			in.close();
		}
	}
}
