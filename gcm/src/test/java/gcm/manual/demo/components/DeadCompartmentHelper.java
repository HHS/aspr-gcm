package gcm.manual.demo.components;

import gcm.components.AbstractComponent;
import gcm.simulation.Environment;

public class DeadCompartmentHelper extends AbstractComponent {
	@Override
	public void init(Environment environment) {				
		System.out.println(environment.getCurrentComponentId().toString());
	}

	@Override
	public void close(Environment environment) {

	}

}
