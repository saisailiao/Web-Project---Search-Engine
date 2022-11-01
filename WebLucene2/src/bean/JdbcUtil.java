package bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtil {
	static Connection con;

	public static Connection getConnection() {
//连接数据库，设置连接数据库配置
		try {
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost/lucenedb2";
			String username = "root";
			String password = "newpassword";
			con = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return con;
	}

	//ִ插入语句
	public int updateExecute(String sql) {
		int result = 0;
		try {
			Connection con = getConnection();
			Statement sta = con.createStatement();
			result = sta.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	//查询语句
	public  ResultSet queryExectue(String sql) {
		ResultSet rs = null;
		try {
			Connection con = getConnection();
			Statement sta = con.createStatement();
			rs = sta.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	//查询语句
	public  String executeScalar(String sql) {
		ResultSet rs = queryExectue(sql);
		String s = "";
		try {
			while (rs.next()) {
				s = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return s;
	}
}
