package mhw_asb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Application {

	public static void main(String[] args) {
		// Load sqlite JDBC driver
		Connection conn = null;
		
		try {
			Class.forName("org.sqlite.JDBC");
			
			
			conn = DriverManager.getConnection("jdbc:sqlite:database.db");
			
			Statement stmt = conn.createStatement();
			stmt.setQueryTimeout(30);
			
			ResultSet res = stmt.executeQuery("SELECT * FROM Equipment WHERE Name like \"Zorah%\"");
			while(res.next()) {
				System.out.println(res.getInt("ID"));
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {         
	        try {
	              if(conn != null)
	                 conn.close();
	        } catch(SQLException e) {  // Use SQLException class instead.          
	        	System.err.println(e); 
	        }
		}
	}
}
