package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import kernel.AppComponentsBank;

public class ControllerNewMeasPopup {
	
	@FXML TextField fxMeasNameField;
	
	@FXML Button fxSaveBtn;
	@FXML Button fxRejectBtn;
	
	@FXML
	public void fxSavePressed(ActionEvent event) {
		
		String name = fxMeasNameField.getText();
		
		if(name.equals("") || name == null) {
			name = "noname_measurement";
		}
		
		AppComponentsBank.getControllerImpSingle().saveCurrentMeasurementToWorkspace(name);
		AppComponentsBank.getControllerImpSingle().clearGUIForNewMeasurement();
		
		Node src = (Node) event.getSource();
		src.getScene().getWindow().hide();
		fxMeasNameField.setText("");
		
	}
	
	@FXML
	public void fxRejectPressed(ActionEvent event) {
		
		AppComponentsBank.getControllerImpSingle().clearGUIForNewMeasurement();
		
		Node src = (Node) event.getSource();
		src.getScene().getWindow().hide();
		fxMeasNameField.setText("");
	}

}
