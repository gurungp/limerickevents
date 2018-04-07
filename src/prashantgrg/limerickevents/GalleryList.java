package prashantgrg.limerickevents;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GalleryList extends Activity {

	private String eventID;
	private ImageView back;
	private GridView gv;
	private String bucket;
	private String ACCESSID = Constants.ACCESSID;
	private String ACCESSKEY = Constants.ACCESSKEY;
	private AWSCredentials credentials;
	private S3Object[] obj; 
	private loginManager lm;
	private boolean loginstate;
	private Button loginBtn,home;
	private Boolean internet;
	private LinearLayout content;
	private ProgressBar load;
	private ProgressDialog progress;
	private AmazonS3 s3client;
	private int listsize;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listsize=0;
		setContentView(R.layout.photo_list);
		lm = (loginManager) getApplicationContext();
		loginstate = lm.state();
		internet = false;
		getActionBar().setDisplayHomeAsUpEnabled(true);
		back = (ImageView) findViewById(R.id.back);
		home = (Button) findViewById(R.id.home_img);		
		content = (LinearLayout) findViewById(R.id.loader);
		gv=(GridView)findViewById(R.id.photo_grid);
		gv.setVisibility(View.INVISIBLE);
		load = (ProgressBar) findViewById(R.id.loading);

		Intent intent=getIntent();
		eventID = Integer.toString(intent.getIntExtra("eventID", 0));
		bucket = "2event";

		back.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				finish();
			}
		});

		 home.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					Intent newInt=new Intent(GalleryList.this,MainActivity.class);
					newInt.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(newInt);
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

					Intent intent = new Intent(GalleryList.this,login_activity.class);
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

					Intent intent = new Intent(GalleryList.this,login_activity.class);
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

		if(isInternetAvailable()){
			photodownload p1=new photodownload();
			p1.execute();
		}
		else{
			TextView warning = new TextView(this);
			warning.setGravity(Gravity.CENTER_HORIZONTAL);
			warning.setText("NO INTERNET CONNECTION");
			gv.setVisibility(View.GONE);
			content.addView(warning);
		}
	}

	public void logout(){
    	progress = new ProgressDialog(GalleryList.this);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	/*--- class to retrieve all the photos on the event by connecting to the Amazon s3 server --*/

	public class photodownload extends AsyncTask<Void, Void, ArrayList<Bitmap>>{

		protected void onPreExecute(){
			try {
				credentials = new BasicAWSCredentials(ACCESSID, ACCESSKEY); 

			} catch (Exception e) {
				throw new AmazonClientException(
						"Cannot load the credentials from the credential profiles file. " +
								"Please make sure that your credentials file is at the correct " +
								"location (C:\\Users\\ ... \\.aws\\credentials), and is in valid format.",
								e);
			}

			s3client = new AmazonS3Client(credentials);
			Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
			s3client.setRegion(euWest1);

		}
		
		@Override
		protected ArrayList<Bitmap> doInBackground(Void... params) {
			
			ObjectListing objectListing = s3client.listObjects(new ListObjectsRequest().withBucketName(bucket).withPrefix(eventID+"/").withMarker(eventID+"/"));
			int size = objectListing.getObjectSummaries().size();
			listsize=size;
			obj = new S3Object[size];
			
			// Bitmap[][] draw = new Bitmap[size][];
			ArrayList<Bitmap> draw = new ArrayList<Bitmap>();
			Bitmap temp = null;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 16;
			int count=0;
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				System.out.println("Object Summary: " + objectSummary.getKey());
				obj[count] = s3client.getObject(new GetObjectRequest(bucket, objectSummary.getKey()));
				try {
					byte[] b = IOUtils.toByteArray(obj[count].getObjectContent());
					temp = BitmapFactory.decodeByteArray(b, 0, b.length,options);
					draw.add(temp);
				} catch (IOException e) {
					e.printStackTrace();
				}

				count++;	                
			}

			return draw;
		}
		
		protected void onPostExecute(ArrayList<Bitmap> bit){
			
			load.setVisibility(View.GONE);
			if(listsize<=0){
				TextView message = new TextView(GalleryList.this);
				gv.setVisibility(View.GONE);
				message.setGravity(Gravity.CENTER_HORIZONTAL);
				message.setText("NO PHOTOS UPLOADED YET");
				content.addView(message);
			}else{
				gv.setAdapter(new ImageAdapter(GalleryList.this,bit));
				gv.setVisibility(View.VISIBLE);
				gv.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View v,int position, long id) {

						// Send intent to SingleViewActivity
						Intent i = new Intent(GalleryList.this, PhotoView.class);
						// Pass image index
						i.putExtra("id", position);
						i.putExtra("Name", obj[position].getKey());
						startActivity(i);
					}
				});	
			}
			
			
		}
	}
	
}
