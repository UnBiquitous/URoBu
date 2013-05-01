package org.unbiquitous.app.urobu;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.Port.Info;

public class Collector {

	private static Collector instance;

	public static Collector getInstance(){
		if (Collector.instance != null) return Collector.instance;
		return new Collector(); 
	}
	public static void setInstance(Collector instance){
		Collector.instance = instance;
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
		return System.getProperty("user.name");
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
	
	public Point getScreenSize(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		return new Point(width,height);
	}
	
	public Point getCameraResolution(){
		return null;
	}
	
	public Float getMaxAudioInputAmplitude(){
		return  getMaxVolumeFromMixer(Port.Info.MICROPHONE);
	}
	
	public Float getMaxAudioOutputAmplitude() {
		return  getMaxVolumeFromMixer(Port.Info.SPEAKER);
	}

	private Float getMaxVolumeFromMixer(Info lineType) {
		Float value = null;
		try {
			Mixer.Info[] infos = AudioSystem.getMixerInfo();
			for (Mixer.Info info : infos) {
				Mixer mixer = AudioSystem.getMixer(info);
				if (mixer.isLineSupported(lineType)) {
					Port port = (Port) mixer.getLine(lineType);
					port.open();
					if (port.isControlSupported(FloatControl.Type.VOLUME)) {
						FloatControl volume = (FloatControl) port
								.getControl(FloatControl.Type.VOLUME);
						value = volume.getMaximum();
					}
					port.close();
				}
			}
		} catch (LineUnavailableException e) {}
		return value;
	}
	
}
