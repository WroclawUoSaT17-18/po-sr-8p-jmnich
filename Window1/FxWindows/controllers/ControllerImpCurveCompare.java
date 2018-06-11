package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream.Filter;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.WindowEvent;
import kernel.AppComponentsBank;
import kernel.ImpedanceCurveMeasurement;

public class ControllerImpCurveCompare {
	
	private class MeasContainer {
		
		public String name = "";
		public ImpedanceCurveMeasurement measurement = null;
		
	}
	
	private class DirectoriesFilter implements Filter<Path> {

		@Override
		public boolean accept(Path arg0) throws IOException {
			return Files.isDirectory(arg0);
		}	
	}
	
	@FXML private Button fxCloseBtn;
	@FXML private Button fxResetBtn;
	
	@FXML private LineChart<Number, Number> fxNyquistPlot;
	@FXML private LineChart<Number, Number> fxBodePhasePlot;
	@FXML private LineChart<Number, Number> fxBodeMagPlot;
	
	@FXML private ListView<String> fxMeasurementsList;
	
	public LinkedList<MeasContainer> measurementsBuffer;
	
	public ControllerImpCurveCompare() {
		AppComponentsBank.setCurveCompareController(this);
		measurementsBuffer = new LinkedList<>();
	}

	@FXML
	public void initialize() {
		
		fxMeasurementsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		fxMeasurementsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				fxNyquistPlot.getData().clear();
				fxBodeMagPlot.getData().clear();
				fxBodePhasePlot.getData().clear();
				
				ObservableList<String> items = fxMeasurementsList.getSelectionModel().getSelectedItems();
				
				for(String item : items) {
					
					ImpedanceCurveMeasurement meas = null;

					for(MeasContainer cont : measurementsBuffer) {
						if(cont.name.equals(item)) {
							meas = cont.measurement;
						}
					}

					if(meas != null) {
						fxNyquistPlot.getData().add(meas.getNyquistChartSeries(item));
						fxBodeMagPlot.getData().add(meas.getBodeMagnitudeChartSeries(item));
						fxBodePhasePlot.getData().add(meas.getBodePhaseChartSeries(item));
					}
				}
			}
		});	
		
		fxMeasurementsList.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				fxNyquistPlot.getData().clear();
				fxBodeMagPlot.getData().clear();
				fxBodePhasePlot.getData().clear();
				
				ObservableList<String> items = fxMeasurementsList.getSelectionModel().getSelectedItems();
				
				for(String item : items) {

					ImpedanceCurveMeasurement meas = null;

					for(MeasContainer cont : measurementsBuffer) {
						if(cont.name.equals(item)) {
							meas = cont.measurement;
						}
					}

					if(meas != null) {
						fxNyquistPlot.getData().add(meas.getNyquistChartSeries(item));
						fxBodeMagPlot.getData().add(meas.getBodeMagnitudeChartSeries(item));
						fxBodePhasePlot.getData().add(meas.getBodePhaseChartSeries(item));
					}
				}
			}
		});
	}
	
	public void activateWindow() {
		AppComponentsBank.getCurveCompStage().show();
		AppComponentsBank.getCurveCompStage().setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				closeWindow();
				event.consume();
			}
		});
		
		resetGUI();
	}
	
	private void closeWindow() {
		AppComponentsBank.getCurveCompStage().hide();
	}
	
	private void resetGUI() {
		reloadMeasurements();
		fxNyquistPlot.getData().clear();
		fxBodeMagPlot.getData().clear();
		fxBodePhasePlot.getData().clear();
	}
	
	private void reloadMeasurements() {
		ObservableList<String> newMeasurementListContent = FXCollections.observableArrayList();
		
		String mainMeasurementDatabasePath = AppComponentsBank.getAppKernel().getDefaultImpedanceCurveDataLocation();
		File workspaceDirectory = new File(mainMeasurementDatabasePath);
		
		LinkedList<Path> availableDirectories = new LinkedList<>();
		
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(workspaceDirectory.toPath(), new DirectoriesFilter())) {
			
			for(Path measPath : ds) {
				availableDirectories.add(measPath);
				newMeasurementListContent.add(measPath.getFileName().toString());
			}
			
		} catch(IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Measurement workspace access error");
			alert.setHeaderText("Unable to access measurements");

			alert.showAndWait();
		}
		
		measurementsBuffer.clear();
		
		for(String meas : newMeasurementListContent) {
			MeasContainer newContainer = new MeasContainer();
			
			newContainer.measurement = ControllerImpedanceCurve.loadMeasurementDataFromFile(meas);
			newContainer.name = meas;
			measurementsBuffer.add(newContainer);
		}
		
		fxMeasurementsList.setItems(newMeasurementListContent);
	}
	
	// GUI HANDLERS
	
	@FXML
	public void closeBtn(ActionEvent event) {
		closeWindow();
	}
	
	@FXML
	public void resetBtn(ActionEvent event) {
		resetGUI();
	}
}
