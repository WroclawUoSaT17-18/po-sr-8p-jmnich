package adapter;

public class Adapter implements Target{

	private Adaptee adaptee;
	
	public Adapter() {
		adaptee = new Adaptee();
	}
	
	@Override
	public void Request() {
		
		adaptee.incompatibleMethod("It is compatible now");
		
	}

}
