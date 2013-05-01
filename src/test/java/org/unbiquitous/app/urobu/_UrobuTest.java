package org.unbiquitous.app.urobu;

import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.driver.execution.executeAgent.Agent;
import org.unbiquitous.driver.execution.executeAgent.AgentUtil;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.UosApplication;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

import com.google.common.collect.Lists;

public class _UrobuTest {

	
	private Urobu app;
	private Gateway gateway;
	private AgentUtil agentUtil;
	private ArgumentCaptor<Agent> agentCatcher;
	private UpDevice thisDevice;

	@Before public void setUp(){
		app = new Urobu();
		app.init(null, null);
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
		//TODO: and the id?
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
	
}
