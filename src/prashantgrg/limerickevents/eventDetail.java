package prashantgrg.limerickevents;



import java.net.InetAddress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



public class eventDetail extends Activity {

	private int eventID;
	private String mapCoor,eventName;
	private Button map;
	private Button home;
	private Button gallery,upload,loginBtn;
	public TextView tv,title;
	private byte[] bitmap;
	private Bitmap bmp;
	private loginManager lm;
	private boolean loginstate;
	private ProgressBar load;
	private Boolean internet;
	private ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventdetail);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		lm = (loginManager) getApplicationContext();
		loginstate = lm.state();
		Intent intent=getIntent();
		internet = false;
		load = (ProgressBar) findViewById(R.id.loading);
		eventID = intent.getIntExtra("EventID", 0);
		mapCoor = intent.getStringExtra("mapCoor");
		eventName = intent.getStringExtra("eventName");
		bitmap = intent.getByteArrayExtra("photo");
		home = (Button) findViewById(R.id.home_img);
		map = (Button) findViewById(R.id.map_icon);
		gallery = (Button) findViewById(R.id.gallery_icon);
		upload = (Button) findViewById(R.id.upload_icon);
		bmp = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
		ImageView back = (ImageView) findViewById(R.id.back);
		ImageView iv = (ImageView) findViewById(R.id.icon_detail);
		tv = (TextView) findViewById(R.id.text_detail);
		title = (TextView) findViewById(R.id.text_title);
		LinearLayout.LayoutParams logoP= new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		logoP.width = 250;
		logoP.height = 250;
		logoP.gravity = Gravity.CENTER_HORIZONTAL;
		iv.setLayoutParams(logoP);
		iv.setImageBitmap(bmp);
		title.setText(eventName);
		
		// click listener for back button, it will finish this activity	
		back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				finish();
			}
		});
		
		uploadButton(); // setting listener to uploadButton. upload button has separate method
		galleryButton();
		
		home.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent newInt=new Intent(eventDetail.this,MainActivity.class);
				newInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(newInt);
			}
		});
		
		map.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				
				try {
					ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
					Intent newInt=new Intent(Intent.ACTION_VIEW,Uri.parse(mapCoor));
					newInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(newInt);
				} catch (NameNotFoundException e) {
					Toast.makeText(eventDetail.this, "Please install Google Maps", Toast.LENGTH_SHORT).show();;
					e.printStackTrace();
					
				}
		        
			}
		});
		
	}


	@Override
	public void onResume() {
		super.onResume();
		//Retrieve Information if internet is working
				if(isInternetAvailable()){
					new RdsDataTask(new getRdsData()).execute(eventID);
				}else{
					tv.setText("NO INTERNET CONNECTION");
					load.setVisibility(View.GONE);
				}
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

					Intent intent = new Intent(eventDetail.this,login_activity.class);
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

					Intent intent = new Intent(eventDetail.this,login_activity.class);
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
			uploadButton();
			galleryButton();
		}
	}
	
	public void logout(){
    	progress = new ProgressDialog(eventDetail.this);
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
	
	public void uploadButton(){
		upload.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				loginstate=lm.state();
				if(loginstate==true)
				{
					Intent newInt=new Intent(eventDetail.this,UploadImage.class);
					newInt.putExtra("eventID", eventID);
					startActivity(newInt);
				}else{
					lm.incCount();
					Intent newInt=new Intent(eventDetail.this,login_activity.class);
					startActivity(newInt);
				}
			}
		});
	}
	public void galleryButton(){
		gallery.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				loginstate=lm.state();
				if(loginstate==true){
					Intent newInt=new Intent(eventDetail.this,GalleryList.class);
					newInt.putExtra("eventID", eventID);
					startActivity(newInt);
				}
				else{
					lm.incCount();
					Intent newInt=new Intent(eventDetail.this,login_activity.class);
					startActivity(newInt);
				}
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class getRdsData implements Rdsinter<String>{

		public void onTaskComplete(String result) {
			load.setVisibility(View.GONE);
			tv.setText(result);
		}
		
	}
	
}
