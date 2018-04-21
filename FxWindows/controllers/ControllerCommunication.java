package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.paint.Paint;
import javafx.scene.control.ComboBox;
import javafx.scene.shape.*;
import javafx.stage.DirectoryChooser;
import kernel.AppComponentsBank;
import kernel.CommunicationManager;
import kernel.CommunicationManager.ConnectResult;
import kernel.CommunicationManager.DisconnectResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.scene.layout.*;


public class ControllerCommunication {
	
	@FXML Button fxConnectButton;
	@FXML ComboBox<String> fxPortComboBox;
	@FXML Label fxInfoLabel;
	@FXML ListView<String> fxTxLogListView;
	@FXML ListView<String> fxRxLogListView;
	@FXML Button fxClearTxLogButton;
	@FXML TextField fxCommand1Field;
	@FXML TextField fxCommand2Field;
	@FXML Button fxRetransmitSelectedButton;
	@FXML Button fxSendCommand1Button;
	@FXML Button fxSendCommand2Button;
	@FXML Button fxClearRxLogButton;
	@FXML TextField fxLogFileNameTextField;
	@FXML TextField fxLogDirectoryPath;
	@FXML Button fxSaveSelectedToFileButton;
	@FXML Button fxSelectLogDirectoryButton;
		
	boolean connectedToDevice = false;
	
	public ControllerCommunication() {
		AppComponentsBank.setControllerCommunication(this);
	}
	 
