package org.unbiquitous.app.urobu;

import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaRecorder;

public class Collector {

	private Context context;

	private Activity activity;
	
	public Collector(Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
	}
	
	public Map<String,Object> collectData(){
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("user",getUserName());
		data.put("platform",getPlatform());
		data.put("screenSize",getScreenSize());
		data.put("cameraResolution",getCameraResolution());
		data.put("maxAudioInputAmplitude",getMaxAudioInputAmplitude());
		data.put("maxAudioOutputAmplitude",getMaxAudioOutputAmplitude());
		//Keybaord
		//Multitouch
		//Drivers
		return data;
	}
	
	public String getUserName(){
		AccountManager manager = AccountManager.get(context);
		Account[] accounts = manager.getAccounts();
		if (accounts != null && accounts.length > 0){
			return accounts[0].name;
		}
		return null;
	}
	
	public Map<String,String> getPlatform(){
		HashMap<String, String> props = new HashMap<String, String>();
		Runtime runtime = Runtime.getRuntime();
		props.put("jvm", System.getProperty("java.vm.name"));
		props.put("os", System.getProperty("os.name"));
		props.put("architecture", System.getProperty("os.arch"));
		props.put("language", System.getProperty("user.language"));
		props.put("numberOfProcessors", Integer.toString(runtime.availableProcessors()));
		props.put("Memory", Long.toString(runtime.maxMemory()));
		return props;
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public Point getScreenSize(){
		Point size = new Point();
		if (android.os.Build.VERSION.SDK_INT < 13){
			size.x = activity.getWindowManager().getDefaultDisplay().getWidth();
			size.y = activity.getWindowManager().getDefaultDisplay().getHeight();
		}else{
			activity.getWindowManager().getDefaultDisplay().getSize(size);
		}
		return size;
	}
	
	public Point getCameraResolution(){
		try {
			Camera camera = Camera.open();
			Size biggest = null;
			for (Size s : camera.getParameters().getSupportedPictureSizes()){
				if (biggest == null){
					biggest = s;
				}else if (s.height > biggest.height && s.width > biggest.width){
					biggest = s;
				}
			}
			return new Point(biggest.width,biggest.height);
		} catch (Exception e) {
			return null;
		}
	}
	
	public Integer getMaxAudioInputAmplitude(){
		//http://stackoverflow.com/questions/4777060/android-sample-microphone-without-recording-to-get-live-amplitude-level
		//http://stackoverflow.com/questions/7197798/get-the-microphone-sound-level-decibel-level-in-android
		try {
			return MediaRecorder.getAudioSourceMax();
		} catch (Exception e) {
			return null;
		}
	}
	
	public Integer getMaxAudioOutputAmplitude(){
		//http://stackoverflow.com/questions/9599259/android-how-to-play-music-at-maximum-possible-volume
		AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return manager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	
}
