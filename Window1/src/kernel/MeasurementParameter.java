package kernel;

import javafx.beans.property.SimpleStringProperty;

public class MeasurementParameter {
	private SimpleStringProperty paramID;
	private SimpleStringProperty paramValue;
	
	public MeasurementParameter(String paramName, String paramVal) {
		paramID = new SimpleStringProperty(paramName);
		paramValue = new SimpleStringProperty(paramVal);
	}
	
	public String getParamID() {
		return paramID.getValue();
	}
	
	public String getParamValue() {
		return paramValue.getValue();
	}
	
	public void setParamID(String newID) {
		paramID.setValue(newID);
	}
	
	public void setParamValue(String newValue) {
		paramValue.setValue(newValue);
	}
}
