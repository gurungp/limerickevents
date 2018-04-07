package prashantgrg.limerickevents;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class login_activity extends Activity{


	private loginManager lm;
	private Button signup,login;
	private static Connection con;
	private static Statement cmd;
	private static ResultSet data;
	private static String conString = Constants.CONNECTION ;
	private Boolean internet;
	private Boolean validUser;
	private ProgressDialog progress;
	private TextView usertv,passtv,lgtext;
	private ImageView back;
	private EditText password,user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);
		internet=false;
		validUser=false;
		getActionBar().setDisplayHomeAsUpEnabled(true);
		lm = (loginManager) getApplicationContext();
		user = (EditText) findViewById(R.id.editText2);
		password = (EditText) findViewById(R.id.editText1);
		login = (Button) findViewById(R.id.button_login);
		back = (ImageView) findViewById(R.id.back);
		signup = (Button) findViewById(R.id.signup);
		usertv = (TextView) findViewById (R.id.usertv);
		passtv= (TextView) findViewById (R.id.passtv);
		lgtext = (TextView) findViewById (R.id.logintxt);

		// click listener for back button, it will finish this activity	
		back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				finish();
			}
		});

	}

	protected void onResume(){
		super.onResume();

		if(isInternetAvailable()){

			signup.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent newInt=new Intent(login_activity.this,Signup.class);
					startActivity(newInt);
				}
			});


			login.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					String username = user.getText().toString();
					String pass = password.getText().toString();

					if((username.isEmpty() || username.contains(" ")) || (pass.isEmpty()))
					{
						Toast.makeText(login_activity.this, "Please enter valid Username or Password", Toast.LENGTH_LONG).show();

					}
					else{
						logger l1 = new logger(username, pass);
						l1.execute();
					}
				}
			});
		}
		else{
			login.setVisibility(View.GONE);
			user.setVisibility(View.GONE);
			password.setVisibility(View.GONE);
			usertv.setVisibility(View.GONE);
			signup.setVisibility(View.GONE);
			passtv.setVisibility(View.GONE);
			lgtext.setText("No Internet Connection");
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
	 
	public Boolean isInternetAvailable(){
		Runnable run=new Runnable(){
			public void run(){
				try {
					InetAddress ipAddr = InetAddress.getByName("google.com"); 

					if (ipAddr.equals(null)) {
						internet = false;
					} else {
						internet = true;
					}

				} catch (Exception e) {
					internet = false;
				}
			}
		};
		Thread t1=new Thread(run);
		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return internet;
	}

	public class logger extends AsyncTask<Void, Void, Boolean>{

		private String username;
		private String password;

		public logger(String u,String p){
			username=u;
			password=p;
		}

		protected void onPreExecute(){
			super.onPreExecute();

			progress = new ProgressDialog(login_activity.this);
			progress.setTitle("Logging in...");
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
				data = cmd.executeQuery("Select * from user where username=" + "'" +username + "'" + "AND password='"+password+"';");
				data.last();
				if(data.getRow()>0){
					temp=true;
				}else{
					temp=false;
				}	
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return temp;
		}

		protected void onPostExecute(Boolean b){
			validUser=b;
			if(progress.isShowing()){
				progress.dismiss();
			}else{}

			if(validUser)
			{
				lm.login();
				finish();
			}
			else{
				Toast.makeText(login_activity.this, "Login Unsuccessful", Toast.LENGTH_LONG).show();
			}
		}
	}

}
