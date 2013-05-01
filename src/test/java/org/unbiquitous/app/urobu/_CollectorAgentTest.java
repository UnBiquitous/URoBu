package org.unbiquitous.app.urobu;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.driver.execution.executeAgent.Agent;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;

public class _CollectorAgentTest {

	@Test public void mustBeAnAgent(){
		assertThat(new CollectorAgent()).isInstanceOf(Agent.class);
	}
	
	@Test public void mustBeSendCollectorDataToApp() throws Exception{
		CollectorAgent agent = new CollectorAgent();
		Map<String, Object> collectedData = createDummyData();
		String appId = "me";
		UpDevice origin = setOriginDevice(agent, appId);
		Gateway gateway = mock(Gateway.class);
		
		agent.run(gateway);
		
		ArgumentCaptor<ServiceCall> callCaptor = ArgumentCaptor.forClass(ServiceCall.class);
		
		verify(gateway).callService(eq(origin),callCaptor.capture());
		assertThat(callCaptor.getValue().getInstanceId()).isEqualTo(appId);
		assertThat(callCaptor.getValue().getDriver()).isEqualTo("app");
		assertThat(callCaptor.getValue().getService()).isEqualTo("dataCollected");
		assertThat(callCaptor.getValue().getParameters()).isEqualTo(collectedData);
	}

	private UpDevice setOriginDevice(CollectorAgent agent, String appId) {
		UpDevice origin = new UpDevice("origin")
			.addNetworkInterface("127.here", "ether");
		UpNetworkInterface addr = origin.getNetworks().get(0);
		agent.setOriginDevice(	origin.getName(), 
								addr.getNetworkAddress(), 
								addr.getNetType(), 
								appId);
		return origin;
	}

	private Map<String, Object> createDummyData() {
		Map<String, Object> collectedData = new HashMap<String, Object>();
		
		Collector dummyCollector = new Collector(){
			public Point getCameraResolution() {
				return new Point(10,20);
			}
			public Float getMaxAudioInputAmplitude() {
				return 30f;
			}
			public Float getMaxAudioOutputAmplitude() {
				return 40f;
			}
			public Map<String, String> getPlatform() {
				Map<String, String> map = new HashMap<String, String>();
				map.put("a","1");
				return map;
			}
			public Point getScreenSize() {
				return new Point(50,60);
			}
			public String getUserName() {
				return "John";
			}
			
		};
		
		collectedData.put("user",dummyCollector.getUserName());
		collectedData.put("platform",dummyCollector.getPlatform());
		collectedData.put("screenSize",dummyCollector.getScreenSize());
		collectedData.put("cameraResolution",dummyCollector.getCameraResolution());
		collectedData.put("maxAudioInputAmplitude",dummyCollector.getMaxAudioInputAmplitude());
		collectedData.put("maxAudioOutputAmplitude",dummyCollector.getMaxAudioOutputAmplitude());
		
		
		
		Collector.setInstance(dummyCollector);
		return collectedData;
	}
	
}
