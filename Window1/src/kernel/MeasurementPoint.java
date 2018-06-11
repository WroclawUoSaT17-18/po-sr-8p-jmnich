package kernel;

public class MeasurementPoint {

	private String x;
	private String y;
	
	public MeasurementPoint(String X, String Y) {
		x = X;
		y = Y;
	}
	
	public String getX() {
		return x;
	}
	
	public String getY() {
		return y;
	}
	
	public void setY(String Y) {
		y = Y;
	}
}
