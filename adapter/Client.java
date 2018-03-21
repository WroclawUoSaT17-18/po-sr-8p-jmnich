package adapter;

public class Client {

	public void runClient(Target aTarget) {
		
		try {
			Thread.sleep(500);
			aTarget.Request();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
