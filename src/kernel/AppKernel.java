package kernel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import java.util.prefs.*;

import dispatcher.CommandDispatcher;
import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import kernel.AppComponentsBank;
import kernel.CommunicationManager.ConnectResult;
import kernel.CommunicationManager.DisconnectResult;

public class AppKernel {
	
	private CommunicationManager communicationManager = null;
	private CommandDispatcher dispatcher = null;
	private MaintenanceCommandsHandler maintenanceCommandsHandler = null;
	private SingleMeasurementCommandsHandler singleMeasurementCommandsHandler = null;
	
	private Preferences userRoot;
	private Preferences appRegistryNode;
	
	public AppKernel() {
		communicationManager = new CommunicationManager();
		dispatcher = new CommandDispatcher();
		
		maintenanceCommandsHandler = new MaintenanceCommandsHandler(dispatcher);
		singleMeasurementCommandsHandler = new SingleMeasurementCommandsHandler(dispatcher);
	
		userRoot = Preferences.userRoot();
		appRegistryNode = userRoot.node("sensdx/datawrench_v1.0");	
	}	
	
	public void switchScene(Scenes scene) {
		
		switch(scene) {
			case SCENE_COMMUNICATION: {
				AppComponentsBank.getMainSceneController().switchContext(AppComponentsBank.getPaneCommunication());
				break;
			}
			case SCENE_CURRENT_MEAS: {
				AppComponentsBank.getMainSceneController().switchContext(AppComponentsBank.getPaneCurrentMeas());
				break;
			}
			case SCENE_IMPEDANCE_CURVE: {
				AppComponentsBank.getMainSceneController().switchContext(AppComponentsBank.getPaneImpCurve());
				break;
			}
			case SCENE_IMPEDANCE_SINGLE: {
				AppComponentsBank.getMainSceneController().switchContext(AppComponentsBank.getPaneImpSingle());
				break;
			}
			case SCENE_MAINTENANCE: {
				AppComponentsBank.getMainSceneController().switchContext(AppComponentsBank.getPaneMaintenance());
				break;
			}
			default: {
				AppComponentsBank.getMainSceneController().switchContext(AppComponentsBank.getPaneCommunication());
				break;
			}			
		}
		
		AppComponentsBank.getMainStage().show();
	}
	
	public String getCommandNumber2() {
		return appRegistryNode.get("command_2", "2 0");
	}
	
	public void setCommandNumber2(String newCmd) {
		appRegistryNode.put("command_2", newCmd);
	}
	
	public String getCommandNumber1() {
		return appRegistryNode.get("command_1", "0 0");
	}
	
	public void setCommandNumber1(String newCmd) {
		appRegistryNode.put("command_1", newCmd);
	}
	
	public String getDefaultParametersLocation() {
		return appRegistryNode.get("parameters_base_path", "");
	}
	
	public void setDefaultParametersLocation(String parametersDatabasePath) {
		appRegistryNode.put("parameters_base_path", parametersDatabasePath);
	}
	
	public String getDefaultCommunicationDumpLocation() {
		return appRegistryNode.get("comm_dump_path", "");
	}
	
	public void setDefaultCommunicationDumpLocation(String communicationDumpLocation) {
		appRegistryNode.put("comm_dump_path", communicationDumpLocation);
	}
	
	public String getDefaultSingleImpedanceDataDumpLocation() {
		return appRegistryNode.get("single_impedance_data_path", "");
	}
	
	public void setDefaultSingleImpedanceDataDumpLocation(String singleImpedanceDataDumpLocation) {
		appRegistryNode.put("single_impedance_data_path", singleImpedanceDataDumpLocation);
	}
	 
	public void lockNonCommunicationInterface() {
		AppComponentsBank.getControllerCommunication().lockNonCommunicationInterface();
		this.switchScene(Scenes.SCENE_COMMUNICATION);
		AppComponentsBank.getControllerContextButtons().lockButtons();
		
	}
	
	public void unlockNonCommunicationInterface() {
		AppComponentsBank.getControllerCommunication().unlockNonCommunicationInterface();
		AppComponentsBank.getControllerContextButtons().unlockButtons();
	}
	
	public String getCurrentTimeStamp() {
	    return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
	}
	
	public CommandDispatcher getCommandDispatcher() {
		return dispatcher;
	}
	
	public ConnectResult requestConnect(String COM) {		
		CommunicationManager.ConnectResult result = communicationManager.connectToCOM(COM);
		
		return result;
	}
	
	public DisconnectResult requestDisconnect() {
		CommunicationManager.DisconnectResult result = communicationManager.disconnectFromCOM();
		
		return result;
	}
	
	public LinkedList<String> requestAvailableCOMs() {
		return communicationManager.getComList();
	}
	
	public void unknownCommandHandler(String unknownCommand) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				String id = unknownCommand.split(" ")[0];

				AppComponentsBank.getControllerCommunication().addToRxLog("<system> --- Unknown command ID:[" + id + "] ---");
			}
		});
	}
		
	
	public void invalidDataHandler(String data) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				AppComponentsBank.getControllerCommunication().addToRxLog("<system> --- Invalid data received DATA:[" + data + "] ---");				
			}
		});
	}
	
	public void requestCommandTransmission(String command) {
		communicationManager.sendString(command + "\r\n");
	}
}
