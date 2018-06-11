package kernel;
import controllers.ControllerMainScene;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application{

	private static AppKernel kernel = null;
	
	public static void main(String[] args) {

		kernel = new AppKernel();
		
		Application.launch(args);

	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		try {
			AppComponentsBank.setAppKernel(kernel);
			
			Pane contextButtons = FXMLLoader.load(getClass().getResource("/ContextButtons.fxml"));
			AppComponentsBank.setContextButtonsPane(contextButtons);
			
			Pane communication = FXMLLoader.load(getClass().getResource("/Communication.fxml"));
			Pane currentMeasurement = FXMLLoader.load(getClass().getResource("/CurrentMeasurement.fxml"));
			Pane impedanceCurve = FXMLLoader.load(getClass().getResource("/ImpedanceCurve.fxml"));
			Pane maintenance = FXMLLoader.load(getClass().getResource("/Maintenance.fxml"));
			Pane singleImpedance = FXMLLoader.load(getClass().getResource("/SingleImpedance.fxml"));
			
			AppComponentsBank.setPaneCommunication(new Pane(communication));
			AppComponentsBank.setPaneCurrentMeas(new Pane(currentMeasurement));
			AppComponentsBank.setPaneImpCurve(new Pane(impedanceCurve));
			AppComponentsBank.setPaneMaintenance(new Pane(maintenance));
			AppComponentsBank.setPaneImpSingle(new Pane(singleImpedance));
			
			Parent curveCompParent = FXMLLoader.load(getClass().getResource("/ImpCurveCompare.fxml"));
			Scene impCurveCompScene = new Scene(curveCompParent);
			AppComponentsBank.setCurveCompScene(impCurveCompScene);
			
			Parent mainSceneParent = FXMLLoader.load(getClass().getResource("/MainScene.fxml"));
			Scene mainScene = new Scene(mainSceneParent);
			AppComponentsBank.setMainScene(mainScene);
			
			AppComponentsBank.setMainStage(primaryStage);	
			primaryStage.setResizable(false);
//			primaryStage.setTitle("SensDx Data Wrench v2.2");
			primaryStage.setTitle("Cube Lab");
			primaryStage.setScene(AppComponentsBank.getMainScene());
			
			// disconnect COM port on exit
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					AppComponentsBank.getAppKernel().requestDisconnect();					
				}
			});
			
			Stage curveCompStage = new Stage();
			curveCompStage.setResizable(true);
//			curveCompStage.setTitle("SensDx Impedance Characteristics Compare Tool");
			curveCompStage.setTitle("Impedance Characteristics Compare Tool");
			curveCompStage.initStyle(StageStyle.UTILITY);
			curveCompStage.setScene(impCurveCompScene);
			
			AppComponentsBank.setCurveCompStage(curveCompStage);
			
			kernel.switchScene(Scenes.SCENE_COMMUNICATION);
			kernel.lockNonCommunicationInterface();
			
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}

}
