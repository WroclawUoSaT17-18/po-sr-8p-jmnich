package kernel;


import controllers.ControllerCommunication;
import controllers.ControllerContextButtons;
import controllers.ControllerCurrentMeas;
import controllers.ControllerImpedanceCurve;
import controllers.ControllerMainScene;
import controllers.ControllerMaintenance;
import controllers.ControllerSingleImpedance;
import controllers.ControllerMainScene;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.*;

public class AppComponentsBank {
	private static ControllerCommunication ctrlCommunication = null;
	private static ControllerCurrentMeas ctrlCurrentMeas = null;
	private static ControllerImpedanceCurve ctrlImpCurve = null;
	private static ControllerSingleImpedance ctrlImpSingle = null;
	private static ControllerMaintenance ctrlMaintenance = null;
	private static ControllerContextButtons ctrlButtons = null;
	
	private static ControllerMainScene ctrlMainScene = null;
	
	private static Pane PaneCommunication = null;
	private static Pane PaneCurrentMeas = null;
	private static Pane PaneImpCurve = null;
	private static Pane PaneImpSingle = null;
	private static Pane PaneMaintenance = null;

	private static Stage stageMainStage = null;
	
	private static boolean globalErrorFlag = false;

	private static Pane paneContextButtons = null;
	
	private static AppKernel kernel = null;
	
	private static Scene mainScene = null;

// ===============================================================================
	// MAINSCENE
//================================================================================
	
