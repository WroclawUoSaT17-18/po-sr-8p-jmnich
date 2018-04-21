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
			
			Parent mainSceneParent = FXMLLoader.load(getClass().getResource("/MainScene.fxml"));
			Scene mainScene = new Scene(mainSceneParent);
			AppComponentsBank.setMainScene(mainScene);
			
			AppComponentsBank.setMainStage(primaryStage);	
			primaryStage.setResizable(false);
			primaryStage.setTitle("Cube Lab v1.0");
			primaryStage.setScene(AppComponentsBank.getMainScene());
			
			// disconnect COM port on exit
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					AppComponentsBank.getAppKernel().requestDisconnect();					
				}
			});
			
			kernel.switchScene(Scenes.SCENE_COMMUNICATION);
			kernel.lockNonCommunicationInterface();
			
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}

}
