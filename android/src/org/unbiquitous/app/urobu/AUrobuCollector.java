package org.unbiquitous.app.urobu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;

public class AUrobuCollector implements Serializable {
	private static final long serialVersionUID = 5411368741627368847L;
	
	private String name;
	private String address;
	private String netType;
	private String appId;


	public void setOriginDevice(String name, String address, String netType,
								String appId){
		this.name = name;
		this.address = address;
		this.netType = netType;
		this.appId = appId;
	}
	
	
	@SuppressWarnings("rawtypes")
	public void run(final Map gateway){
		Map<String, Object> data = collectRawData(gateway);
		collectDrivers(gateway, data);
		collectDevice(gateway, data);
		sendData(gateway, data);
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void sendData(final Map gateway, Map<String, Object> data) {
		Map<String, Object> device = createDevice();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		parameters.put("device", device);
		parameters.put("serviceName", "collectData");
		parameters.put("driverName", "app");
		parameters.put("instanceId", appId);
		parameters.put("parameters", data);
		
		gateway.put("callService", parameters);
	}


	private Map<String, Object> createDevice() {
		Map<String, Object> device = new HashMap<String, Object>();
		Map<String, String> networkInterface = new HashMap<String, String>();
		device.put("name", name);
		networkInterface.put("networkAddress",address);
		networkInterface.put("netType",netType);
		
		List<Map<String, String>> networks = new ArrayList<Map<String,String>>();
		device.put("networks", networks);
		return device;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void collectDrivers(final Map gateway, Map<String, Object> data) {
		List<Map<String, Object>> drivers = 
									(List<Map<String, Object>>) 
									gateway.put("listDrivers",new HashMap());
		
		Set<String> driverSet = new HashSet<String>();
		for (Map<String, Object> driverData: drivers){
			Map<String, Object> driverMap = 
								(Map<String, Object>) driverData.get("driver");
			driverSet.add((String) driverMap.get("name"));
		}
		data.put("drivers", driverSet);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void collectDevice(final Map gateway, Map<String, Object> data) {
		Map<String, Object> device = (Map<String, Object>) 
										gateway.put("getCurrentDevice",null);
		
		data.put("device", device.get("name"));
	}

	@SuppressWarnings({  "rawtypes" })
	private Map<String, Object> collectRawData(final Map gateway) {
		Collector collector = new Collector(
									(Context)gateway.get("context"), 
									(Activity)gateway.get("activity"));
		Map<String, Object> data = collector.collectData();
		return data;
	}
}
