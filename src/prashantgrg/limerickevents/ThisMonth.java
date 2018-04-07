package prashantgrg.limerickevents;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class ThisMonth extends Activity {


	private Button home;
	private ResultSet rs;
	private static int resultsetSize;
	private byte[][] bitmapdata; 
	private SimpleDateFormat format;
	private SimpleDateFormat format2;
	private String dateString;
	private String map[];
	private String eventName[];
	private int j;
	private loginManager lm;
	private Button loginBtn;
	private boolean loginstate;
	private ProgressBar load;
	private ImageView back,forward;
	private LinearLayout contentHolder;
	private Boolean internet;
	private int offset,prefer,lastid,padding_in_dp,padding_in_px,paddingtop;
	private ProgressDialog progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.events_list_view);
		lm = (loginManager) getApplicationContext();
		loginstate = lm.state(); // checking the login state of the user
		internet = false;
		offset=0;  //offset used to get the data from database starting from offset
		prefer=0; // if 1 then user has pressed forward, if 2 then, user has clicked back.
				 // In case of 0 it is running for first time.
		lastid=1;
		padding_in_dp = 7;  // 6 dps
	    final float scale = getResources().getDisplayMetrics().density;
	    padding_in_px = (int) (padding_in_dp * scale + 0.5f);
	    paddingtop = (int) (10 * scale + 0.5f);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		/*----- Gathering view resources of the screen/layout -----*/	
		home = (Button) findViewById(R.id.home_img);
		back = (ImageView) findViewById(R.id.back);
		forward = (ImageView) findViewById(R.id.forward);
		format = new SimpleDateFormat("yyyy-MM-dd");
		format2 = new SimpleDateFormat("MMMM dd, yyyy");
		load = (ProgressBar) findViewById(R.id.loading);
		contentHolder = (LinearLayout) findViewById(R.id.contentHolder);

		// click listener for back button, it will finish this activity	

		home.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent newInt=new Intent(ThisMonth.this,MainActivity.class);
				newInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(newInt);
			}
		});
		
		//Retrieve Information if internet is working
		if(isInternetAvailable()){
			new RdsDataThisMonth(new rdsData(), offset, 2).execute();
		}else{
			TextView warning = new TextView(this);
			warning.setGravity(Gravity.CENTER_HORIZONTAL);
			warning.setText("NO INTERNET CONNECTION");
			load.setVisibility(View.GONE);
			contentHolder.addView(warning);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	 public boolean onOptionsItemSelected(MenuItem item) {
	   		
	 		switch(item.getItemId()){
	 		case android.R.id.home:
	 			finish();
	 			return true;
	 		}
	 		return super.onOptionsItemSelected(item);
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

					Intent intent = new Intent(ThisMonth.this,login_activity.class);
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

					Intent intent = new Intent(ThisMonth.this,login_activity.class);
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
    	progress = new ProgressDialog(ThisMonth.this);
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

	public void setPrevNextButton(){
		if(resultsetSize>=1){ // if there are results returned
			forward.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					resetView();
					prefer=1;
					try {
						rs.last();
						offset=rs.getInt(1); // setting the id of the last loaded event to offset
						lastid=offset+1;
					} catch (SQLException e) {
						e.printStackTrace();
					}							
					new RdsDataAllEvents(new rdsData(), offset, 1).execute();
				}
			});
			
			back.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					resetView();
					prefer=2;
					try {
						rs.first();
						offset=rs.getInt(1);
						lastid=offset-1;
					} catch (SQLException e) {
						e.printStackTrace();
					}
					new RdsDataAllEvents(new rdsData(), offset, 0).execute();						
				}
			});
		}else{
			checkPrevNextButton();	
		}
	}
	
	public void checkPrevNextButton(){
		if(prefer==1){ // if user had clicked forward button previously
			back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					resetView();
					prefer=2;						
					new RdsDataAllEvents(new rdsData(), lastid, 0).execute();							
				}
			});
			forward.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {		
				}
			});
		}else if(prefer==2){// if user had clicked back button previously
			forward.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					resetView();
					prefer=1;						
					new RdsDataAllEvents(new rdsData(), lastid, 1).execute();							
				}
			});
			back.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {						
				}
			});
		}else{}
	}
	public void resetView(){
		contentHolder.removeAllViews();
		contentHolder.addView(load);
		ViewGroup.LayoutParams layoutParams = contentHolder.getLayoutParams(); 
		ScrollView.LayoutParams castLayoutParams = (ScrollView.LayoutParams) layoutParams; 
		castLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		load.setVisibility(View.VISIBLE);
	}
	public class rdsData implements Rdsinter<ResultSet>{

		public void onTaskComplete(ResultSet result) {

			load.setVisibility(View.GONE);
			rs=result; //getting result from the Rdsinter interface which is used by an Aysnctask class
			boolean anyResult = false;
			try {
				anyResult = rs.first(); // to check if we have any data on the resultset
			} catch (SQLException e3) {
				e3.printStackTrace();
			}
			
			if(!anyResult&&prefer==0){ 
				TextView message = new TextView(ThisMonth.this);
				message.setGravity(Gravity.CENTER_HORIZONTAL);
				message.setText("NO EVENTS IN THIS PERIOD");
				contentHolder.addView(message);
				checkPrevNextButton();
			}
			else if(!anyResult&&prefer==2){
				TextView message = new TextView(ThisMonth.this);
				message.setGravity(Gravity.CENTER_HORIZONTAL);
				message.setText("NO PREVIOUS EVENTS");
				contentHolder.addView(message);
				checkPrevNextButton();
			}
			else if(!anyResult&&prefer==1){
				TextView message = new TextView(ThisMonth.this);
				message.setGravity(Gravity.CENTER_HORIZONTAL);
				message.setText("NO FURTHER EVENTS");
				contentHolder.addView(message);
				checkPrevNextButton();
			}
			else{
				
				/*-- Setting Parameters to adjust the image and text layout on the screen --*/	
				LinearLayout.LayoutParams logoP= new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				LinearLayout.LayoutParams textGrav= new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				textGrav.gravity = Gravity.END;
				logoP.gravity = Gravity.START;
				logoP.width = 250;
				logoP.height = 250;

				// layout params for the linear layouts inside the Scroll View	
				LinearLayout.LayoutParams ctLayout= new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				// using scrollview here, for the layout of the child linearlayout, because it should be of parent's class type
				ScrollView.LayoutParams ctLayout2= new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT); 


				contentHolder.setLayoutParams(ctLayout2); 
				contentHolder.setOrientation(LinearLayout.VERTICAL);
				try {
					rs.last();
					resultsetSize= rs.getRow();

				} catch (SQLException e2) {
					e2.printStackTrace();
				}

				try {
					rs.first();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				String text[]=new String[resultsetSize];
				map=new String[resultsetSize];
				eventName=new String[resultsetSize];
				bitmapdata=new byte[resultsetSize][];
				setPrevNextButton();
				
				/*----This loop will store the Title, Date , Time and Location of an event----*/
				for(int i=0;i<=resultsetSize;i++){
					try {

						Date date = format.parse(rs.getString(3));
						dateString = format2.format(date);
						text[i] = rs.getString(2) + System.getProperty ("line.separator") + dateString + 
								System.getProperty ("line.separator") + rs.getString(4) + System.getProperty ("line.separator")
								+ rs.getString(5);

						rs.next();
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				try {
					rs.first();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}			


				getImages task=null;

				for(int i=1;i<=resultsetSize;i++)
				{
					j=i-1;
					final LinearLayout contentLayout = new LinearLayout(ThisMonth.this);
					contentLayout.setOrientation(LinearLayout.HORIZONTAL);
					contentLayout.setLayoutParams(ctLayout);
					try {
						contentLayout.setId(rs.getInt(1));
						contentLayout.setTag(j);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					try {
						task = new getImages("https://s3-eu-west-1.amazonaws.com/limerickeventphotos" + "/" + rs.getString(6));
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					ExecutorService ex=Executors.newFixedThreadPool(1);
					Future<Drawable> future = ex.submit(task);
					Drawable dx=null;

					try {
						dx=future.get(); //getting the Drawable from separate thread
						ImageView iv=new ImageView(ThisMonth.this); 
						iv.setImageDrawable(dx); // setting the image to the image view
						iv.setLayoutParams(logoP); 
						iv.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

						/*---------- FOR TEXT ------------*/
						TextView tv=new TextView(ThisMonth.this);
						//	tv.setText(Integer.toString(contentLayout.getId())); 
						tv.setText(text[j]); //
						tv.setLayoutParams(textGrav);
						tv.setTextSize(12);
						tv.setPadding(padding_in_px, paddingtop, padding_in_px, padding_in_px);


						contentLayout.addView(iv); //adding the image 
						contentLayout.addView(tv); // and adding text to the linear layout, contentLayout
						contentHolder.addView(contentLayout); // adding the contentLayout as a child view to contentHolder

					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} 

					/*---converting drawable to bytearray to put as Extra below. ----*/
					Bitmap bit=((BitmapDrawable)dx).getBitmap();
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bit.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					bitmapdata[j] = stream.toByteArray();
					/*-------   ------------     -------*/

					try {
						map[j] = rs.getString(5);
						eventName[j]=rs.getString(2);
					} catch (SQLException e1) {
						e1.printStackTrace();
					}


					contentLayout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent detailPage = new Intent(ThisMonth.this,eventDetail.class);
							detailPage.putExtra("EventID", contentLayout.getId());
							detailPage.putExtra("eventName", eventName[(Integer) contentLayout.getTag()]);
							detailPage.putExtra("photo", bitmapdata[(Integer) contentLayout.getTag()]);
							detailPage.putExtra("mapCoor","geo:0,0?q=" + map[(Integer) contentLayout.getTag()]);							
							startActivity(detailPage);
						}
					});
					try {
						rs.next(); //to the values of next row of the database
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}	
			}
		}
		public class getImages implements Callable<Drawable>{ // for creating threads to retrieve images from web server
			private final String url;
			Drawable d=null;

			public getImages(String u){
				url=u;
			}
			public Drawable call() throws Exception {

				try{
					InputStream inputStream = new URL(url).openStream();
					d = Drawable.createFromStream(inputStream, null);
					inputStream.close();

				}
				catch(MalformedURLException ex){}
				catch(IOException ex){}

				return d;
			}

		}
	}


}
