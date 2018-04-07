package prashantgrg.limerickevents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.os.AsyncTask;


public class RdsDataTask extends AsyncTask<Integer, Void, String>{

	private Rdsinter<String> listener;
	private static Connection con;
	private static Statement cmd;
	private static ResultSet data;
	private static String conString = Constants.CONNECTION ;
	
	public RdsDataTask(Rdsinter<String> r){
		this.listener = r;
	}

	protected String doInBackground(Integer... eventID) {
		Integer eventid=eventID[0];
		try{
			con = DriverManager.getConnection(conString);
			cmd = con.createStatement();
			data = cmd.executeQuery("Select eventDesc from event where eventID="+"'"+eventid+"'");
			data.next();
			String result1 = data.getString("eventDesc");
			return result1;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e);
			return "Error in network connection";
		} 
	}		
	protected void onPreExecute(){
		super.onPreExecute();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} 
	}
	protected void onPostExecute(String result){
		String textdata=result;
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		listener.onTaskComplete(textdata);
		}

}