package prashantgrg.limerickevents;


import java.io.File;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UploadImage extends Activity {
		private final String ACCESSID = Constants.ACCESSID;
		private final String ACCESSKEY = Constants.ACCESSKEY;
		private AWSCredentials credentials;
		private String bucket;
		private uploadPhoto uploadphoto;
		private String eventID;
		private TextView filenameText;
		private Button b;
		private Button upload;
		private Uri uri;
		private String filePath,realName;
		private AmazonS3 s3;
		private ProgressDialog progress;
		
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.uploadimage);
	        bucket="2event";
	        Intent i=getIntent();
	        int tmp = i.getIntExtra("eventID",0);
	        eventID=Integer.toString(tmp);
	        uploadphoto=null;
	        uri = null;
	        upload = (Button) findViewById(R.id.upload_button);
	        b = (Button) findViewById(R.id.select);
	        filenameText = (TextView) findViewById(R.id.file_tv);
			getActionBar().setDisplayHomeAsUpEnabled(true);        
	        
	        b.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startActivityForResult(new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI),54);
				}
			});
	        
	        setUpload();
	    }
	  
	  public void setUpload(){
		  upload.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(uri!=null){
						uploadphoto=new uploadPhoto(uri, filePath, realName);
						uploadphoto.execute();
						uri=null;
					}
					else{
						filenameText.setText("Please choose a Picture");
					}
				}
			});
	  }
		 public boolean onOptionsItemSelected(MenuItem item) {
		   		
		 		switch(item.getItemId()){
		 		case android.R.id.home:
		 			finish();
		 			return true;
		 		}
		 		return super.onOptionsItemSelected(item);
		 }
		 
		 

	  public void onActivityResult(int requestCode, int resultCode, Intent data){
		  super.onActivityResult(requestCode, resultCode, data);
		  if(requestCode == 54 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
			  	uri=data.getData();
			  	String[] filePathColumn = { MediaStore.Images.Media.DATA };
                // Get the cursor
                Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
				realName=filePath.substring(filePath.lastIndexOf("/")+1,filePath.length());
				filenameText.setText("File Selected : " + realName);
			  
		  }else{
			  filenameText.setText("Cannot get the Image");
		  }
	  }
	  
	  
	  //this task will decode the file path and then upload on the server
	  public class uploadPhoto extends AsyncTask<Void, Void, Boolean>{

		 private Uri uri;
		 private String filepath,realname;
		 private boolean success;
		 
		 public  uploadPhoto(Uri u, String f, String r){
			uri=u;
			filepath=f;
			realname=r;
			success=false;
		}
		 
		@Override
		protected Boolean doInBackground(Void... params) {
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
			
			realname=filepath.substring(filepath.lastIndexOf("/")+1,filepath.length());
			
			try{
				File file = new File(filepath);
		        Region euWest1 = Region.getRegion(Regions.EU_WEST_1);
		        s3.setRegion(euWest1);
		        s3.putObject(new PutObjectRequest(bucket, eventID+"/"+realname, file ){});
		        success=true;
			}catch(Exception e){
				success=false;
			}
			return success;
		}
		

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			success=result;
			progress.dismiss();
			if(success){
				filenameText.setText("File Upload Success");
			}else{
				
				filenameText.setText("File Upload Error");
			}
			setUpload();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(UploadImage.this);
			progress.setTitle("Uploading Photo...");
			progress.setMessage("Please wait...");
			progress.setCancelable(false);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.show();
		}
		
	  }
	  
}
