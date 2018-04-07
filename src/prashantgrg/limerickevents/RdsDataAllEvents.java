package prashantgrg.limerickevents;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.AsyncTask;


public class RdsDataAllEvents extends AsyncTask<Void, Void, ResultSet>{

	private Rdsinter<ResultSet> listener;
	private static Connection con;
	private static Statement cmd;
	private int eventid, prefer; // previous if value is 0 and next is value is 1 for prefer , first screen 2
	private static ResultSet data;
	private static Calendar cal = Calendar.getInstance();
	private static Date currentDate;
	private DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	private String dateToday;
	private static String conString = Constants.CONNECTION ;
	
	public RdsDataAllEvents(Rdsinter<ResultSet> r,int id, int p){
		this.listener = r;
		prefer=p;
		eventid=id;
		currentDate = new Date();
		cal.setTime(currentDate);
		dateToday = fmt.format(currentDate);
	}

	@Override
	protected ResultSet doInBackground(Void... params) {
		try{
			con = DriverManager.getConnection(conString);
			cmd = con.createStatement();
			
			if(prefer==1){ // if user clicked forward button
					data = cmd.executeQuery("Select * from event where eventID > '" + eventid + "' ORDER BY eventID LIMIT 15;");	
			}else if(prefer==2){ // if it is running for first time
				data = cmd.executeQuery("Select * from event where DATE>= '" + dateToday + "' ORDER BY eventID LIMIT 15;");
			}
			else{ // if user clicked back button
				if(eventid-1<=0){
					data=cmd.executeQuery("Select * from event where eventID < '"+ 0 + "' ORDER BY eventID LIMIT 15;"); // returning empty results
				}
				else{
					data = cmd.executeQuery("Select * from event where eventID >= '" + (eventid-15) + "' AND eventID<= '" + eventid + "' ORDER BY eventID LIMIT 15;");
				}
			}	
			data.last();
			return data;
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e);
			return null;
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
	protected void onPostExecute(ResultSet result){
			listener.onTaskComplete(result);
		}		
}