	@FXML 
	public void initialize() {		
	
		// setup a thread for refreshing available COM ports
		Thread th = new Thread(new Runnable() {			
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if(connectedToDevice == false) {
						LinkedList<String> comList = AppComponentsBank.getAppKernel().requestAvailableCOMs();
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								try {
									for(String s : comList) {
										if (fxPortComboBox.getItems().contains(s) == false) {
											fxPortComboBox.getItems().add(s);
										}
									}	

									for(String s : fxPortComboBox.getItems()) {
										if(comList.contains(s) == false) {
											fxPortComboBox.getItems().remove(s);
										}
									}
								} catch(Exception e) {
									// ignore
								}
							}
						});
					}				
				}
			}
		});
		th.start();		
		
		fxRxLogListView.getItems().add("<system> --- LOG READY ---");
		fxTxLogListView.getItems().add("<system> --- LOG READY ---");
		
		fxLogDirectoryPath.setText(AppComponentsBank.getAppKernel().getDefaultCommunicationDumpLocation());
		
		if(fxLogDirectoryPath.getText().equals("")) {
			fxSaveSelectedToFileButton.setDisable(true);
		}
		
		fxCommand1Field.setText(AppComponentsBank.getAppKernel().getCommandNumber1());
		fxCommand2Field.setText(AppComponentsBank.getAppKernel().getCommandNumber2());
	}
	
	public void setInfoLabel(String newInfo) {
		fxInfoLabel.setText(newInfo);
	}
	
	public void addToRxLog(String rxData) {
		String rxDataProcessed = rxData.replaceAll("\r\n", "");	
		
		Platform.runLater(new Runnable() {			
			@Override
			public void run() {
				fxRxLogListView.getItems().add(rxDataProcessed);
				
				int size = fxRxLogListView.getItems().size();
				fxRxLogListView.scrollTo(size - 1);
			}
		});
	}
	
	public void addToTxLog(String txData) {
		String txDataProcessed = txData.replaceAll("\r\n", "");	
		
		Platform.runLater(new Runnable() {			
			@Override
			public void run() {
				fxTxLogListView.getItems().add(txDataProcessed);
				
				int size = fxTxLogListView.getItems().size();
				fxTxLogListView.scrollTo(size - 1);
			}
		});
	}
	
	public void lockNonCommunicationInterface() {
		fxRetransmitSelectedButton.setDisable(true);
		fxSendCommand1Button.setDisable(true);
		fxSendCommand2Button.setDisable(true);
	}
	
	public void unlockNonCommunicationInterface() {
		fxRetransmitSelectedButton.setDisable(false);
		fxSendCommand1Button.setDisable(false);
		fxSendCommand2Button.setDisable(false);
	}
	
	// GUI handlers
	
	public void btnConnect(ActionEvent event) {
		if(connectedToDevice == false) {
			CommunicationManager.ConnectResult result = AppComponentsBank.getAppKernel().requestConnect(fxPortComboBox.getValue());
			
			if(result == ConnectResult.CONNECT_ERROR) {
				setInfoLabel("Connect failed");
				connectedToDevice = false;
			} else {
				setInfoLabel("Connected");
				connectedToDevice = true;
				fxConnectButton.setText("DISCONNECT");
				addToTxLog("<system> --- CONNECTED TO " + fxPortComboBox.getValue() + " ---");
				AppComponentsBank.getAppKernel().unlockNonCommunicationInterface();
			}	
		} else {
			CommunicationManager.DisconnectResult result = AppComponentsBank.getAppKernel().requestDisconnect();
			
			if(result == DisconnectResult.DISCONNECT_ERROR) {
				setInfoLabel("Disconnect annomaly - restart application");
				fxConnectButton.setDisable(true);
				fxConnectButton.setText("<ERROR>");
				connectedToDevice = false;
				AppComponentsBank.getAppKernel().lockNonCommunicationInterface();
			} else {
				setInfoLabel("Disconnected");
				connectedToDevice = false;
				fxConnectButton.setText("CONNECT");
				addToTxLog("<system> --- DISCONNECTED ---");
				AppComponentsBank.getAppKernel().lockNonCommunicationInterface();
			}	
		}		
	}
	
	public void btnSendCommand1(ActionEvent event) {
		String command = fxCommand1Field.getText();
		if(command != null && command.matches("[0-9\\s]+") && !command.equals("")) {
			AppComponentsBank.getAppKernel().requestCommandTransmission(command);
			AppComponentsBank.getAppKernel().setCommandNumber1(command);
		} else {
			fxTxLogListView.getItems().add("<system> --- TX ERROR - wrong sign in command ---");
		}		
	}
	
	public void btnSendCommand2(ActionEvent event) {
		String command = fxCommand2Field.getText();
		if(command != null && command.matches("[0-9\\s]+") && !command.equals("")) {
			AppComponentsBank.getAppKernel().requestCommandTransmission(command);
			AppComponentsBank.getAppKernel().setCommandNumber2(command);
		} else {
			fxTxLogListView.getItems().add("<system> --- TX ERROR - wrong sign in command ---");
		}		
	}
	
	public void btnRetransmitSelected(ActionEvent event) {
		String command = fxTxLogListView.getSelectionModel().getSelectedItem();
		
		if(command != null && command.matches("[0-9\\s]+")) {
			AppComponentsBank.getAppKernel().requestCommandTransmission(command);
		} else {
			fxTxLogListView.getItems().add("<system> --- TX ERROR - wrong sign in command ---");
		}
	}
	
	public void btnClearTxLog(ActionEvent event) {
		fxTxLogListView.getItems().clear();
		fxTxLogListView.getItems().add("<system> --- LOG CLEARED ---");
	}
	
	public void btnClearRxLog(ActionEvent event) {
		fxRxLogListView.getItems().clear();
		fxRxLogListView.getItems().add("<system> --- LOG CLEARED ---");
	}
	
	public void btnSaveToFile(ActionEvent event) {

		if(fxRxLogListView.getSelectionModel().getSelectedItem() == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Log error");
			alert.setHeaderText("Log dump to file failed!");
			alert.setContentText("No data selected for dump");

			alert.showAndWait();
			
			return;
		}
		
		String fileName = fxLogFileNameTextField.getText();

		if(fileName.equals("")) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Log file name");
			alert.setHeaderText("Target file name not specified");
			alert.setContentText("File name set to 'mobi_dat'");

			alert.showAndWait();  

			fileName = "mobi_dat";
		}
		fileName += ".dat";

		File targetFile = new File(fxLogDirectoryPath.getText(), fileName);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(targetFile));
			writer.write("MOBI DATA FILE - FROM DIRECT TRANSMISSION LOG | TIME STAMP:   " + 
					AppComponentsBank.getAppKernel().getCurrentTimeStamp() + "\r\n" +
					fxRxLogListView.getSelectionModel().getSelectedItem());

		} 
		catch ( IOException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Log error");
			alert.setHeaderText("Log dump to file failed!");
			alert.setContentText("Make sure you chose the correct path, file name and a vaild data for save");

			alert.showAndWait();
		} 
		finally {

			try {		    	
				if ( writer != null)
					writer.close( );
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Log done");
				alert.setHeaderText("Successful data dump");
				alert.setContentText("Your data is now available in the specified location");

				alert.showAndWait();
			} catch ( IOException e) {		    	
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Log error");
				alert.setHeaderText("Can not close the target file!");
				alert.setContentText("Restart the application. Your data might have been lost.");

				alert.showAndWait();
			}
		}	
	}
	
	public void btnSelectWorkingDirectory(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(AppComponentsBank.getMainStage());
		
		if(selectedDirectory != null) {
			fxLogDirectoryPath.setText(selectedDirectory.getAbsolutePath());
			AppComponentsBank.getAppKernel().setDefaultCommunicationDumpLocation(selectedDirectory.getAbsolutePath());
			fxSaveSelectedToFileButton.setDisable(false);
		}		
	}
}
