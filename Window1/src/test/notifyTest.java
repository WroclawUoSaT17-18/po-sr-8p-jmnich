package test;

import junit.framework.TestCase;
import junit.framework.TestFailure;

public class notifyTest extends TestCase {
	
	
	public void testNotufy() throws Exception {
//		fail();

		String command = "106 42";
//		String falseCmd = "106 41";
		
		dispatcher.CommandDispatcher disp = new dispatcher.CommandDispatcher();
		
		dispatcher.CmdListener listener = new dispatcher.CmdListener() {
			
			@Override
			public void passCommand(String cmd) {
				
				System.out.println("Notification arrived : " + cmd);				
				
				if(!cmd.equals(command)) {
					fail();
				}
				
			}
		};
		
		disp.subscribe(106, listener);
		
		disp.notify(command);
		
	}

}
