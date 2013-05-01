package org.unbiquitous.app.urobu;

import java.util.HashSet;
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
	private String myId;
	
	@Override
	public void init(OntologyDeploy ontology, String id) {
		this.myId = id;
		visited = new HashSet<UpDevice>();
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
	
//	public Map<String,Object> dataCollected(Map<String,Object> parameter){
//		return callbackMap = parameter;
//	}

}
