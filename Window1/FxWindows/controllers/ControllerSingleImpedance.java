package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.StringJoiner;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupBuilder;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kernel.AppComponentsBank;
import kernel.CMD;
import kernel.Measurement;
import kernel.MeasurementCurve;
import kernel.MeasurementParameter;
import kernel.MeasurementPoint;

public class ControllerSingleImpedance {
	
	private class DirectoriesFilter implements Filter<Path> {

		@Override
		public boolean accept(Path arg0) throws IOException {
			return Files.isDirectory(arg0);
		}	
	}
	
	@FXML private Button fxMeasureBtn;
	@FXML private Button fxLoadParamsBtn;
	@FXML private Button fxDeleteParamsBtn;
	@FXML private Button fxSaveParamsBtn;
	@FXML private Button fxParamDatabaseSelectBtn;
	@FXML private Button fxSaveMeasurement;
	@FXML private Button fxSelectMeasWorkspace;
	@FXML private Button fxDeleteMeasBtn;
	
	@FXML private TextField fxParamsNameField;
	@FXML private TextField fxParametersDataBaseField;
	@FXML private TextField fxMeasurementNameField;
	@FXML private TextField fxMeasDataBaseField;
	
	@FXML private ListView<String> fxModeList;
	@FXML private ListView<String> fxVoltageList;
	@FXML private ListView<String> fxCurrentList;
	@FXML private ListView<String> fxParameterSetsList;	
	@FXML private ListView<String> fxMeasurementList;

	@FXML private TableView<MeasurementParameter> fxParamsSetTable;
	@FXML private TableView<MeasurementParameter> fxParamsActualTable;
	
	@FXML private LineChart<Number,Number> fxVoltageChart;
	@FXML private LineChart<Number,Number> fxCurrentChart;
	
	private ObservableList<String> commandsList;
	private ObservableList<String> currentParamFilesNames;
	
	private Measurement currentMeasurement;
	
	private TableColumn<MeasurementParameter, String> setParamIDColumn;
	private TableColumn<MeasurementParameter, String> measParamIDColumn;
	
	private TableColumn<MeasurementParameter, String> setParamValueColumn;
	private TableColumn<MeasurementParameter, String> measParamValueColumn;
	
	// COMMANDS
	private final static String cmd100 = "<100> IMP MAN RAW";
	private final static String cmd101 = "<101> IMP AUTO RAW";
	private final static String cmd102 = "<102> ELEC CODE";
	private final static String cmd103 = "<103> MAN FIL";
	private final static String cmd104 = "<104> MAN IMP";
	private final static String cmd105 = "<105> AUT IMP";
	private final static String cmd106 = "<106> FULL AUT";
	
	// UTIL
	private final static String space = " ";
	private boolean currentMeasurementSaved = false;
	private boolean controllerEnable = false;
	
