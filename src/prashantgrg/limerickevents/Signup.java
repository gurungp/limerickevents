package prashantgrg.limerickevents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Signup extends Activity {
	
	private static EditText user;
	private static EditText password;
	private static EditText retype;
	private static ResultSet data;
	private Button sign;
	private static Connection con;
	private static Statement cmd;
	private static String conString = Constants.CONNECTION ;
	private ProgressDialog progress;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_screen);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		user = (EditText) findViewById(R.id.editText);
		password = (EditText) findViewById(R.id.editText1);
		retype= (EditText) findViewById(R.id.editText2);
        sign = (Button) findViewById(R.id.button_signup);
	
        
        sign.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = user.getText().toString();
				String pass = password.getText().toString();
				String repass = retype.getText().toString();
				if((username.isEmpty() || username.contains(" ")) || (pass.isEmpty()) || pass.contains(" ") || repass.contains(" "))
				{
					Toast.makeText(Signup.this, "Please enter valid Username or Password", Toast.LENGTH_LONG).show();
				}
				else if(pass.equals(repass)==false){
					Toast.makeText(Signup.this, "Retyped password doesn't match", Toast.LENGTH_LONG).show();
				}
				else{
					signup temp=new signup(username, pass);
					temp.execute();
				}
			}
		});
	}
	
	
	public class signup extends AsyncTask<Void, Void, Boolean>{

		private String username;
		private String password;

		public signup(String u,String p){
			username=u;
			password=p;
		}

		protected void onPreExecute(){
			super.onPreExecute();

			progress = new ProgressDialog(Signup.this);
			progress.setTitle("Signing up...");
			progress.setMessage("Please wait...");
			progress.setCancelable(true);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.show();

			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
		protected Boolean doInBackground(Void... params) {
			boolean temp=false;
			try {
				con = DriverManager.getConnection(conString);
				cmd = con.createStatement();
				data = cmd.executeQuery("Select * from user where username=" + "'" +username + "';");
				data.last();
				if(data.getRow()>0){
					temp=true; // username already exist
				}else{
					temp=false; // if no such user exist, then proceed signing up
					cmd = con.createStatement();
					cmd.executeUpdate("INSERT INTO user (username,password) VALUES " + "('" +username + "'" + "," + "'" + password+"');");
				}	
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return temp;
		}

		protected void onPostExecute(Boolean b){
			boolean userExist=b;
			if(progress.isShowing()){
				progress.dismiss();
			}else{}

			if(userExist)
			{
				Toast.makeText(Signup.this, "Username Already Exist", Toast.LENGTH_LONG).show();
			}
			else{
				Toast.makeText(Signup.this, "Signup Successfull", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}
	
	 public boolean onOptionsItemSelected(MenuItem item) {
	   		
	 		switch(item.getItemId()){
	 		case android.R.id.home:
	 			finish();
	 			return true;
	 		}
	 		return super.onOptionsItemSelected(item);
	 }
}
