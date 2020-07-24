package gcm.manual.newagent;

import gcm.manual.newagent.events.ExtendedMessageEvent;
import gcm.manual.newagent.events.MessageEvent;
import gcm.manual.newagent.events.ValueEvent;
import gcm.manual.newagent.events.Vector2DEvent;
import gcm.manual.newagent.events.Vector3DEvent;

public class SimClient {

	public void execute() {
		Sim sim = new Sim();
		
		//Registering event handling base on agent type -- the "type" is fully defined by the client
		sim.registerEventHandler(AgentType.INFECTED, ValueEvent.class, AgentBehaviorProvider::handleValueEvent_INFECTED);
		sim.registerEventHandler(AgentType.EXPOSED, ValueEvent.class, AgentBehaviorProvider::handleValueEvent_EXPOSED);
		sim.registerEventHandler(AgentType.EXPOSED, Vector2DEvent.class, AgentBehaviorProvider::handleVectorEvent_EXPOSED);
		sim.registerEventHandler(AgentType.RECOVERED, MessageEvent.class, AgentBehaviorProvider::handleMessageEvent_RECOVERED);
		
		//Addition of agents to the sim -- the agent ids are fully defined by the client
		sim.addAgent(AgentType.EXPOSED, "Exposed1");
		sim.addAgent(AgentType.EXPOSED, "Exposed2");
		sim.addAgent(AgentType.INFECTED, "Infected1");
		sim.addAgent(AgentType.INFECTED, "Infected2");
		sim.addAgent(AgentType.RECOVERED, "Recoved1");
		
		//Individual agents can define event handling that overrides the registered behaviors above
		sim.registerCustomEventHandler("Exposed2", Vector2DEvent.class, AgentBehaviorProvider::handleVectorEvent_EXPOSED_custom);
		
		
		System.out.println("Examples of agents using explicitly registered handlers");
		sim.sendEvent(new ValueEvent("age", 37), "Exposed1");		
		sim.sendEvent(new ValueEvent("age", 37), "Infected1");
		sim.sendEvent(new ValueEvent("weight", 190), "Exposed2");
		sim.sendEvent(new MessageEvent("Bill","Alice","hi"), "Recoved1");
		sim.sendEvent(new ValueEvent("age", 37), "Infected2");
		sim.sendEvent(new Vector2DEvent(2.3,5.6), "Exposed1");
		
		System.out.println();
		System.out.println("Examples of agents using implicitly registered handlers");
		sim.sendEvent(new ExtendedMessageEvent("George","Time","here is the extended message","augmented data"), "Recoved1");
		sim.sendEvent(new Vector3DEvent(1.0,2.0,3.0), "Exposed2");
		
		System.out.println();
		System.out.println("Example of an agent using custom registered handlers");
		sim.sendEvent(new Vector2DEvent(4.5,20.7), "Exposed2");
	}
	
}
