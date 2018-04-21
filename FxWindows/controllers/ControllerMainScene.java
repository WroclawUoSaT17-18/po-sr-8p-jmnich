package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Paint;
import javafx.scene.control.ComboBox;
import javafx.scene.shape.*;

import kernel.AppComponentsBank;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.scene.layout.*;

public class ControllerMainScene {
	
	@FXML AnchorPane fxSplitLeft;
	@FXML AnchorPane fxSplitRight;

	@FXML 
	public void initialize() {		
		AppComponentsBank.setMainSceneController(this);
	
		fxSplitLeft.getChildren().add(AppComponentsBank.getContextButtonsPane());
		AnchorPane.setRightAnchor(AppComponentsBank.getContextButtonsPane(), 2.0);		
	}
	
	public void switchContext(Pane newPane) {
		
		fxSplitRight.getChildren().clear();			
		fxSplitRight.getChildren().add(newPane);
		
		AnchorPane.setLeftAnchor(newPane, 2.0);
	}
}
