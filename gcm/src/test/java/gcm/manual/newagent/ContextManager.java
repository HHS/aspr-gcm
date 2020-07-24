package gcm.manual.newagent;

public class ContextManager {
	
	public final class Context{
		private Context() {}
		public Object getAgentId() {
			return agentId;
		}
	}
	private Context context = new Context();
	public Context getContext() {
		return context;
	}
	
	private Object agentId;
	
	public void setAgentId(Object agentId) {
		this.agentId = agentId;
	}
	
}
