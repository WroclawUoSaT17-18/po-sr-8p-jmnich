package adapter;

public class Main {

	public static void main(String[] args) {
		
		Target target = new Adapter();
		
		Client client = new Client();
		
		while(true) {
			client.runClient(target);
		}

	}

}
