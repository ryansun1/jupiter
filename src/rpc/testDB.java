package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import db.mysql.MySQLDBUtil;


/**
 * Servlet implementation class testDB
 */
@WebServlet("/testDB")
public class testDB extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public testDB() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String printString = "";
		PrintWriter writer = response.getWriter();
		
		
		String URL = "jdbc:mysql://"
				+ "127.0.0.1" + ":" + "3306" + "/" + "laiproject"
				+ "?user=" + "root" + "&password=" + "root"
				+ "&autoReconnect=true&serverTimezone=UTC";
		Connection conn = null;
		
		try {
			System.out.println("Connecting to " + URL);
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(URL);
			
		} catch (Exception e){
			e.printStackTrace();
			writer.print(e.getMessage());
		}
		
		
		
		if (conn == null) {
	  		 System.err.println("DB connection failed");
	  		 return;
	  	       }
	  	
	  	      try {
	  		 String sql = "SELECT * FROM history";
	  		 System.out.println(sql);
	  		 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery();
			 
			 while (rs.next()) {
					printString += rs.getString("item_id");
				}
			 writer.print(printString);
			 
	  	     } catch (Exception e) {
	  	    	 e.printStackTrace();
	  	     }
	
	}

}
