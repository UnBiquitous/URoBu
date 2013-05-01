package org.unbiquitous.app.urobu;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.driver.execution.executeAgent.Agent;
import org.unbiquitous.driver.execution.executeAgent.AgentUtil;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class _UrobuTest {

	
	private Urobu app;
	private Gateway gateway;
	private AgentUtil agentUtil;
	private ArgumentCaptor<Agent> agentCatcher;
	private UpDevice thisDevice;

	@Before public void setUp(){
		app = new Urobu();
		app.init(null, "urobu_1");
		gateway = mock(Gateway.class);
		thisDevice = new UpDevice("Me")
								.addNetworkInterface("addr", "type");
		when(gateway.getCurrentDevice()).thenReturn(thisDevice);
		
		
		agentUtil = mock(AgentUtil.class);
		AgentUtil.setInstance(agentUtil);
		agentCatcher = ArgumentCaptor.forClass(Agent.class);
	}
	
	@Test public void UrobuIsAnApp(){
		assertThat(app).isInstanceOf(UosApplication.class);
	}
	
	@Test public void sendACollectorAgentToDevices() throws Exception{
		
		UpDevice targetDevice = new UpDevice("Test");
		when(gateway.listDevices()).thenReturn(Lists.newArrayList(targetDevice));
		
		app.loop(gateway, null);
		
		verify(agentUtil).move(agentCatcher.capture(), eq(targetDevice), eq(gateway));
		assertThat(agentCatcher.getValue()).isInstanceOf(CollectorAgent.class);
	}
	
	@Test public void setCollectorAgentWithDeviceData() throws Exception{
		when(gateway.listDevices())
						.thenReturn(Lists.newArrayList(new UpDevice("Test")));
		
		app.loop(gateway, null);
		
		verify(agentUtil).move(agentCatcher.capture(), (UpDevice) anyObject(), eq(gateway));
		CollectorAgent agent = (CollectorAgent) agentCatcher.getValue();
		assertThat(agent.origin).isEqualTo(thisDevice);
		assertThat(agent.appId).isEqualTo("urobu_1");
	}
	
	@Test public void sendAgentToNewDevices() throws Exception{
		UpDevice oldDevice = new UpDevice("Old");
		when(gateway.listDevices()).thenReturn(Lists.newArrayList(oldDevice));
		app.loop(gateway, null);
		
		UpDevice newDevice = new UpDevice("New");
		when(gateway.listDevices()).thenReturn(Lists.newArrayList(oldDevice,newDevice));
		app.loop(gateway, null);
		
		verify(agentUtil,times(1)).move(agentCatcher.capture(), eq(oldDevice), eq(gateway));
		verify(agentUtil,times(1)).move(agentCatcher.capture(), eq(newDevice), eq(gateway));
	}
	
	@SuppressWarnings("serial")
	@Test public void onDataCollectionRelatesTheUserWithTheData(){
		app.dataCollected(new HashMap<String, Object>(){
			{
				put("user","john");
				put("platform",  new HashMap<String,Object>(){
					{
						put("language","pt");
						put("architecture","64bits");
					}
				});
				put("device","my_nokia_s60");
				put("drivers",Lists.newArrayList("a","b"));
			}
		});
		Map<String,Map<String,Object>> stats= app.userStats();
		
		
		Map<String, Map<String, Object>> result = new HashMap<String, Map<String,Object>>(){
			{
				put("john",  new HashMap<String,Object>(){
					{
						put("language","pt");
						put("devices",Lists.newArrayList("my_nokia_s60"));
						put("drivers",Sets.newHashSet("a","b"));
					}
				});
			}
		};
		
		assertThat(stats).isEqualTo(result);
	}
	
	@SuppressWarnings("serial")
	@Test public void onEachDataCollectionAgregatesUserWithTheData(){
		app.dataCollected(new HashMap<String, Object>(){
			{
				put("user","john");
				put("platform",  new HashMap<String,Object>(){
					{
						put("language","pt");
						put("architecture","64bits");
					}
				});
				put("device","my_nokia_s60");
				put("drivers",Lists.newArrayList("a","b"));
			}
		});
		app.dataCollected(new HashMap<String, Object>(){
			{
				put("user","john");
				put("platform",  new HashMap<String,Object>(){
					{
						put("language","en");
						put("architecture","32bits");
					}
				});
				put("device","my_nexus_one");
				put("drivers",Lists.newArrayList("b","c"));
			}
		});
		
		Map<String,Map<String,Object>> stats= app.userStats();
		
		
		Map<String, Map<String, Object>> result = new HashMap<String, Map<String,Object>>(){
			{
				put("john",  new HashMap<String,Object>(){
					{
						put("language","en");
						put("devices",Lists.newArrayList("my_nokia_s60","my_nexus_one"));
						put("drivers",Sets.newHashSet("a","b","c"));
					}
				});
			}
		};
		
		assertThat(stats).isEqualTo(result);
	}
	
}
