package prashantgrg.limerickevents;


import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.os.AsyncTask;


public class RdsDataThisWeek extends AsyncTask<Void, Void, ResultSet>{

	private Rdsinter<ResultSet> listener;
	private static Calendar cal = Calendar.getInstance();
	private int dayOfWeek=0,endOfWeek=7;
	private static Date currentDate;
	private DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
	private String dateToday, dateEnd;
	private static Connection con;
	private static Statement cmd;
	private int eventid, prefer; // previous if value is 0 and next is value is 1 for prefer , first screen 2
	private static ResultSet data;
	private static String conString = Constants.CONNECTION ;

	public RdsDataThisWeek(Rdsinter<ResultSet> r,int id, int p){
		this.listener = r;
		eventid=id;
		prefer=p;
		currentDate = new Date();
		cal.setTime(currentDate);
		dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		endOfWeek = endOfWeek- dayOfWeek;
		dateToday = fmt.format(currentDate);
		cal.add(Calendar.DATE, endOfWeek);
		dateEnd = fmt.format(cal.getTime());
	}

	@Override
	protected ResultSet doInBackground(Void... params) {
		try{
			con = DriverManager.getConnection(conString);
			cmd = con.createStatement();

			if(prefer==1){ // if user clicked forward button
				data = cmd.executeQuery("Select * from event where eventID > '" + eventid + "' ORDER BY eventID LIMIT 15;");	
			}else if(prefer==2){ // if it is running for first time
				
				data = cmd.executeQuery("Select * from event where DATE>= '" + dateToday + "' AND DATE <= '" + dateEnd + "' ORDER BY eventID LIMIT 15;");
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
		} catch (SQLException e) {
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