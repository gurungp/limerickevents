package prashantgrg.limerickevents;


import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class PhotoView extends Activity {

	private final String ACCESSID = Constants.ACCESSID;
	private final String ACCESSKEY = Constants.ACCESSKEY;
	private AWSCredentials credentials;
	private String temp;
	private String bucket;
	private ImageView imageView;
	private Bitmap tmp;
	private AmazonS3 s3;
	private ProgressBar progress;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_photoview);
		bucket="2event";
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent i = getIntent();        // Get intent data
		temp = i.getExtras().getString("Name");
		imageView = (ImageView) findViewById(R.id.single_photo);
		progress = (ProgressBar) findViewById(R.id.progressBar);
		photoDisplay p1=new photoDisplay();
		p1.execute();
	}

	 public boolean onOptionsItemSelected(MenuItem item) {
	   		
 		switch(item.getItemId()){
 		case android.R.id.home:
 			finish();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
	 }
	
	 public class photoDisplay extends AsyncTask<Void, Void, Bitmap>{

		protected void onPreExecute(Void... params){
			
		}
		 
		protected Bitmap doInBackground(Void... params) {
			try {
				credentials = new BasicAWSCredentials(ACCESSID, ACCESSKEY); 

			} catch (Exception e) {
				throw new AmazonClientException(
						"Cannot load the credentials from the credential profiles file. " +
								"Please make sure that your credentials file is at the correct " +
								"location (C:\\Users\\ ... \\.aws\\credentials), and is in valid format.",
								e);
			}

			s3 = new AmazonS3Client(credentials);
			Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
			s3.setRegion(euWest1);
			
			S3Object obj = s3.getObject(new GetObjectRequest(bucket, temp));
			try {
				byte[] b = IOUtils.toByteArray(obj.getObjectContent());
				BitmapFactory.Options imageOpts = new BitmapFactory.Options();
				System.out.println("byte length : " +b.length );
				if(b.length>=4500000){
					imageOpts.inSampleSize = 8; 
				}else{
					imageOpts.inSampleSize = 1; 
				}
				tmp = BitmapFactory.decodeByteArray(b, 0, b.length,imageOpts);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return tmp;
		}
		 
		protected void onPostExecute(Bitmap	b){
			progress.setVisibility(View.GONE);
			tmp=b;
			imageView.setImageBitmap(tmp);
		}
	 }

}
