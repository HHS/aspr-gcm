package gcm.manual.newagent.events;

public class MessageEvent implements Event{
	private final String sender;
	private final String reciver;
	private final String message;
	public MessageEvent(String sender, String reciver, String message) {
		super();
		this.sender = sender;
		this.reciver = reciver;
		this.message = message;
	}
	public String getSender() {
		return sender;
	}
	public String getReciver() {
		return reciver;
	}
	public String getMessage() {
		return message;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MessageEvent [sender=");
		builder.append(sender);
		builder.append(", reciver=");
		builder.append(reciver);
		builder.append(", message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}
	
}
