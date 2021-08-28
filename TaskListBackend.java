package com.localhost.queryoperation;
import com.localhost.dbconnect.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;  
public class TaskListBackend {   
	Connection conn = null;
	public Connection connect() {
   	 final String url = "jdbc:postgresql://localhost:5432/tasklistapplication";
        final String user = "postgres";
        final String password = "kuna271999";
      
       
       try {
	Class.forName("org.postgresql.Driver");
           conn = DriverManager.getConnection(url, user, password);
           System.out.println("Connected to the PostgreSQL server successfully.");
           
           }
       catch (Exception e) {
           System.out.println(e.getMessage());
       }

       return conn;
   }
	public void adduser(String createname,String createusername,String createpassword){
		connect();
		if(conn==null) {
			System.out.println("DB connection not available");
	        }
		else {
		Statement stmt=null;
		ResultSet rs=null;
		ResultSet rw=null;
		try {
			stmt = conn.createStatement();
			String sql = "INSERT INTO newUser (createName,createUserName,createPassword) "
	                + "VALUES ('"+createname+"','"+createusername+"',"+"crypt('"+createpassword+"',gen_salt('bf')"+"));";
	        stmt.executeUpdate(sql);
	        rs=stmt.executeQuery("select * from newuser");
	        while(rs.next()) {
	        	int userid=rs.getInt("userId");
			String name=rs.getString("createName");
	        	String username=rs.getString("createUserName");
	        	String userpassword=rs.getString("createPassword");
	        	System.out.println( "ID = " + userid );
	            System.out.println( "NAME = " + name);
			System.out.println( "USERNAME = " + username);
	            System.out.println( "PASSWORD = " + userpassword );
		   }
	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
			LocalDateTime now = LocalDateTime.now();  
	        rw=stmt.executeQuery("select userId from newUser where createusername='"+createusername+"';");
            if(rw.next()) {                
			int uid=rw.getInt("userId");
			String sql1 = "INSERT INTO taskDetails (taskName,taskCreatedby,taskStatus,taskDescription,taskCreatedTime,userId,estimatedtime) "
	                + "VALUES ('Attendance','Manager','nottaken','Daily attendance','"+dtf.format(now)+"','"+uid+"','1');";
			stmt.executeUpdate(sql1);
			String sql2 = "INSERT INTO taskDetails (taskName,taskCreatedby,taskStatus,taskDescription,taskCreatedTime,userId,estimatedtime) "
	                + "VALUES ('Attendance','Manager','nottaken','Daily attendance','"+dtf.format(now)+"','"+uid+"','1');";
			stmt.executeUpdate(sql2);
            }
		}catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
			if(rs!=null) {
				rs.close();
			}
			if(rw!=null) {
				rw.close();
			}
			if(stmt!=null) {
				stmt.close();
			}
			if(conn!=null) {
				conn.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		}}

	}
	public String logincheck(String loginUsername,String loginPassword) {
		connect(); 
		String result = " ";
		if(conn==null) {
			System.out.println("DB connection not available");
	     }
		else {
			Statement stmt=null;
			ResultSet rw=null;
			try {
				stmt = conn.createStatement();
System.out.println(loginUsername);
				 System.out.println(loginPassword);
				  rw=stmt.executeQuery("select createUserName,(createPassword=crypt('"+loginPassword+"',createPassword)) as password from newuser where createUsername='"+loginUsername+"'");
				 if(!rw.next()) {
					 result="Userinvalid";
				 }
				 else {
					 Boolean passmatch=rw.getBoolean("password");
					 if(passmatch) {
						 result="Successfullogin";
					 }
					 else {
						 result="Passinvalid";
					 }
					
				 }
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if(rw!=null) {
						rw.close();
					}
					if(stmt!=null) {
						stmt.close();
					}
					if(conn!=null) {
						conn.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		
	}
		return result;
	}
	public void autoassigntask(JSONArray jarr,String taskname,String taskdescription,String userN,int estitime) {
		int i=1026;
		int min=10000;
		int ui=1026;
		String cname="";
		Boolean flag=true;
		connect();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
		LocalDateTime now = LocalDateTime.now(); 
		if(conn==null) {
			System.out.println("DB connection not available");
	     }
		else {
			ResultSet rs=null;
			ResultSet rw=null;
			Statement stmt=null;
			
    		try {
    		while(flag) {
    			stmt = conn.createStatement();
    		rs=stmt.executeQuery("select userid,sum(estimatedtime) as s from taskDetails where userid='"+i+"' and taskstatus='nottaken' group by userid;");
    		
    		if(rs.next())
    		{
    			int et=rs.getInt("s");
    			System.out.println(i+" "+et);
    			if(et<min) {
    				min=et;
    				ui=i;
    			}
    		}
    		else {
    			flag=false;
    			System.out.println("no");
    		}
    		i=i+1;
    		}
    		System.out.println("min "+min+" "+"id "+ui);
    		String sql = "INSERT INTO taskDetails (taskName,taskCreatedby,taskStatus,taskDescription,taskCreatedTime,userId,estimatedtime) "
	                + "VALUES ('"+taskname+"','"+userN+"','nottaken','"+taskdescription+"','"+dtf.format(now)+"','"+ui+"','"+estitime+"');";
			stmt.executeUpdate(sql);
			rw=stmt.executeQuery("select createusername from newUser where userid='"+ui+"';");
			if(rw.next()) {
				cname=rw.getString("createusername");
			}
    		JSONObject jobj=new JSONObject();
	         jobj.put("result", "taskadded");
			jobj.put("assigntoo",cname);
	         jarr.put(jobj);
    		}catch(Exception e) {
    			e.printStackTrace();
    		}finally {
				try {				
					if(rs!=null) {
						rs.close();
					}
					if(stmt!=null) {
						stmt.close();
					}
					if(conn!=null) {
						conn.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	public void addtask(JSONArray jarr,String taskname,String taskdescription,String taskassign,String userN,int estitime) {
		connect();
		if(conn==null) {
			System.out.println("DB connection not available");
	     }
		else {
			Statement stmt=null;
			ResultSet rs=null;
			ResultSet rw=null;
			ResultSet rq=null;
			try {
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
				LocalDateTime now = LocalDateTime.now();  
				stmt = conn.createStatement();
				rs=stmt.executeQuery("select userId from newUser where createusername='"+taskassign+"';");
                if(rs.next()) {                
				int uid=rs.getInt("userId");
				String sql = "INSERT INTO taskDetails (taskName,taskCreatedby,taskStatus,taskDescription,taskCreatedTime,userId,estimatedtime) "
		                + "VALUES ('"+taskname+"','"+userN+"','nottaken','"+taskdescription+"','"+dtf.format(now)+"','"+uid+"','"+estitime+"');";
				stmt.executeUpdate(sql);
				         JSONObject jobj=new JSONObject();
				         jobj.put("result", "taskadded");
				         jarr.put(jobj);
                }
        		               
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally {
				try {
					if(rs!=null) {
						rs.close();
					}
					if(rw!=null) {
						rw.close();
					}
					if(rq!=null) {
						rq.close();
					}
					if(stmt!=null) {
						stmt.close();
					}
					if(conn!=null) {
						conn.close();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	public void updatetask(int idtask,String tstatus) {
		connect();
		if(conn==null) {
			System.out.println("DB connection not available");
	     }
		else {
			Statement stmt=null;
			try {
				stmt = conn.createStatement();
				String sql = "update taskDetails set taskstatus='"+tstatus+"' where taskid='"+idtask+"';";  
		        stmt.executeUpdate(sql);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
				if(stmt!=null) {
					stmt.close();
				}
				if(conn!=null) {
					conn.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			}
		}
	}
}