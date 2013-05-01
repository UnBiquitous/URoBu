package org.unbiquitous.app.urobu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unbiquitous.driver.execution.executeAgent.AgentUtil;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyDeploy;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyStart;
import org.unbiquitous.uos.core.ontologyEngine.api.OntologyUndeploy;

public class Urobu implements UosApplication{
	
	private Set<UpDevice> visited ;
	private Map<String, Map<String, Object>> userStats ;
	private String myId;
	
	@Override
	public void init(OntologyDeploy ontology, String id) {
		this.myId = id;
		visited = new HashSet<UpDevice>();
		userStats = new HashMap<String, Map<String,Object>>();
	}

	@Override
	public void start(Gateway gateway, OntologyStart ontology) {
		while(true){
			loop(gateway, ontology); //TODO: test this
		}
	}
	
	void loop(Gateway gateway, OntologyStart ontology) {
		try {
			UpDevice currentDevice = gateway.getCurrentDevice();
			UpNetworkInterface network = currentDevice.getNetworks().get(0);
			for(UpDevice device : gateway.listDevices()){
				if (!visited.contains(device)){
					visited.add(device);
					sendAgent(gateway, currentDevice, network, device);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendAgent(Gateway gateway, UpDevice currentDevice,
			UpNetworkInterface network, UpDevice device) throws Exception {
		CollectorAgent agent = new CollectorAgent();
		agent.setOriginDevice(currentDevice.getName(), 
								network.getNetworkAddress(), 
								network.getNetType(), this.myId);
		AgentUtil.getInstance().move(agent, device, gateway);
	}

	@Override
	public void stop() throws Exception {}

	@Override
	public void tearDown(OntologyUndeploy ontology) throws Exception {}
	
	public Map<String,Object> dataCollected(Map<String,Object> parameter){
		String user = (String) parameter.get("user");
		
		if (!userStats.containsKey(user)){
			userStats.put(user, new HashMap<String, Object>());
		}
		
		Map<String, Object> userMap = userStats.get(user);
		
		populatePlatformInfo(parameter, userMap);
		populateDevices(parameter, userMap);
		populateDrivers(parameter, userMap);
		
		return null;
	}

	@SuppressWarnings("unchecked")
	private void populateDevices(Map<String, Object> parameter,
			Map<String, Object> userMap) {
		List<String> devices;
		if (!userMap.containsKey("devices")){
			devices = new ArrayList<String>();
			userMap.put("devices",devices);
		}else{
			devices = (List<String>) userMap.get("devices");
		}
		devices.add((String) parameter.get("device"));
	}
	
	@SuppressWarnings("unchecked")
	private void populateDrivers(Map<String, Object> parameter,
			Map<String, Object> userMap) {
		Set<String> drivers;
		if (!userMap.containsKey("drivers")){
			drivers = new HashSet<String>();
			userMap.put("drivers",drivers);
		}else{
			drivers = (Set<String>) userMap.get("drivers");
		}
		drivers.addAll((List<String>)parameter.get("drivers"));
	}

	@SuppressWarnings("unchecked")
	private void populatePlatformInfo(Map<String, Object> parameter,
			Map<String, Object> userMap) {
		Map<String, String> platform = (Map<String, String>) parameter.get("platform");
		userMap.put("language",platform.get("language"));
	}
	
	Map<String, Map<String, Object>> userStats() {
		return userStats;
	}

}
