package org.unbiquitous.app.urobu;

import org.unbiquitous.driver.execution.executeAgent.Agent;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;

public class CollectorAgent extends Agent{
	private static final long serialVersionUID = -2445441397378542216L;
	private static final Logger logger = Logger.getLogger(CollectorAgent.class);

	String appId;
	UpDevice origin;

	public void setOriginDevice(String name, String address, String netType,
								String appId){
		origin = new UpDevice(name).addNetworkInterface(address, netType);
		this.appId = appId;
	}
	
	
	@Override
	public void run(Gateway gateway) {
		try {
			Collector collector = Collector.getInstance();
			ServiceCall call = new ServiceCall("app","dataCollected", appId);
			call.setParameters(collector.collectData());
			gateway.callService(origin,call);
		} catch (ServiceCallException e) {
			logger.error(e);
		}
	}

}