	public ControllerSingleImpedance() {
		AppComponentsBank.setControllerImpSingle(this);
	}
	
	
	@SuppressWarnings("unchecked")
	@FXML 
	public void initialize() {
		commandsList = FXCollections.observableArrayList();
		commandsList.add(cmd100);
		commandsList.add(cmd101);
		commandsList.add(cmd102);
		commandsList.add(cmd103);
		commandsList.add(cmd104);
		commandsList.add(cmd105);
		commandsList.add(cmd106);
		
		fxModeList.setItems(commandsList); 
		
		fxModeList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				switchMode(newValue);
				updateCurvesDisplay(currentMeasurement);
			    fxMeasureBtn.setDisable(false);
			}
		});
		
		fxParameterSetsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				fxDeleteParamsBtn.setDisable(false);
			}
		});
		
		// initialize parameter tables
		
		setParamIDColumn = new TableColumn<MeasurementParameter, String>("Parameter");
		setParamValueColumn = new TableColumn<MeasurementParameter, String>("Value");
		
		measParamIDColumn = new TableColumn<MeasurementParameter, String>("Parameter");
		measParamValueColumn = new TableColumn<MeasurementParameter, String>("Value");
		
		
		setParamIDColumn.setCellValueFactory(new PropertyValueFactory<MeasurementParameter, String>("paramID"));		
		setParamValueColumn.setCellValueFactory(new PropertyValueFactory<MeasurementParameter, String>("paramValue"));
		setParamValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
		setParamValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<MeasurementParameter,String>>() {
			
			@Override
			public void handle(CellEditEvent<MeasurementParameter, String> event) {
				
				MeasurementParameter tableParam = fxParamsSetTable.getItems().get(event.getTablePosition().getRow());
				
				for(MeasurementParameter param : currentMeasurement.getSetPointParams()) {
					if(param.getParamID().equals(tableParam.getParamID())) {
						param.setParamValue(event.getNewValue());
						tableParam.setParamValue(event.getNewValue());
					}
				}
				
				fxParamsSetTable.refresh();
			}
		});
		
		measParamIDColumn.setCellValueFactory(new PropertyValueFactory<MeasurementParameter, String>("paramID"));
		measParamValueColumn.setCellValueFactory(new PropertyValueFactory<MeasurementParameter, String>("paramValue"));
		
		
		setParamIDColumn.setMaxWidth(180);
		setParamIDColumn.setPrefWidth(180);
		setParamIDColumn.setMinWidth(180);
		setParamValueColumn.setPrefWidth(210);
		measParamIDColumn.setMaxWidth(180);
		measParamIDColumn.setPrefWidth(180);
		measParamIDColumn.setMinWidth(180);
		measParamValueColumn.setPrefWidth(210);
		
		
		fxParamsSetTable.getColumns().setAll(setParamIDColumn, setParamValueColumn);
		fxParamsActualTable.getColumns().setAll(measParamIDColumn, measParamValueColumn);
		
		fxParamsSetTable.setEditable(true);
		
		// parameters section initialization
		fxDeleteParamsBtn.setDisable(true);
		
		fxParametersDataBaseField.setText(AppComponentsBank.getAppKernel().getDefaultParametersLocation());
		
		if(fxParametersDataBaseField.getText().equals("")) {
			fxSaveParamsBtn.setDisable(true);
			fxLoadParamsBtn.setDisable(true);
		}
		
		currentParamFilesNames = FXCollections.observableArrayList();
		fxParameterSetsList.setItems(currentParamFilesNames);
		
		// measurement data management init
		fxMeasDataBaseField.setText(AppComponentsBank.getAppKernel().getDefaultSingleImpedanceDataDumpLocation());
		
		fxSaveMeasurement.setDisable(true);
		fxMeasureBtn.setDisable(true);
		
		updateMeasurementList();
		fxMeasurementList.getSelectionModel().select("CURRENT MEASUREMENT");
		
		fxMeasurementList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				if(oldValue != null && newValue != null) {

					if(newValue.equals("CURRENT MEASUREMENT")) {
						setNonInvestigationGUIDisable(false);
					} else {
						setNonInvestigationGUIDisable(true);
					}
					
					if(oldValue.equals(newValue)) {
						return;
					}

					if(oldValue.equals("CURRENT MEASUREMENT")) {

						saveCurrentMeasurementToWorkspace("CURRENT MEASUREMENT");
					}

					Measurement loadedMeasurement = loadMeasurementDataFromFile(newValue);

					if(loadedMeasurement == null) {
						loadedMeasurement = new Measurement(cmd100);
					}

					// update GUI for the loaded measurement
					loadMeasurementDataIntoGUI(loadedMeasurement);
				}
			}
		});
		
		fxCurrentList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				LinkedList<MeasurementCurve> curveToDisplay = new LinkedList<>();
				
				for(MeasurementCurve cv : currentMeasurement.getCurvesCurrent()) {
					if(cv.getId().equals(newValue)) {
						curveToDisplay.add(cv);
					}
				}
				
				updateCurrentChart(curveToDisplay, true);				
			}
		});
		
		fxVoltageList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				
				LinkedList<MeasurementCurve> curveToDisplay = new LinkedList<>();
				
				for(MeasurementCurve cv : currentMeasurement.getCurvesVoltage()) {
					if(cv.getId().equals(newValue)) {
						curveToDisplay.add(cv);
					}
				}
				
				updateVoltageChart(curveToDisplay, true);				
			}
		});
		
		switchMode(cmd100);
	}
	
	public void setControllerEnable(boolean enable) {
		controllerEnable = enable;
	}
	
	private Measurement loadMeasurementDataFromFile(String fileNameInWorkspace) {
		
		Measurement loadedMeasurement = null;
		
		String mainMeasurementDatabasePath = AppComponentsBank.getAppKernel().getDefaultSingleImpedanceDataDumpLocation();
		File workspaceDirectory = new File(mainMeasurementDatabasePath);

		try(DirectoryStream<Path> ds = Files.newDirectoryStream(workspaceDirectory.toPath(), new DirectoriesFilter())) {

			Path particularMeasPath = null;

			for(Path measPath : ds) {

				if(measPath.getFileName().toString().equals(fileNameInWorkspace)) {
					particularMeasPath = measPath;
				}
			}

			if(particularMeasPath != null) {

				Path pathToParamsFile = Paths.get(particularMeasPath.toString(), fileNameInWorkspace + ".mobi_meas");
				Path pathToCurveFiles = Paths.get(particularMeasPath.toString(), fileNameInWorkspace + "_curves.csv");

				File paramsFile = pathToParamsFile.toFile();
				File curvesFile = pathToCurveFiles.toFile();

				String cmdID = "";
				String fullCmd = "";

				ObservableList<MeasurementParameter> setParams = FXCollections.observableArrayList();
				ObservableList<MeasurementParameter> resultParams = FXCollections.observableArrayList();

				LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
				LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();

				// params file
				try(BufferedReader reader = new BufferedReader(new FileReader(paramsFile))) {

					String loadingContext = "";

					while(reader.ready()) {
						String line = reader.readLine();

						// check if the line contains anything at all and is not a comment
						if((line.contains("#") == false) && (line.trim().length() > 0)) {

							// lines with ':' are parameter data lines while those without are headers
							if(line.contains(":")) {
								if(loadingContext.contains("SET PARAMETERS")) {

									String[] data = line.split(":");												
									setParams.add(new MeasurementParameter(data[0].trim(), data[1].trim()));
								} 
								else if(loadingContext.contains("ACTUAL PARAMETERS")) {

									String[] data = line.split(":");
									resultParams.add(new MeasurementParameter(data[0].trim(), data[1].trim()));
								} 
								else if(loadingContext.contains("MEASUREMENT COMMAND")) {
									String[] data = line.split(":");

									if(data[0].trim().equals("COMMAND ID")) {
										cmdID = data[1].trim();
									} else {
										fullCmd = data[1].trim();
									}												
								}
								else {
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Measurement loading error");
									alert.setHeaderText("Data corrupted");

									alert.showAndWait();
								}

							} else {
								loadingContext = line.trim();
							}										
						}
					}

					reader.close();

				} catch(IOException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Measurement loading error");
					alert.setHeaderText("Unable to read .mobi_meas file");

					alert.showAndWait();
				}

				// curves file
				try(BufferedReader reader = new BufferedReader(new FileReader(curvesFile))) {

					LinkedList<MeasurementCurve> collectiveCurveStorage = new LinkedList<>();

					String header = reader.readLine();
					
					if(header.equals("NO CURVES RECORDED FOR THIS MEASUREMENT") == false) {

						String[] headerData = header.trim().split("\t");

						for(String s : headerData) {

							if(s.equals("SAMPLE") == false) {
								if(s.contains("CURRENT")) {
									MeasurementCurve curve = new MeasurementCurve(s);
									currentCurves.add(curve);
									collectiveCurveStorage.add(curve);
								} 
								else if(s.contains("VOLTAGE")) {
									MeasurementCurve curve = new MeasurementCurve(s);
									voltageCurves.add(curve);
									collectiveCurveStorage.add(curve);
								} 
								else {
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Measurement loading error");
									alert.setHeaderText("Data column ID not recognized");

									alert.showAndWait();
								}										
							}

						}

						while(reader.ready()) {

							String line = reader.readLine();

							if(line.trim().equals("") == false) {

								String[] data = line.trim().split("\t");

								for(int i = 0 ; i < collectiveCurveStorage.size() ; i++) {
									collectiveCurveStorage.get(i).addPointToCurve(new MeasurementPoint(data[0].trim(), data[i+1].trim()));
								}
							}
						}

					} else {
						// ignore
					}
					
					reader.close();

				} catch(IOException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Measurement loading error");
					alert.setHeaderText("Unable to read the curves file");

					alert.showAndWait();
				}

				// encapsulate data
				loadedMeasurement = new Measurement(cmdID);
				loadedMeasurement.setCmdFull(fullCmd);
				loadedMeasurement.setActualParams(resultParams);
				loadedMeasurement.setSetPointParams(setParams);
				loadedMeasurement.setMeasurementCurvesCurrent(currentCurves);
				loadedMeasurement.setMeasurementCurvesVoltage(voltageCurves);

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Measurement loading error");
				alert.setHeaderText("Unable to locate the desired measurement data");

				alert.showAndWait();
			}



		} catch(Exception e) {
			e.printStackTrace();	
		}
		
		return loadedMeasurement;
		
	}
	
	private void setNonInvestigationGUIDisable(boolean requestedState) {
		
		// lists 
		fxModeList.setDisable(requestedState);
		fxParameterSetsList.setDisable(requestedState);
		
		// tables
		fxParamsSetTable.setEditable(!requestedState);
		
		// buttons
		fxMeasureBtn.setDisable(requestedState);
		fxSaveMeasurement.setDisable(requestedState);
		fxMeasureBtn.setDisable(requestedState);
		fxLoadParamsBtn.setDisable(requestedState);
	}
	
	// just update currentMeasurement with new Mobi data
	private void updateCurrentMeasurementDataInGUI() {
		updateActualParametersTable(currentMeasurement.getResultParams());
		updateSetParametersTable(currentMeasurement.getSetPointParams());
		
		updateCurvesDisplay(currentMeasurement);
	}
	
	private void loadMeasurementDataIntoGUI(Measurement meas) {
		
		// switch to correct command context
		fxModeList.getSelectionModel().select(meas.getCmdID());	
		switchMode(meas.getCmdID());
					
		currentMeasurement = meas;
		
		// load parameter values
		updateActualParametersTable(meas.getResultParams());
		updateSetParametersTable(meas.getSetPointParams());
		
		// load curves
		updateCurvesDisplay(meas);
	}
	
	private void updateCurvesDisplay(Measurement meas) {
		
		fxCurrentList.getItems().clear();
		fxVoltageList.getItems().clear();
		
		updateCurrentChart(null, true);
		updateVoltageChart(null, true);
		
		if(meas.getCurvesCurrent().size() > 0) {
			
			for(MeasurementCurve curve : meas.getCurvesCurrent()) {
				fxCurrentList.getItems().add(curve.getId());
			}
			
			fxCurrentList.getSelectionModel().select(0);
			
			LinkedList<MeasurementCurve> currentCurvesList = new LinkedList<>();
			currentCurvesList.add(meas.getCurvesCurrent().get(0));
		}
		
		if(meas.getCurvesVoltage().size() > 0) {
			
			for(MeasurementCurve curve : meas.getCurvesVoltage()) {
				fxVoltageList.getItems().add(curve.getId());
			}
			
			fxVoltageList.getSelectionModel().select(0);
			
			LinkedList<MeasurementCurve> voltageCurvesList = new LinkedList<>();
			voltageCurvesList.add(meas.getCurvesVoltage().get(0));
		}
	}
	
	private void updateMeasurementList() {
		// scans measurement files from the workspace
		// automatically takes care for the current measurement
		ObservableList<String> newMeasurementListContent = FXCollections.observableArrayList();
		
		String mainMeasurementDatabasePath = AppComponentsBank.getAppKernel().getDefaultSingleImpedanceDataDumpLocation();
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
		fxMeasurementList.setItems(newMeasurementListContent);
	}
	
	private void assembleCommand() {
		
		String cmdID = currentMeasurement.getCmdID();
		String cmdIDOnly = cmdID.substring(cmdID.indexOf("<") + 1,  cmdID.indexOf(">"));
		
		switch(currentMeasurement.getCmdID()) {
		case cmd100: 
		{			
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("9");
			joiner.add(getCurrentMeasParam("CHANNEL"));
			joiner.add(getCurrentMeasParam("FREQUENCY"));
			joiner.add(getCurrentMeasParam("PEAK-PEAK"));
			joiner.add(getCurrentMeasParam("MAIN OFFSET"));
			joiner.add(getCurrentMeasParam("TIA RESISTANCE"));
			joiner.add(getCurrentMeasParam("CURRENT OFFSET"));
			joiner.add(getCurrentMeasParam("CURRENT GAIN RESISTOR"));
			joiner.add(getCurrentMeasParam("VOLTAGE GAIN RESISTOR"));
			joiner.add(getCurrentMeasParam("PRE-MEASUREMENT DELAY"));
			
			currentMeasurement.setCmdFull(joiner.toString());
			break;
		}
		case cmd101:
		{
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("5");
			joiner.add(getCurrentMeasParam("CHANNEL"));
			joiner.add(getCurrentMeasParam("FREQUENCY"));
			joiner.add(getCurrentMeasParam("PEAK-PEAK"));
			joiner.add(getCurrentMeasParam("OFFSET"));
			joiner.add(getCurrentMeasParam("PRE-MEASUREMENT DELAY"));
			
			currentMeasurement.setCmdFull(joiner.toString());			
			break;
		}
		case cmd102:
		{
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("0");
			
			currentMeasurement.setCmdFull(joiner.toString());
			break;
		}
		case cmd103:
		{
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("9");
			joiner.add(getCurrentMeasParam("CHANNEL"));
			joiner.add(getCurrentMeasParam("FREQUENCY"));
			joiner.add(getCurrentMeasParam("PEAK-PEAK"));
			joiner.add(getCurrentMeasParam("MAIN OFFSET"));
			joiner.add(getCurrentMeasParam("TIA RESISTANCE"));
			joiner.add(getCurrentMeasParam("CURRENT OFFSET"));
			joiner.add(getCurrentMeasParam("CURRENT GAIN RESISTOR"));
			joiner.add(getCurrentMeasParam("VOLTAGE GAIN RESISTOR"));
			joiner.add(getCurrentMeasParam("PRE-MEASUREMENT DELAY"));
			
			currentMeasurement.setCmdFull(joiner.toString());
			break;
		}
		case cmd104:
		{
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("9");
			joiner.add(getCurrentMeasParam("CHANNEL"));
			joiner.add(getCurrentMeasParam("FREQUENCY"));
			joiner.add(getCurrentMeasParam("PEAK-PEAK"));
			joiner.add(getCurrentMeasParam("MAIN OFFSET"));
			joiner.add(getCurrentMeasParam("TIA RESISTANCE"));
			joiner.add(getCurrentMeasParam("CURRENT OFFSET"));
			joiner.add(getCurrentMeasParam("CURRENT GAIN RESISTOR"));
			joiner.add(getCurrentMeasParam("VOLTAGE GAIN RESISTOR"));
			joiner.add(getCurrentMeasParam("PRE-MEASUREMENT DELAY"));
			
			currentMeasurement.setCmdFull(joiner.toString());
			break;
		}
		case cmd105:
		{
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("5");
			joiner.add(getCurrentMeasParam("CHANNEL"));
			joiner.add(getCurrentMeasParam("FREQUENCY"));
			joiner.add(getCurrentMeasParam("PEAK-PEAK"));
			joiner.add(getCurrentMeasParam("OFFSET"));
			joiner.add(getCurrentMeasParam("PRE-MEASUREMENT DELAY"));
			
			currentMeasurement.setCmdFull(joiner.toString());
			break;
		}
		case cmd106:
		{
			StringJoiner joiner = new StringJoiner(space);
			
			joiner.add(cmdIDOnly);
			joiner.add("2");
			joiner.add(getCurrentMeasParam("CHANNEL"));
			joiner.add(getCurrentMeasParam("FREQUENCY"));
			
			currentMeasurement.setCmdFull(joiner.toString());
			break;
		}
		default:
		{
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Command transmission failed");
			alert.setHeaderText("Could not assembly the requested command");
			alert.setContentText("");

			alert.showAndWait();
		}}
	}
	
	public String getCurrentMeasParam(String paramID) {
		String param = "";
		
		for(MeasurementParameter par : currentMeasurement.getSetPointParams()) {
			if(par.getParamID().equals(paramID)) {
				param = par.getParamValue();
			}
		}
		
		return param;
	}

	private void updateActualParametersTable(LinkedList<MeasurementParameter> newParamValues) {

		for(MeasurementParameter newParam: newParamValues) {

			boolean paramLocated = false;

			for(MeasurementParameter tableParam: fxParamsActualTable.getItems()) {
				if(tableParam.getParamID().equals(newParam.getParamID())) {
					paramLocated = true;

					tableParam.setParamValue(newParam.getParamValue());
				}
			}

			if(paramLocated == false) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Data loading annomaly");
				alert.setHeaderText("New data contains a parameter not supported for the selected measurement");

				alert.showAndWait();
			}
		}

		fxParamsActualTable.refresh();
	}
	
	// clears the current measurement
	public void switchMode(String mode) {
		
		switch(mode) {
		case cmd100: 
		{
			currentMeasurement = new Measurement(cmd100);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CHANNEL", "1"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("FREQUENCY", "10000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PEAK-PEAK", "50000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("MAIN OFFSET", "500000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("TIA RESISTANCE", "15000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CURRENT OFFSET", "350000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CURRENT GAIN RESISTOR", "5100"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "5100"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PRE-MEASUREMENT DELAY", "200"));
			
			
			currentMeasurement.addResultParam(new MeasurementParameter("CHANNEL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("FREQUENCY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PEAK-PEAK", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("MAIN OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("TIA RESISTANCE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PRE-MEASUREMENT DELAY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("POINTS PER PERIOD", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("ELECTRODE CODE", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("100", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		case cmd101:			
		{
			currentMeasurement = new Measurement(cmd101);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CHANNEL", "1"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("FREQUENCY", "10000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PEAK-PEAK", "50000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("OFFSET", "200000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PRE-MEASUREMENT DELAY", "200"));
			
			
			currentMeasurement.addResultParam(new MeasurementParameter("CHANNEL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("FREQUENCY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PEAK-PEAK", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("MAIN OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("TIA RESISTANCE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PRE-MEASUREMENT DELAY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("POINTS PER PERIOD", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("ELECTRODE CODE", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("101", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		case cmd102:
		{
			currentMeasurement = new Measurement(cmd102);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("<NO PARAMETERS REQUIRED>", ""));
			
			currentMeasurement.addResultParam(new MeasurementParameter("ELECTRODE CODE", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("102", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		case cmd103:
		{			
			currentMeasurement = new Measurement(cmd103);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CHANNEL", "1"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("FREQUENCY", "10000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PEAK-PEAK", "50000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("MAIN OFFSET", "500000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("TIA RESISTANCE", "15000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CURRENT OFFSET", "350000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CURRENT GAIN RESISTOR", "5100"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "5100"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PRE-MEASUREMENT DELAY", "200"));
			
			
			currentMeasurement.addResultParam(new MeasurementParameter("CHANNEL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("FREQUENCY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PEAK-PEAK", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("MAIN OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("TIA RESISTANCE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PRE-MEASUREMENT DELAY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("POINTS PER PERIOD", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("ELECTRODE CODE", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("103", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		case cmd104:
		{
			currentMeasurement = new Measurement(cmd104);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CHANNEL", "1"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("FREQUENCY", "10000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PEAK-PEAK", "50000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("MAIN OFFSET", "500000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("TIA RESISTANCE", "15000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CURRENT OFFSET", "350000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CURRENT GAIN RESISTOR", "5100"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "5100"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PRE-MEASUREMENT DELAY", "200"));
			
			
			currentMeasurement.addResultParam(new MeasurementParameter("CHANNEL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("FREQUENCY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PEAK-PEAK", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("MAIN OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("TIA RESISTANCE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PRE-MEASUREMENT DELAY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("POINTS PER PERIOD", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("ELECTRODE CODE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-REAL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-IMAGINARY", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("104", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		case cmd105:
		{
			currentMeasurement = new Measurement(cmd105);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CHANNEL", "1"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("FREQUENCY", "10000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PEAK-PEAK", "50000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("OFFSET", "200000"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("PRE-MEASUREMENT DELAY", "200"));
			
			
			currentMeasurement.addResultParam(new MeasurementParameter("CHANNEL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("FREQUENCY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PEAK-PEAK", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("MAIN OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("TIA RESISTANCE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT OFFSET", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("CURRENT GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("VOLTAGE GAIN RESISTOR", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("PRE-MEASUREMENT DELAY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("POINTS PER PERIOD", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("ELECTRODE CODE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-REAL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-IMAGINARY", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("105", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		case cmd106:
		{
			currentMeasurement = new Measurement(cmd106);
			
			currentMeasurement.addSetPointParameter(new MeasurementParameter("CHANNEL", "1"));
			currentMeasurement.addSetPointParameter(new MeasurementParameter("FREQUENCY", "10000"));
			
			currentMeasurement.addResultParam(new MeasurementParameter("CHANNEL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("FREQUENCY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-REAL", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-IMAGINARY", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-MAGNITUDE", "<NO DATA>"));
			currentMeasurement.addResultParam(new MeasurementParameter("IMPEDANCE-PHASE[DEG]", "<NO DATA>"));
			
			// LOAD DATA INTO THE TABLES
			fxParamsActualTable.setItems(currentMeasurement.getResultParams());
			fxParamsSetTable.setItems(currentMeasurement.getSetPointParams());
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand("106", pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
			
			break;
		}
		default:
		{
			// do nothing
		}}
	}
	
	private void updateActualParametersTable(ObservableList<MeasurementParameter> newParamValues) {
		
		for(MeasurementParameter newParam: newParamValues) {
			
			boolean paramLocated = false;
			
			for(MeasurementParameter tableParam: fxParamsActualTable.getItems()) {
				if(tableParam.getParamID().equals(newParam.getParamID())) {
					paramLocated = true;
					
					tableParam.setParamValue(newParam.getParamValue());
				}
			}
			
			if(paramLocated == false) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Data loading annomaly");
				alert.setHeaderText("New data contains a parameter not supported for the selected measurement");

				alert.showAndWait();
			}
		}
		
		fxParamsActualTable.refresh();
	}
	
	private void updateSetParametersTable(ObservableList<MeasurementParameter> newParamValues) {
		
		for(MeasurementParameter newParam: newParamValues) {
			
			boolean paramLocated = false;
			
			for(MeasurementParameter tableParam: fxParamsSetTable.getItems()) {
				if(tableParam.getParamID().equals(newParam.getParamID())) {
					paramLocated = true;
					
					tableParam.setParamValue(newParam.getParamValue());
				}
			}
			
			if(paramLocated == false) {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Data loading annomaly");
				alert.setHeaderText("New data contains a parameter not supported for the selected measurement");

				alert.showAndWait();
			}
		}
		
		fxParamsActualTable.refresh();
	}
	
	public void passMeasurementData(String originalRequestID, LinkedList<MeasurementParameter> actualParams, 
			LinkedList<MeasurementCurve> voltageCurves, LinkedList<MeasurementCurve> currentCurves) {
		
		if(controllerEnable) {

			fxSaveMeasurement.setDisable(false);

			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					String curMeasName = currentMeasurement.getCmdID();
					String curMeasID = curMeasName.substring(curMeasName.indexOf("<") + 1,  curMeasName.indexOf(">"));

					if(curMeasID.equals(originalRequestID)) {					
						currentMeasurement.clearMeasurementCurvesCurrent();
						currentMeasurement.clearMeasurementCurvesVoltage();
						currentMeasurement.setMeasurementCurvesCurrent(currentCurves);
						currentMeasurement.setMeasurementCurvesVoltage(voltageCurves);

						currentMeasurement.setActualParams(FXCollections.observableArrayList(actualParams));

						updateCurrentMeasurementDataInGUI();

					} else {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Data Error");
						alert.setHeaderText("Input data from Mobi incompatible with current measurement");

						alert.showAndWait();
					}				
				}			
			});	
		}
	}
	
	private void updateCurrentChart(LinkedList<MeasurementCurve> curves, boolean resetChart) {
		
		if(resetChart) {
			fxCurrentChart.getData().clear();
		}
		
		if(curves != null) {

			for(MeasurementCurve curve : curves) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						XYChart.Series<Number, Number> series = new XYChart.Series<>();

						for(MeasurementPoint point : curve.getCurve()) {
							series.getData().add(new XYChart.Data<Number, Number>(Double.valueOf(point.getX()), 
									Double.valueOf(point.getY())));
						}					

						series.setName(curve.getId());
						fxCurrentChart.getData().add(series);
					}
				});
			}		
		}
	}
	
	private void updateVoltageChart(LinkedList<MeasurementCurve> curves, boolean resetChart) {

		if(resetChart) {
			fxVoltageChart.getData().clear();
		}
		
		if(curves != null) {

			for(MeasurementCurve curve : curves) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						XYChart.Series<Number, Number> series = new XYChart.Series<>();

						for(MeasurementPoint point : curve.getCurve()) {
							series.getData().add(new XYChart.Data<Number, Number>(Double.valueOf(point.getX()), 
									Double.valueOf(point.getY())));
						}					

						series.setName(curve.getId());
						fxVoltageChart.getData().add(series);
					}
				});
			}
		}
	}
	
	private File[] getParameterFilesAssociatedWithCommand(String commandNumber, String mainParameterDatabasePath) {
		File mainDirectory = new File(mainParameterDatabasePath);
		Path commandSpecificDirectory = null;
		if(mainDirectory.getPath().trim().length() > 0) {
			LinkedList<Path> availableDirectories = new LinkedList<>();

			try (DirectoryStream<Path> ds = Files.newDirectoryStream(mainDirectory.toPath(), new DirectoriesFilter())) {
				for (Path p : ds) {
					availableDirectories.add(p);
				}
				
				for(Path p : availableDirectories) {
					if(p.getFileName().toString().equals(commandNumber)) {
						commandSpecificDirectory = p;
					}
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed direcotry filtering for the parameters data base");
			}
			
			if(commandSpecificDirectory != null) {
				File dir = new File(commandSpecificDirectory.toString());
				File[] parametersFiles = dir.listFiles(new FileFilter() {
					
					@Override
					public boolean accept(File pathname) {
						
						if(pathname.toString().endsWith(".mobi_param")) {
							return true;
						} else {
							return false;
						}
					}
				});
				
				return parametersFiles;
				
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void saveCurrentMeasurementToWorkspace(String measurementName) {
		
		File workspaceDirectory = new File(AppComponentsBank.getAppKernel().getDefaultSingleImpedanceDataDumpLocation());
		
		String fullMeasurementName = measurementName + ".mobi_meas";
		String fullCurvesFileName = measurementName + "_curves.csv";
		
		if(workspaceDirectory.getPath().trim().length() > 0) {
			
			File targetGeneralFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, fullMeasurementName).toFile();
			
			if(targetGeneralFile.getParentFile() != null) {
				targetGeneralFile.getParentFile().mkdirs();
			}
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(targetGeneralFile))) {
				
				// write header to file
				String header1 = "#" + " MOBI MEASUREMENT DATA    | TIME STAMP:  " + AppComponentsBank.getAppKernel().getCurrentTimeStamp();
				String header2 = "#" + " MEASUREMENT ID: " + measurementName;
				String header3 = "#";
				String header4 = "MEASUREMENT COMMAND";
				String header5 = "COMMAND ID   :   " + currentMeasurement.getCmdID();
				String header6 = "FULL COMMAND   :   " + currentMeasurement.getCmdFull();
				String header7 = "#";
				
				writer.write(header1);
				writer.newLine();
				writer.write(header2);
				writer.newLine();
				writer.write(header3);
				writer.newLine();
				writer.write(header4);
				writer.newLine();
				writer.write(header5);
				writer.newLine();
				writer.write(header6);
				writer.newLine();
				writer.write(header7);
				writer.newLine();

				
				
				// write set parameters
				String paramHeader1 = "#" + "===================== MEASUREMENT PARAMETERS SECTION =====================";
				String paramHeader2 = "#";
				String paramHeader3 = "#";
				
				writer.write(paramHeader1);
				writer.newLine();
				writer.write(paramHeader2);
				writer.newLine();
				writer.write(paramHeader3);
				writer.newLine();
				
				writer.write("SET PARAMETERS");
				writer.newLine();
				for(MeasurementParameter setParam : currentMeasurement.getSetPointParams()) {
					writer.write(setParam.getParamID() + "  :  " + setParam.getParamValue());
					writer.newLine();
				}
				
				writer.write("#");
				writer.newLine();
				writer.write("ACTUAL PARAMETERS");
				writer.newLine();
				for(MeasurementParameter actualParam : currentMeasurement.getResultParams()) {
					writer.write(actualParam.getParamID() + "  :  " + actualParam.getParamValue());
					writer.newLine();
				}
				
				writer.write("#");
				writer.newLine();
				writer.write("#");
				writer.newLine();
				writer.write("#");
				writer.newLine();
				
				// write measurement curves
				String curvesHeader1 = "#" + "===================== MEASUREMENT CURVES SECTION =====================";
				String curvesHeader2 = "#" + " CURRENT CURVES COUNT: " + String.valueOf(currentMeasurement.getCurvesCurrent().size());
				String curvesHeader3 = "#" + " VOLTAGE CURVES COUNT: " + String.valueOf(currentMeasurement.getCurvesVoltage().size());
				String curvesHeader4 = "#" + " CURVES LIST:";
				
				writer.write(curvesHeader1);
				writer.newLine();
				writer.write(curvesHeader2);
				writer.newLine();
				writer.write(curvesHeader3);
				writer.newLine();
				writer.write(curvesHeader4);
				writer.newLine();
				
				for(MeasurementCurve curCurve : currentMeasurement.getCurvesCurrent()) {
					writer.write("#" + " CURVE ID: " + curCurve.getId() + "\t CURVE LENGTH: " + String.valueOf(curCurve.getCurve().size()));
					writer.newLine();
				}
				for(MeasurementCurve voltCurve : currentMeasurement.getCurvesVoltage()) {
					writer.write("#" + " CURVE ID: " + voltCurve.getId() + "\t CURVE LENGTH: " + String.valueOf(voltCurve.getCurve().size()));
					writer.newLine();
				}
				
				writer.write("#EoF");
				writer.newLine();
				
				writer.close();
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			File targetCurvesFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, fullCurvesFileName).toFile();
			
			if(targetGeneralFile.getParentFile() != null) {
				targetGeneralFile.getParentFile().mkdirs();
			}
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(targetCurvesFile))) {
				
				if(currentMeasurement.getCurvesCurrent().size() > 0 && currentMeasurement.getCurvesVoltage().size() > 0) {
					
					// the first line
					writer.write("SAMPLE" + "\t");
					
					for(MeasurementCurve curve: currentMeasurement.getCurvesVoltage()) {
						writer.write(curve.getId() + "\t");
					}
					for(MeasurementCurve curve: currentMeasurement.getCurvesCurrent()) {
						writer.write(curve.getId() + "\t");
					}
					writer.newLine();
					
					// the data lines...
					for(int i = 0; i < currentMeasurement.getCurvesVoltage().getFirst().getCurve().size(); i++) {
						
						writer.write(String.valueOf(i) + "\t");
						
						for(MeasurementCurve curve: currentMeasurement.getCurvesVoltage()) {
							writer.write(curve.getCurve().get(i).getY() + "\t");
						}
						for(MeasurementCurve curve: currentMeasurement.getCurvesCurrent()) {
							writer.write(curve.getCurve().get(i).getY() + "\t");
						}
						writer.newLine();
					}
					
					writer.close();
					
				} else {
					writer.write("NO CURVES RECORDED FOR THIS MEASUREMENT");
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			// chart images
	        try {
	        	File targetChartFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, "current.png").toFile();
	        	WritableImage snapShot = null;
		        snapShot = fxCurrentChart.snapshot(null, snapShot);
				ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", targetChartFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        try {
	        	File targetChartFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, "voltage.png").toFile();
	        	WritableImage snapShot = null;
		        snapShot = fxVoltageChart.snapshot(null, snapShot);
				ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", targetChartFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Measurement workspace access error");
			alert.setHeaderText("No path to the workspace");

			alert.showAndWait();
		}
		
	}
	
	public void clearGUIForNewMeasurement() {
		// TODO
	}
	
	// GUI handlers
	
	@FXML
	public void measureBtn(ActionEvent event) {
		this.assembleCommand();
		AppComponentsBank.getAppKernel().requestCommandTransmission(currentMeasurement.getCmdFull());
		
		currentMeasurementSaved = false;
	}
	
	@FXML
	public void loadParamsBtn(ActionEvent event) {
		String selectedParam = fxParameterSetsList.getSelectionModel().getSelectedItem();
		
		if(selectedParam == null) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("No file selected");
			alert.setHeaderText("Select parameters file first");

			alert.showAndWait();
			
			return;
		}
		
		String fileName = selectedParam + ".mobi_param";
		String cmdID = currentMeasurement.getCmdID();
		String commandSpecificDir= cmdID.substring(cmdID.indexOf("<") + 1,  cmdID.indexOf(">"));
		Path pathToFile = Paths.get(AppComponentsBank.getAppKernel().getDefaultParametersLocation(), commandSpecificDir, fileName);  
		
		try(BufferedReader reader = new BufferedReader(new FileReader(pathToFile.toString()))) {
			
			while(reader.ready()) {
				String line = reader.readLine();
				// ignore all lines which contain "#" sign
				if(!(line.contains("#"))) {
					String[] paramAndValue = line.split("=");
					
					boolean paramLocated = false;
					
					for(MeasurementParameter param : fxParamsSetTable.getItems()) {
						if(param.getParamID().equals(paramAndValue[0])) {
							paramLocated = true;
							param.setParamValue(paramAndValue[1]);
						}
					}
					
					if(paramLocated == false) {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Parameter loading annomaly");
						alert.setHeaderText("Parameter in file not present in parameters table");
						alert.setContentText("File is likely corrupted or not supported");

						alert.showAndWait();
					}
				}
			}
			
			fxParamsSetTable.refresh();
			
		} catch(Exception e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Param load error");
			alert.setHeaderText("Could not load parameters file");
			alert.setContentText("File is either corrupted or can not be asscessed");

			alert.showAndWait();
		}
	}
	
	@FXML
	public void deleteParamsBtn(ActionEvent event) {
		String selectedFileName = fxParameterSetsList.getSelectionModel().getSelectedItem();
		fxParameterSetsList.getItems().remove(selectedFileName);
		
		String fileName = selectedFileName + ".mobi_param";
		String cmdID = currentMeasurement.getCmdID();
		String commandSpecificDir= cmdID.substring(cmdID.indexOf("<") + 1,  cmdID.indexOf(">"));
		Path pathToFile = Paths.get(AppComponentsBank.getAppKernel().getDefaultParametersLocation(), commandSpecificDir, fileName);
		
		File file = pathToFile.toFile();
		if(file.delete() == false) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("File deletion error");
			alert.setHeaderText("The parameter file could not be deleted");
			alert.setContentText("Make sure your file is not open in another program and that your application can access it");

			alert.showAndWait();
		} 
		
		if(fxParameterSetsList.getSelectionModel().getSelectedItem() == null) {
			fxDeleteParamsBtn.setDisable(true);
		}
			
	}
	
	@FXML
	public void saveParamsBtn(ActionEvent event) {
		String paramFileName = fxParamsNameField.getText();
		
		if(paramFileName.equals("")) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Param file name");
			alert.setHeaderText("Target file name not specified");
			alert.setContentText("File name set to 'default'");

			alert.showAndWait();

			paramFileName = "default";
		}
		paramFileName += ".mobi_param";
		
		String cmdID = currentMeasurement.getCmdID();
		String commandSpecificDir= cmdID.substring(cmdID.indexOf("<") + 1,  cmdID.indexOf(">"));
		
		File targetFile = new File(Paths.get(AppComponentsBank.getAppKernel().getDefaultParametersLocation(), commandSpecificDir).toString(), 
				paramFileName);
		if(targetFile.getParentFile() != null) {
			targetFile.getParentFile().mkdirs();
		}		
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(targetFile));
			writer.write("# MOBI PARAMETERS FILE - COMMAND : " + currentMeasurement.getCmdID() + " | TIME STAMP:   " + 
					AppComponentsBank.getAppKernel().getCurrentTimeStamp() + "\r\n");
			
			for(MeasurementParameter mp : currentMeasurement.getSetPointParams()) {
				writer.write(mp.getParamID() + "=" + mp.getParamValue() + "\r\n");
			}
			
			writer.write("#EoF\r\n");
			
			// UPDATE AVAILABLE PARAMETER SETS
			String pathToDirectory = AppComponentsBank.getAppKernel().getDefaultParametersLocation();
			fxParametersDataBaseField.setText(pathToDirectory);

			File[] paramFiles = getParameterFilesAssociatedWithCommand(commandSpecificDir, pathToDirectory);
			
			currentParamFilesNames.clear();
			if(paramFiles != null) {
				for(File singleParamFile : paramFiles) {
					String name = singleParamFile.getName().substring(0, singleParamFile.getName().lastIndexOf("."));
					currentParamFilesNames.add(name);
				}
				fxParameterSetsList.setItems(currentParamFilesNames);
			}
		} 
		catch (IOException e) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Param file creation error");
			alert.setHeaderText("Saving parameters to file failed!");
			alert.setContentText("Make sure you chose the correct path, file name and a vaild data for save");

			alert.showAndWait();
		} 
		finally {

			try {		    	
				if ( writer != null)
					writer.close( );
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Save done");
				alert.setHeaderText("Successful parameters save");
				alert.setContentText("");

				alert.showAndWait();
			} catch ( IOException e) {		    	
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Save error");
				alert.setHeaderText("Can not close the target file!");
				alert.setContentText("Restart the application. Your data might have been lost.");

				alert.showAndWait();
			}
		}	
	}
	
	@FXML
	public void paramDatabaseSelectBtn(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(AppComponentsBank.getMainStage());
		
		if(selectedDirectory != null) {
			AppComponentsBank.getAppKernel().setDefaultParametersLocation(selectedDirectory.getAbsolutePath());
			fxParametersDataBaseField.setText(AppComponentsBank.getAppKernel().getDefaultParametersLocation());
			fxSaveParamsBtn.setDisable(false);
			fxDeleteParamsBtn.setDisable(false);
		}		
	}
	
	@FXML
	public void selectMeasWorkspaceBtn(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(AppComponentsBank.getMainStage());
		
		if(selectedDirectory != null) {
			AppComponentsBank.getAppKernel().setDefaultSingleImpedanceDataDumpLocation(selectedDirectory.getAbsolutePath());
			fxMeasDataBaseField.setText(AppComponentsBank.getAppKernel().getDefaultSingleImpedanceDataDumpLocation());
			fxSaveMeasurement.setDisable(false);
			
		}		
	}
	
	@FXML
	public void saveMeasurementBtn(ActionEvent event) {
		
		fxSaveMeasurement.setDisable(true);
		currentMeasurementSaved = true;
		
		saveCurrentMeasurementToWorkspace(fxMeasurementNameField.getText());
		updateMeasurementList();
	}
	
	@FXML
	public void deleteMeasurementBtn(ActionEvent event) {
		
		String measurement = fxMeasurementList.getSelectionModel().getSelectedItem();
		
		if(measurement.equals("CURRENT MEASUREMENT")) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Deletion error");
			alert.setHeaderText("Current measurement can not be deleted");
			alert.setContentText("Choose another measurement");

			alert.showAndWait();
		} else {
			String mainMeasurementDatabasePath = AppComponentsBank.getAppKernel().getDefaultSingleImpedanceDataDumpLocation();
			File workspaceDirectory = new File(mainMeasurementDatabasePath);

			try(DirectoryStream<Path> ds = Files.newDirectoryStream(workspaceDirectory.toPath(), new DirectoriesFilter())) {

				Path particularMeasPath = null;

				for(Path measPath : ds) {

					if(measPath.getFileName().toString().equals(measurement)) {
						particularMeasPath = measPath;
					}
				}
				
				if(particularMeasPath != null) {
					File dirToDelete = particularMeasPath.toFile();
					
					final File[] files = dirToDelete.listFiles();
					for (File f: files) f.delete();
					dirToDelete.delete();
				}
				
			} catch(IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Deletion error");
				alert.setHeaderText("Selected measurement can not be deleted");
				alert.setContentText("Can not remove one or more files from the measurement directory.");

				alert.showAndWait();
			}
		}
		
		updateMeasurementList();
		
		fxMeasurementList.getSelectionModel().select("CURRENT MEASUREMENT");
		Measurement curMeas = loadMeasurementDataFromFile("CURRENT MEASUREMENT");
		loadMeasurementDataIntoGUI(curMeas);
		setNonInvestigationGUIDisable(false);
	}
}
