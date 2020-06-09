package gcm.manual.ppp;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import gcm.util.TimeElapser;

public class Exchange {

	private static class MemberStatus {
		private boolean readyForTermination;
		private boolean readyForProgress;
	}
	
	private TimeElapser timeElapser = new TimeElapser();
	
	public synchronized double getTime() {
		return timeElapser.getElapsedMilliSeconds();
	}
	
	private void report(Object message) {
		//System.out.println("Exchange "+message);
	}
	
	private boolean terminationAchieved;
	
	private Map<Object, MemberStatus> memberIds = new LinkedHashMap<>();

	private Map<Object, EventPackage.Builder> inboundMap = new LinkedHashMap<>();

	private Map<Object, EventPackage> outboundMap = new LinkedHashMap<>();

	public Exchange(Set<? extends Object> memberIds) {
		for (Object memberId : memberIds) {
			report("establishing member "+memberId);
			MemberStatus memberStatus = new MemberStatus();
			this.memberIds.put(memberId, memberStatus);
			inboundMap.put(memberId,EventPackage.builder());
		}
	}
	
	public synchronized boolean terminationAchieved() {
		return terminationAchieved;
	}
	
	public synchronized Set<Object> getMemberIds(){
		return new LinkedHashSet<>(memberIds.keySet());
	}

	public synchronized void signalReadyForProgress(Object memberId, boolean readyForTermination) {
		report("Member "+memberId+" has signaled ready for progress");
		MemberStatus memberStatus = memberIds.get(memberId);
		memberStatus.readyForProgress = true;
		memberStatus.readyForTermination = readyForTermination;
		
		for(Object memId : memberIds.keySet()) {
			memberStatus = memberIds.get(memId);
			if(!memberStatus.readyForProgress) {
				return;
			}
		}
		report("All members agree to progress");
		for(Object memId : memberIds.keySet()) {
			memberStatus = memberIds.get(memId);
			memberStatus.readyForProgress = false;
		}
		
		outboundMap.clear();
		boolean outboundEventsExist = false;
		for(Object memId : memberIds.keySet()) {
			EventPackage eventPackage = inboundMap.get(memId).build();
			outboundEventsExist |= !eventPackage.isEmpty();
			report("Placing event package for "+memId+" containing "+eventPackage.getEvents().size()+" events onto outbound map");
			outboundMap.put(memId, eventPackage);			
		}
		if(outboundEventsExist) {
			return;
		}
		for(Object memId : memberIds.keySet()) {
			if(!memberIds.get(memId).readyForTermination) {
				return;
			}
		}
		terminationAchieved = true;
	}
	
	public synchronized void putEvent(Object memberId, Object event) {
		report(memberId.toString()+" has recieved event: "+event);
		inboundMap.get(memberId).addEvent(event);
	}

	public synchronized Optional<EventPackage> getEvents(Object memberId) {
		EventPackage eventPackage = outboundMap.remove(memberId);
		
		if(eventPackage == null) {
			report("Member "+memberId+" is getting a null package");
		}else {
			report("Member "+memberId+" is getting a package containing "+eventPackage.getEvents().size()+" events");
		}
		return Optional.ofNullable(eventPackage);
	}
}
