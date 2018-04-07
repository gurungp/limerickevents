
package prashantgrg.limerickevents;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends Activity {

	private Button allEventsBtn;
	private Button loginBtn;
	private Button thisMonth;
	private Button thisWeek;
	private boolean loginstate;
	private loginManager lm;
	private ProgressDialog progress;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lm = (loginManager) getApplicationContext();
		loginstate = lm.state();
		
		allEventsBtn = (Button) findViewById(R.id.allEvents);
		thisWeek = (Button) findViewById(R.id.thisWeek);
		thisMonth = (Button) findViewById(R.id.thisMonth);
		
		allEventsBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v){
				Intent intent=new Intent(MainActivity.this, all_events.class);
				startActivity(intent);
			}
		});
		
		thisMonth.setOnClickListener(new OnClickListener(){

			public void onClick(View v){
				Intent intent=new Intent(MainActivity.this, ThisMonth.class);
				startActivity(intent);
			}
		});
		
		thisWeek.setOnClickListener(new OnClickListener(){

			public void onClick(View v){
				Intent intent=new Intent(MainActivity.this, ThisWeek.class);
				startActivity(intent);
			}
		});
		
		
		
	}
			
	@Override
	public void onResume() {
		super.onResume();
		loginBtn = (Button) findViewById(R.id.login_img);
		loginstate = lm.state();
		if(loginstate==false&&lm.count()>0){
			lm.decCount();
		}else{}
		if(loginstate==false)
		{
			loginBtn.setText("Login");
			loginBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Intent intent = new Intent(MainActivity.this,login_activity.class);
					if(lm.count()==0)
					{
						lm.incCount();
						startActivity(intent);
					}
					else{
						lm.decCount();
						logout();
						loginBtn.setText("Login");
					}
					
				}
			});
		}
		else{
			loginBtn.setText("Logout");
			loginBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Intent intent = new Intent(MainActivity.this,login_activity.class);
					if(lm.count()==0)
					{
						lm.incCount();
						startActivity(intent);
					}
					else{
						lm.decCount();
						logout();
						loginBtn.setText("Login");
					}
					
				}
			});
			
		}
	}
	
	public void logout(){
    	progress = new ProgressDialog(MainActivity.this);
		progress.setTitle("Logging out...");
		progress.setMessage("Please wait...");
		progress.setCancelable(true);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.show();
		
		Handler handler = new Handler(); // waiting for 2 seconds
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	 			progress.dismiss();
	 			lm.logout();
	         } 
	    }, 2000);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
