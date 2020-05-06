package gcm.test.manual.demo.components;

import gcm.components.AbstractComponent;
import gcm.scenario.GlobalComponentId;
import gcm.simulation.Environment;

public class DeadCompartment extends AbstractComponent {

	private static class DeadGlobalComponentId implements GlobalComponentId {
		private final int id;

		private DeadGlobalComponentId(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("DeadGlobalComponentId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	@Override
	public void init(Environment environment) {
		for (int i = 0; i < 10; i++) {
			environment.addGlobalComponent(new DeadGlobalComponentId(i), DeadCompartmentHelper.class);
		}

	}

	@Override
	public void close(Environment environment) {

	}

}
