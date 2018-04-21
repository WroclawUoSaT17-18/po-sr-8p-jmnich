package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import kernel.AppComponentsBank;
import kernel.Scenes;

public class ControllerContextButtons {
	
	@FXML ToggleButton btnComm;
	@FXML ToggleButton btnMaintenance;
	@FXML ToggleButton btnCurrentMeas;
	@FXML ToggleButton btnImpSingle;
	@FXML ToggleButton btnImpCurve;
	
	public ControllerContextButtons() {
		AppComponentsBank.setControllerContextButtons(this);
	}
	
	@FXML
	public void initialize() {
		
	}
	
	public void btnCommunication(ActionEvent event) {
		btnComm.setSelected(true);
		btnMaintenance.setSelected(false);
		btnCurrentMeas.setSelected(false);
		btnImpSingle.setSelected(false);
		btnImpCurve.setSelected(false);
		
		AppComponentsBank.getAppKernel().switchScene(Scenes.SCENE_COMMUNICATION);
	}
	
	public void btnMaintenance(ActionEvent event) {
		btnComm.setSelected(false);
		btnMaintenance.setSelected(true);
		btnCurrentMeas.setSelected(false);
		btnImpSingle.setSelected(false);
		btnImpCurve.setSelected(false);

		AppComponentsBank.getAppKernel().switchScene(Scenes.SCENE_MAINTENANCE);
	}
	
	public void btnImpCurve(ActionEvent event) {
		btnComm.setSelected(false);
		btnMaintenance.setSelected(false);
		btnCurrentMeas.setSelected(false);
		btnImpSingle.setSelected(false);
		btnImpCurve.setSelected(true);

		AppComponentsBank.getAppKernel().switchScene(Scenes.SCENE_IMPEDANCE_CURVE);
	}
	
	public void btnImpSingle(ActionEvent event) {
		btnComm.setSelected(false);
		btnMaintenance.setSelected(false);
		btnCurrentMeas.setSelected(false);
		btnImpSingle.setSelected(true);
		btnImpCurve.setSelected(false);

		AppComponentsBank.getAppKernel().switchScene(Scenes.SCENE_IMPEDANCE_SINGLE);
	}
	
	public void btnCurrentMeas(ActionEvent event) {
		btnComm.setSelected(false);
		btnMaintenance.setSelected(false);
		btnCurrentMeas.setSelected(true);
		btnImpSingle.setSelected(false);
		btnImpCurve.setSelected(false);
		
		AppComponentsBank.getAppKernel().switchScene(Scenes.SCENE_CURRENT_MEAS);
	}
	
	public void lockButtons() {
		btnComm.setDisable(true);
		btnMaintenance.setDisable(true);
		btnCurrentMeas.setDisable(true);
		btnImpSingle.setDisable(true);
		btnImpCurve.setDisable(true);
	}
	
	public void unlockButtons() {
		btnComm.setDisable(false);
		btnMaintenance.setDisable(false);
		btnCurrentMeas.setDisable(false);
		btnImpSingle.setDisable(false);
		btnImpCurve.setDisable(false);
	}

}
