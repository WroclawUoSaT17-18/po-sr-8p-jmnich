package controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import kernel.AppComponentsBank;
import kernel.CMD;

public class ControllerMaintenance {
	
	@FXML private Button fxRefreshAllBtn;
	@FXML private Button fxRunDiagnosticsBtn;
	@FXML private Button fxI2C3BusResetBtn;
	
	@FXML private AnchorPane fxLeftSplit;
	@FXML private AnchorPane fxRightSplit;
	
	@FXML private TableView<DeviceStatusEntry> fxPropertyTableView;
	@FXML private TableView<DeviceStatusEntry> fxDiagnosticTableView;

	@FXML private TextArea fxInfoTextArea; 
	
	private ObservableList<DeviceStatusEntry> propslist;
	private ObservableList<DeviceStatusEntry> diaglist;
	
	public class DeviceStatusEntry {		
		private SimpleStringProperty propertyId;
		private SimpleStringProperty propertyValue;
		
		public DeviceStatusEntry(String propertyName, String propertyVal) {
			propertyId = new SimpleStringProperty(propertyName);
			propertyValue = new SimpleStringProperty(propertyVal);
		}
		
		public String getPropertyValue() {
			return propertyValue.get();
		}
		
		public void setPropertyValue(String newValue) {
			propertyValue.set(newValue);
		}
		
		public String getPropertyId() {
			return propertyId.get();
		}
	}
	
	public ControllerMaintenance() {
		AppComponentsBank.setControllerMaintenance(this);
	}
	
	@SuppressWarnings("unchecked")
	@FXML
	public void initialize() {
		// property table
		propslist = FXCollections.observableArrayList();
		
		TableColumn<DeviceStatusEntry, String> propCol = 
				new TableColumn<DeviceStatusEntry, String>("Property");
		TableColumn<DeviceStatusEntry, String> valCol = 
				new TableColumn<DeviceStatusEntry, String>("Value");
		
		valCol.setCellValueFactory(new PropertyValueFactory<DeviceStatusEntry, String>("propertyValue"));
		propCol.setCellValueFactory(new PropertyValueFactory<DeviceStatusEntry, String>("propertyId"));
	
		propCol.setPrefWidth(200);
		propCol.setMinWidth(200);
		propCol.setMaxWidth(200);
		
		valCol.setPrefWidth(600);
		valCol.setMinWidth(600);
		valCol.setMaxWidth(600);
		
		propslist.add(new DeviceStatusEntry("Device Type", ">REFRESH<"));
		propslist.add(new DeviceStatusEntry("Device ID", ">REFRESH<"));
		propslist.add(new DeviceStatusEntry("Firmware version", ">REFRESH<"));
		propslist.add(new DeviceStatusEntry("Battery state", ">REFRESH<"));
		propslist.add(new DeviceStatusEntry("Free heap space", ">REFRESH<"));
		propslist.add(new DeviceStatusEntry("Minimal ever free heap space", ">REFRESH<"));
		propslist.add(new DeviceStatusEntry("Error count in the internal device log", ">REFRESH<"));
		
		fxPropertyTableView.setItems(propslist);
		
		fxPropertyTableView.getColumns().setAll(propCol, valCol);
		
		// diagnostic table
		diaglist = FXCollections.observableArrayList();
		
		TableColumn<DeviceStatusEntry, String> modCol = 
				new TableColumn<DeviceStatusEntry, String>("Module");
		TableColumn<DeviceStatusEntry, String> statCol = 
				new TableColumn<DeviceStatusEntry, String>("Status");
		
		statCol.setCellValueFactory(new PropertyValueFactory<DeviceStatusEntry, String>("propertyValue"));
		modCol.setCellValueFactory(new PropertyValueFactory<DeviceStatusEntry, String>("propertyId"));
		
		statCol.setMinWidth(150);
		statCol.setPrefWidth(150);
		statCol.setMaxWidth(150);
		
		modCol.setMinWidth(205);
		modCol.setPrefWidth(205);
		modCol.setMaxWidth(205);
		
		diaglist.add(new DeviceStatusEntry(">TODO<", ">REFRESH<"));
		
		fxDiagnosticTableView.setItems(diaglist);
		
		fxDiagnosticTableView.getColumns().setAll(modCol, statCol);
		
		addToInfoWindow("Maintenance ready");
	}

	
	public void resetDeviceProperties() {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				fxPropertyTableView.getItems().clear();
			}
		});	
	}
	
	public void resetDeviceDiagnostics() {
		// TODO
	}
	
	public void addToInfoWindow(String newMessage) {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {
				fxInfoTextArea.appendText(">> " + newMessage + "\r\n");				
			}
		});		
	}
	
	public void refreshProperty(String property, String value) {
		
		Platform.runLater(new Runnable() {
			
			@Override
			public void run() {

				boolean propertyPresent = false;
				
				for(DeviceStatusEntry stat : fxPropertyTableView.getItems()) {
					if(stat.propertyId.get().equals(property)) {
						stat.setPropertyValue(value);
						propertyPresent = true;
					}
				}
				
				if(propertyPresent == false) {
					propslist.add(new DeviceStatusEntry(property, value));
				}						
				
				// force refresh
				fxPropertyTableView.getColumns().get(0).setVisible(false);
				fxPropertyTableView.getColumns().get(0).setVisible(true);
				fxPropertyTableView.getColumns().get(1).setVisible(false);
				fxPropertyTableView.getColumns().get(1).setVisible(true);
			}
		});		
	}
	
	// GUI HANDLERS
	
	public void btnRefreshAll(ActionEvent event) {
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					AppComponentsBank.getAppKernel().requestCommandTransmission(String.valueOf(CMD.messageHostFREE_HEAP_SIZE_REQUEST) + " 0");
					Thread.sleep(100);
					AppComponentsBank.getAppKernel().requestCommandTransmission(String.valueOf(CMD.messageHostMINIMAL_EVER_FREE_HEAP_SIZE_REQUEST) + " 0");
						
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		th.start();
	}
	
	public void btnRunDiagnostics(ActionEvent event) {
		// TODO
	}
	
	public void btnResetI2C3(ActionEvent event) {
		AppComponentsBank.getAppKernel().requestCommandTransmission(String.valueOf(CMD.messageHostPERFORM_I2C_RESET_SEQUENCE) + " 0");
		addToInfoWindow("I2C3 reset sequence done");
	}
}