	/**
	 * Sets the Main Scene reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param scene reference to an initialized Scene
	 */
	public static void setMainScene(Scene scene) {
		if(mainScene == null) {
			mainScene = scene;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the main scene
	 * @return reference to the static main scene
	 */
	public static Scene getMainScene() {
		return mainScene;
	}
	
	/**
	 * Sets the Main controller reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param controller reference to an initialized controller
	 */
	public static void setMainSceneController(ControllerMainScene controller) {
		if(ctrlMainScene == null) {
			ctrlMainScene = controller;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the main scene controller
	 * @return reference to the static main scene
	 */
	public static ControllerMainScene getMainSceneController() {
		return ctrlMainScene;
	}
	
	
// ===============================================================================
	// APPKERNEL
//================================================================================
	
	/**
	 * Sets the Application Kernel reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param buttonsPane reference to an initialized app kernel
	 */
	public static void setAppKernel(AppKernel appKernel) {
		if(kernel == null) {
			kernel = appKernel;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the AppKernel
	 * @return reference to the static kernel
	 */
	public static AppKernel getAppKernel() {
		return kernel;
	}
	
// ===============================================================================
	// CONTEXT BUTTONS PANE AND CONTROLLER
// ===============================================================================
	
	/**
	 * Sets a static reference to the Context Buttons window controller.
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param ctrl reference to an initialized controller
	 */
	public static void setControllerContextButtons(ControllerContextButtons ctrl) {
		if(ctrlButtons == null) {
			ctrlButtons = ctrl;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for Context Buttons window controller
	 * @return reference to the controller
	 */
	public static ControllerContextButtons getControllerContextButtons() {
		return ctrlButtons;
	}
	
	// ===============================================================================
	
	/**
	 * Sets the Context Buttons reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param buttonsPane reference to an initialized buttons pane
	 */
	public static void setContextButtonsPane(Pane buttonsPane) {
		if(paneContextButtons == null) {
			paneContextButtons = buttonsPane;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the ContextButtons Pane
	 * @return reference to the static contextButtons Pane
	 */
	public static Pane getContextButtonsPane() {
		return paneContextButtons;
	}	
	
// ===============================================================================
	// ERROR HANDLING
// ===============================================================================
	
	/**
	 * Sets the static GlobalErrorFlag
	 */
	public static void setGlobalErrorFlag() {
		globalErrorFlag = true;
	}
	
	// ===============================================================================
	
	/**
	 * Resets the static GlobalErrorFlag
	 */
	public static void resetGlobalErrorFlag() {
		globalErrorFlag = false;
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the GlobalErrorFlag
	 * @return GlobalErrorFlag state
	 */
	public static boolean getGlobalErrorFlag() {
		return globalErrorFlag;
	}
	
// ===============================================================================
	// STAGES
// ===============================================================================
	
	/**
	 * Sets the Main Stage reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param stage reference to an initialized stage
	 */
	public static void setMainStage(Stage stage) {
		if(stageMainStage == null) {
			stageMainStage = stage;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the Main Stage
	 * @return reference to the stage
	 */
	public static Stage getMainStage() {
		return stageMainStage;
	}
	
// ===============================================================================
	// PaneS
// ===============================================================================
	
	/**
	 * Sets the Communication Pane reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param Pane reference to an initialized Pane
	 */
	public static void setPaneCommunication(Pane Pane) {
		if(PaneCommunication == null) {
			PaneCommunication = Pane;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets the Current Measurement Pane reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param Pane reference to an initialized Pane
	 */
	public static void setPaneCurrentMeas(Pane Pane) {
		if(PaneCurrentMeas == null) {
			PaneCurrentMeas = Pane;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets the Impedance Curve Pane reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param Pane reference to an initialized Pane
	 */
	public static void setPaneImpCurve(Pane Pane) {
		if(PaneImpCurve == null) {
			PaneImpCurve = Pane;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets the Single Point impedance Pane reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param Pane reference to an initialized Pane
	 */
	public static void setPaneImpSingle(Pane Pane) {
		if(PaneImpSingle == null) {
			PaneImpSingle = Pane;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets the Maintenance Pane reference. 
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param Pane reference to an initialized Pane
	 */
	public static void setPaneMaintenance(Pane Pane) {
		if(PaneMaintenance == null) {
			PaneMaintenance = Pane;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the Communication Pane
	 * @return reference to the Pane
	 */
	public static Pane getPaneCommunication() {
		return PaneCommunication;
	}
	
	// ===============================================================================
	
	/**
	 * Getter for the Current Measurement Pane
	 * @return reference to the Pane
	 */
	public static Pane getPaneCurrentMeas() {
		return PaneCurrentMeas;
	}

	// ===============================================================================
	
	/**
	 * Getter for the Impedance Curve Pane
	 * @return reference to the Pane
	 */
	public static Pane getPaneImpCurve() {
		return PaneImpCurve;
	}

	// ===============================================================================
	
	/**
	 * Getter for the Single Point Impedance Pane
	 * @return reference to the Pane
	 */
	public static Pane getPaneImpSingle() {
		return PaneImpSingle;
	}

	// ===============================================================================
	
	/**
	 * Getter for the Maintenance Pane
	 * @return reference to the Pane
	 */
	public static Pane getPaneMaintenance() {
		return PaneMaintenance;
	}
	
// ===============================================================================
	// CONTROLLERS
// ===============================================================================
	
	/**
	 * Sets a static reference to the Communication window controller.
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param ctrl reference to an initialized controller
	 */
	public static void setControllerCommunication(ControllerCommunication ctrl) {
		if(ctrlCommunication == null) {
			ctrlCommunication = ctrl;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets a static reference to the CurrentMeas window controller.
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param ctrl reference to an initialized controller
	 */
	public static void setControllerCurrentMeas(ControllerCurrentMeas ctrl) {
		if(ctrlCurrentMeas == null) {
			ctrlCurrentMeas = ctrl;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets a static reference to the ImpedanceCurve window controller.
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param ctrl reference to an initialized controller
	 */
	public static void setControllerImpCurve(ControllerImpedanceCurve ctrl) {
		if(ctrlImpCurve == null) {
			ctrlImpCurve = ctrl;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets a static reference to the SinglePointImpedance window controller.
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param ctrl reference to an initialized controller
	 */
	public static void setControllerImpSingle(ControllerSingleImpedance ctrl) {
		if(ctrlImpSingle == null) {
			ctrlImpSingle = ctrl;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Sets a static reference to the Maintenance window controller.
	 * First-time setting must never be changed. globalErrorFlag sets on an attempt
	 * to do so. 
	 * @param ctrl reference to an initialized controller
	 */
	public static void setControllerMaintenance(ControllerMaintenance ctrl) {
		if(ctrlMaintenance == null) {
			ctrlMaintenance = ctrl;
		} else {
			globalErrorFlag = true;
		}
	}
	
	// ===============================================================================
	
	/**
	 * Getter for Communication window controller
	 * @return reference to the controller
	 */
	public static ControllerCommunication getControllerCommunication() {
		return ctrlCommunication;
	}
	
	// ===============================================================================

	/**
	 * Getter for Current Measurement window controller
	 * @return reference to the controller
	 */
	public static ControllerCurrentMeas getControllerCurrentMeas() {
		return ctrlCurrentMeas;
	}
	
	// ===============================================================================
	
	/**
	 * Getter for Impedance Curve window controller
	 * @return reference to the controller
	 */
	public static ControllerImpedanceCurve getControllerImpCurve() {
		return ctrlImpCurve;
	}
	
	// ===============================================================================
	
	/**
	 * Getter for Single Point Impedance window controller
	 * @return reference to the controller
	 */
	public static ControllerSingleImpedance getControllerImpSingle() {
		return ctrlImpSingle;
	}
	
	// ===============================================================================
	
	/**
	 * Getter for Maintenance window controller
	 * @return reference to the controller
	 */
	public static ControllerMaintenance getControllerMaintenance() {
		return ctrlMaintenance;
	}
	
	// ===============================================================================
}
