package prashantgrg.limerickevents;


import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private Context context;
	public ArrayList<Bitmap> bitmap;
	
	public ImageAdapter(Context c, ArrayList<Bitmap> bm){
		context=c;
		bitmap=bm;
	}
	
	@Override
	public int getCount() {
		return bitmap.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		  if (convertView == null) {
		  imageView = new ImageView(context);
	      imageView.setLayoutParams(new GridView.LayoutParams(250, 250));
	      imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	      } else {
	    	imageView = (ImageView) convertView;
	      }

	      imageView.setImageBitmap(bitmap.get(position));
	      return imageView;
	}

}

