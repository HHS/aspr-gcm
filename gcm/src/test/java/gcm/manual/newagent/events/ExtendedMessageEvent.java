package gcm.manual.newagent.events;

public class ExtendedMessageEvent extends MessageEvent{
	private String augment;
	public ExtendedMessageEvent(String sender, String reciver, String message, String augment) {
		super(sender,reciver,message);
		this.augment = augment;
	}
	public String getAugment() {
		return augment;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExtendedMessageEvent [augment=");
		builder.append(augment);
		builder.append(", getSender()=");
		builder.append(getSender());
		builder.append(", getReciver()=");
		builder.append(getReciver());
		builder.append(", getMessage()=");
		builder.append(getMessage());
		builder.append("]");
		return builder.toString();
	}
	
	
	
		
}
