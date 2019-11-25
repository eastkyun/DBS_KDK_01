import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;

public class MyConnection {
	public static StringBuilder sb = new StringBuilder();
	
	public static Connection makeConnection() {
		Connection con = null;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String url = "jdbc:sqlserver://DESKTOP-OTTM6T5\\SQLEXPRESS:1433;databaseName=DBS_KDK_01";
			con = DriverManager.getConnection(url,"sa","1111");
			
			sb.append("Database connection Established Successfully"+ "\n");
			sb.append("Connected!");
		}catch(Exception e) {
			System.out.println("There is connection problems - "+e.getStackTrace()+ "\n");
			sb.append("There is connection problems - " + e.getMessage()+ "\n");
		}
		return con;
		
	}

}
