package kernel;
import java.util.*;

public class MeasurementCurve {

	String curveID;	
	LinkedList<MeasurementPoint> points;
	
	public MeasurementCurve(String id) {
		curveID = id;
		points = new LinkedList<MeasurementPoint>();
	}
	
	public void setCurve(LinkedList<MeasurementPoint> curve) {
		points = curve;
	}
	
	public LinkedList<MeasurementPoint> getCurve() {
		return points;
	}
	
	public String getId() {
		return curveID;
	}
	
	public void addPointToCurve(MeasurementPoint newPoint) {
		points.add(newPoint);
	}
	
	
}
