package kernel;

import java.util.LinkedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Measurement {

	private String commandID;
	private String fullCommand;
	
	private ObservableList<MeasurementParameter> setPointParams;
	private ObservableList<MeasurementParameter> resultParams;
	
	private LinkedList<MeasurementCurve> measurementCurvesCurrent; 
	private LinkedList<MeasurementCurve> measurementCurvesVoltage;
	
	
	public Measurement(String cmdID) {
		measurementCurvesCurrent = new LinkedList<>();
		measurementCurvesVoltage = new LinkedList<>();
		commandID = cmdID;
		fullCommand = "";
		resultParams = FXCollections.observableArrayList();
		setPointParams = FXCollections.observableArrayList();
	}
	
	public void setCmdFull(String fullCmd) {
		fullCommand = fullCmd;
	}
	
	public String getCmdFull() {
		return fullCommand;
	}
	
	public String getCmdID() {
		return commandID;
	}
	
	public void addMeasurementCurveCurrent(MeasurementCurve newCurve) {
		measurementCurvesCurrent.add(newCurve);
	}
	
	public void clearMeasurementCurvesCurrent() {
		measurementCurvesCurrent.clear();
	}
	
	public void addMeasurementCurveVotlage(MeasurementCurve newCurve) {
		measurementCurvesVoltage.add(newCurve);
	}
	
	public void clearMeasurementCurvesVoltage() {
		measurementCurvesVoltage.clear();
	}
	
	public void setMeasurementCurvesCurrent(LinkedList<MeasurementCurve> curves) {
		measurementCurvesCurrent = curves;
	}
	
	public void setMeasurementCurvesVoltage(LinkedList<MeasurementCurve> curves) {
		measurementCurvesVoltage = curves;
	}
	
	public LinkedList<MeasurementCurve> getCurvesCurrent() {
		return measurementCurvesCurrent;
	}
	
	public LinkedList<MeasurementCurve> getCurvesVoltage() {
		return measurementCurvesVoltage;
	}
	
	public ObservableList<MeasurementParameter> getSetPointParams() {
		return setPointParams;
	}
	
	public ObservableList<MeasurementParameter> getResultParams() {
		return resultParams;
	}
	
	public void addSetPointParameter(MeasurementParameter newParam) {
		setPointParams.add(newParam);
	}
	
	public void addResultParam(MeasurementParameter newParam) {
		resultParams.add(newParam);
	}
}
