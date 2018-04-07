package prashantgrg.limerickevents;

import android.app.Application;

public class loginManager extends Application {
	
	private boolean state;
	private int count;
	
	public loginManager(){
		state=false;
		count=0;
	}

	public boolean state(){
		return state;
	}
	
	public void login(){
		state=true;
	}
	public void logout(){
		state=false;
	}
	public int count(){
		return count;
	}
	public int incCount(){
		return count++;
	}
	public int decCount(){
		return count--;
	}
}
