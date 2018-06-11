package dispatcher;

import dispatcher.CmdListener;
import kernel.AppComponentsBank;

import java.util.*;

public class CommandDispatcher {
	
	private class Subscription {
		public int identifier;
		
		private LinkedList<CmdListener> listeners;
		
		public Subscription(int id) {
			identifier = id;
			listeners = new LinkedList<CmdListener>();
		}
		
		public LinkedList<CmdListener> getListeners() {
			return listeners;
		}
		
		public void addListener(CmdListener newListener) {
			listeners.add(newListener);
		}
	}
	
	private LinkedList<Subscription> subscriptions;
	
	public CommandDispatcher() {
		subscriptions = new LinkedList<Subscription>();
	}
	
	public void notify(String command) {
		int id = Integer.valueOf(command.split(" ")[0]);
		
		Subscription sub = getSubscription(id);
		
		if(sub == null) {
			AppComponentsBank.getAppKernel().unknownCommandHandler(command);
		} else {
			for(CmdListener ls : sub.getListeners()) {
				ls.passCommand(command);
			}
		}
	}
	 
	public void subscribe(int cmdIdentifier, CmdListener listener) {
		Subscription sub = getSubscription(cmdIdentifier);
		
		if(sub == null) {
			sub = new Subscription(cmdIdentifier);
			subscriptions.add(sub);
			sub.addListener(listener);
		} else {
			sub.addListener(listener);
		}
	}
	
	private Subscription getSubscription(int id) {
		
		if(subscriptions.size() > 0) {
			for(Subscription s : subscriptions) {
				if(s.identifier == id) {
					return s;
				}
			}
		} 
		
		return null;
	}
}
