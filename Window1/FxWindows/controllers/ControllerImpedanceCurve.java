package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringJoiner;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import kernel.AppComponentsBank;
import kernel.CurveMeasurementManager;
import kernel.ImpedanceCurveMeasurement;

public class ControllerImpedanceCurve {

	private class DirectoriesFilter implements Filter<Path> {

		@Override
		public boolean accept(Path arg0) throws IOException {
			return Files.isDirectory(arg0);
		}	
	}
	
	// JAVAFX OBJECTS
	
	@FXML private Button fxMeasureBtn;
	@FXML private Button fxGoToCompareBtn;
	@FXML private Button fxDeleteMeasBtn;
	@FXML private Button fxSaveMeasBtn;
	@FXML private Button fxWorkspaceBtn;
	
	@FXML private TextField fxFrequencyField;
	@FXML private TextField fxWorkspaceAddressField;
	@FXML private TextField fxMeasNameField;
	
	@FXML private RadioButton fxChannel1Radio;
	@FXML private RadioButton fxChannel2Radio;
	@FXML private RadioButton fxChannel3Radio;
	@FXML private RadioButton fxChannel4Radio;
	@FXML private RadioButton fxChannel5Radio;
	@FXML private RadioButton fxChannel6Radio;
	@FXML private RadioButton fxChannel7Radio;
	@FXML private RadioButton fxChannel8Radio;
	
	@FXML private RadioButton fxPrecisionRadio;
	
	@FXML private TextArea fxSystemMsgArea;
	@FXML private TextArea fxMeasCommentArea;
	
	@FXML private ListView<String> fxMeasurementsListView;

	@FXML private LineChart<Number, Number> fxNyquistPlot;
	@FXML private LineChart<Number, Number> fxBodeMagPlot;
	@FXML private LineChart<Number, Number> fxBodePhPlot;
	
	// FIELDS
	
	private boolean controllerEnable = false;
	private LinkedList<RadioButton> channelButtons;
	private CurveMeasurementManager measManager;
	private ImpedanceCurveMeasurement currentMeasurement;
	private static DirectoriesFilter dirFilter;
	
	
	// constructor
	public ControllerImpedanceCurve() {
		AppComponentsBank.setControllerImpCurve(this);
		dirFilter = new DirectoriesFilter();
	}
	
	// JavaFx initialization method
	@FXML
	public void initialize() {
		// channels buttons
		channelButtons = new LinkedList<>();
		channelButtons.addAll(Arrays.asList(
				fxChannel1Radio, fxChannel2Radio,
				fxChannel3Radio, fxChannel4Radio,
				fxChannel5Radio, fxChannel6Radio,
				fxChannel7Radio, fxChannel8Radio
				));
		fxChannel1Radio.setSelected(true);
		
		// workspace
		fxWorkspaceAddressField.setText(AppComponentsBank.getAppKernel().getDefaultImpedanceCurveDataLocation());
		
		// frequencies field
		fxFrequencyField.setText(AppComponentsBank.getAppKernel().getDefaultFrequenciesForImpedanceCurve());
		
		// measurement manager
		measManager = new CurveMeasurementManager(AppComponentsBank.getAppKernel().getDispatcher());

		// measurements list				
		updateMeasurementsList();
		fxMeasurementsListView.getSelectionModel().select("CURRENT MEASUREMENT");

		fxMeasurementsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				if(oldValue == null) {
					//							addSystemMessage("old null");
					fxMeasurementsListView.getSelectionModel().select("CURRENT MEASUREMENT");
				} else {

					if(newValue == null) {
						//							addSystemMessage("new null");
						fxMeasurementsListView.getSelectionModel().select("CURRENT MEASUREMENT");
					} else {
						if(newValue.equals("CURRENT MEASUREMENT")) {
							setMeasurementControlsDisable(false);
							addSystemMessage("Switched to active measurement");
						} else {
							setMeasurementControlsDisable(true);
							addSystemMessage("Loaded measurement " + newValue);
						}

						if(oldValue != null) {
							if(oldValue.equals("CURRENT MEASUREMENT")) {
								saveCurrentMeasurementToWorkspace("CURRENT MEASUREMENT");
							}
						}

						ImpedanceCurveMeasurement loadedMeas = loadMeasurementDataFromFile(newValue);						
						loadMeasurementDataIntoGUI(loadedMeas);	

					}
				}
			}
		});

		// TODO - who knows what...

		addSystemMessage("Ready");
	}

	private void setMeasurementControlsDisable(boolean disableFlag) {
		fxMeasureBtn.setDisable(disableFlag);
		fxFrequencyField.setEditable(!disableFlag);
		fxMeasCommentArea.setEditable(!disableFlag);;
		fxMeasNameField.setDisable(disableFlag);
		fxPrecisionRadio.setDisable(disableFlag);

		for(RadioButton btn : channelButtons) {
			btn.setDisable(disableFlag);
		}
	}
	
	public void setInterfaceInNonCommunicationModeDisable(boolean disableFlag) {
		setMeasurementControlsDisable(disableFlag);
	}
	
	// appends a new short system message to the message area
	private void addSystemMessage(String msg) {
		fxSystemMsgArea.appendText(">>>" + msg + "\n");
	}
	
	public void setControllerEnable(boolean enable) {
		controllerEnable = enable;
	}
	
	// update measurement list content with files present in the workspace
	private void updateMeasurementsList() {
		// scans measurement files from the workspace
		// automatically takes care for the current measurement
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
		
		if(newMeasurementListContent.contains("CURRENT MEASUREMENT") == false) {
			newMeasurementListContent.add("CURRENT MEASUREMENT");
			currentMeasurement = new ImpedanceCurveMeasurement();
		} else {
			currentMeasurement = loadMeasurementDataFromFile("CURRENT MEASUREMENT");
			loadMeasurementDataIntoGUI(currentMeasurement);
		}
		
		fxMeasurementsListView.setItems(newMeasurementListContent);
	}
	
	// informs the controller that the current measurement is finished
	public void reportMeasurementFinished() {
		addSystemMessage("Measurement DONE");
		setGUIDisable(false);
	}
	
	// get selected channel as a number
	private int getSelectedChannel() {
		
		String selectedButtonName = "";
		
		for(RadioButton btn : channelButtons) {
			if(btn.isSelected()) {
				selectedButtonName = btn.getText();
			}
		}
		
		int channel = 1;
		
		switch(selectedButtonName) {
		case "CHANNEL 1":
		{
			channel = 1;
			break;
		}
		case "CHANNEL 2":
		{
			channel = 2;
			break;
		}
		case "CHANNEL 3":
		{
			channel = 3;
			break;
		}
		case "CHANNEL 4":
		{
			channel = 4;
			break;
		}
		case "CHANNEL 5":
		{
			channel = 5;
			break;
		}
		case "CHANNEL 6":
		{
			channel = 6;
			break;
		}
		case "CHANNEL 7":
		{
			channel = 7;
			break;
		}
		case "CHANNEL 8":
		{
			channel = 8;
			break;
		}
		default:
		{
			channel = 1;
			break;
		}}
		
		return channel;
	}
	
	private static String reformat_mHzToHz(String dataIn_mHz) {
		
		Double dat = Double.valueOf(dataIn_mHz);
		
		return String.valueOf(dat / 1000.0);
	}
	
	// save "currentMeasurement" to workspace
	public void saveCurrentMeasurementToWorkspace(String measurementName) {
		
		File workspaceDirectory = new File(AppComponentsBank.getAppKernel().getDefaultImpedanceCurveDataLocation());
		
		String fullMeasurementName = measurementName + ".mobi_meas";
		String fullCurvesFileName = "curves.csv";
		
		if(workspaceDirectory.getPath().trim().length() > 0) {
			
			File targetGeneralFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, fullMeasurementName).toFile();
			
			if(targetGeneralFile.getParentFile() != null) {
				targetGeneralFile.getParentFile().mkdirs();
			}
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(targetGeneralFile))) {
				
				// write header to file
				String header1 = "#" + " MOBI MEASUREMENT DATA ||| IMPEDANCE CURVE |||  TIME STAMP:  " + AppComponentsBank.getAppKernel().getCurrentTimeStamp();
				String header2 = "#" + " MEASUREMENT ID: " + measurementName;
				String header3 = "#";
				
				writer.write(header1);
				writer.newLine();
				writer.write(header2);
				writer.newLine();
				writer.write(header3);
				writer.newLine();

				// write comment
				writer.write("COMMENT");
				writer.newLine();
				String wrappedCmt = currentMeasurement.getComment();
				writer.write(wrappedCmt);
				writer.newLine();
				
				
				writer.write("#");
				
				// write parameters
				String paramHeader1 = "#" + "===================== MEASUREMENT FREQUENCIES SECTION =====================";
				String paramHeader2 = "#" + "\tUNITS: [Hz]";
				String paramHeader3 = "#";
				
				writer.write(paramHeader1);
				writer.newLine();
				writer.write(paramHeader2);
				writer.newLine();
				writer.write(paramHeader3);
				writer.newLine();
				
				writer.write("# FREQUENCIES");
				writer.newLine();
				for(String freq : currentMeasurement.getFrequencyAxis()) {
					writer.write("# " + freq);
					writer.newLine();
				}
				
				writer.write("#");
				writer.newLine();
				
				// write measurement curves params
				String curvesHeader1 = "#" + "===================== MEASUREMENT CURVES SECTION =====================";
				String curvesHeader2 = "#" + " Frequency curve points: " + String.valueOf(currentMeasurement.getFrequencyAxis().size());
				String curvesHeader3 = "#" + " Impedance [Re] points: " + String.valueOf(currentMeasurement.getImpedanceRealAxis().size());
				String curvesHeader4 = "#" + " Impedance [Im] points: " + String.valueOf(currentMeasurement.getImpedanceImaginaryAxis().size());
				String curvesHeader5 = "#" + " Impedance [Mag] points: " + String.valueOf(currentMeasurement.getImpedanceMagnitudeAxis().size());
				String curvesHeader6 = "#" + " Impedance [Ph] points: " + String.valueOf(currentMeasurement.getImpedancePhaseAxis().size());
				String curvesHeader7 = "#";
				
				writer.write(curvesHeader1);
				writer.newLine();
				writer.write(curvesHeader2);
				writer.newLine();
				writer.write(curvesHeader3);
				writer.newLine();
				writer.write(curvesHeader4);
				writer.newLine();
				writer.write(curvesHeader5);
				writer.newLine();
				writer.write(curvesHeader6);
				writer.newLine();
				writer.write(curvesHeader7);
				writer.newLine();
				
				writer.write("#EoF");
				writer.newLine();
				
				writer.close();
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			// curves file
			File targetCurvesFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, fullCurvesFileName).toFile();
			
			if(targetGeneralFile.getParentFile() != null) {
				targetGeneralFile.getParentFile().mkdirs();
			}
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(targetCurvesFile))) {
					
					// the first line
					writer.write("FREQUENCY" + "\t");
					writer.write("Z_REAL" + "\t");
					writer.write("Z_IMAGINARY" + "\t");
					writer.write("Z_MAGNITUDE" + "\t");
					writer.write("Z_PHASE");
					writer.newLine();
					
					// the data lines...
					for(int i = 0; i < currentMeasurement.getFrequencyAxis().size(); i++) {
						
						writer.write(currentMeasurement.getFrequencyAxis().get(i) + "\t");
						writer.write(currentMeasurement.getImpedanceRealAxis().get(i) + "\t");
						writer.write(currentMeasurement.getImpedanceImaginaryAxis().get(i) + "\t");
						writer.write(currentMeasurement.getImpedanceMagnitudeAxis().get(i) + "\t");
						writer.write(currentMeasurement.getImpedancePhaseAxis().get(i));
						
						writer.newLine();
					}
					
					writer.close();
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			// chart images
	        try {
	        	File targetChartFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, "nyquist.png").toFile();
	        	WritableImage snapShot = null;
		        snapShot = fxNyquistPlot.snapshot(null, snapShot);
				ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", targetChartFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        try {
	        	File targetChartFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, "bode_magnitude.png").toFile();
	        	WritableImage snapShot = null;
		        snapShot = fxBodeMagPlot.snapshot(null, snapShot);
				ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null), "png", targetChartFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        try {
	        	File targetChartFile = Paths.get(workspaceDirectory.getAbsolutePath(), measurementName, "bode_phase.png").toFile();
	        	WritableImage snapShot = null;
		        snapShot = fxBodePhPlot.snapshot(null, snapShot);
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
	
	// select one button and clear the rest
	private void handleChannelButtonAction(String sourceButtonName) {
		for(RadioButton btn : channelButtons) {
			btn.setSelected(false);
			
			if(btn.getText().equals(sourceButtonName)) {
				btn.setSelected(true);
			}
		}		
	}
	
	// load and return a container file
	public static ImpedanceCurveMeasurement loadMeasurementDataFromFile(String measurementPackageName) {

		ImpedanceCurveMeasurement loadedMeasurement = new ImpedanceCurveMeasurement();

		String mainMeasurementDatabasePath = AppComponentsBank.getAppKernel().getDefaultImpedanceCurveDataLocation();
		File workspaceDirectory = new File(mainMeasurementDatabasePath);

		try(DirectoryStream<Path> ds = Files.newDirectoryStream(workspaceDirectory.toPath(), dirFilter)) {

			Path particularMeasPath = null;

			for(Path measPath : ds) {

				if(measPath.getFileName().toString().equals(measurementPackageName)) {
					particularMeasPath = measPath;
				}
			}

			if(particularMeasPath != null) {

				Path pathToParamsFile = Paths.get(particularMeasPath.toString(), measurementPackageName + ".mobi_meas");
				Path pathToCurveFiles = Paths.get(particularMeasPath.toString(), "curves.csv");

				File paramsFile = pathToParamsFile.toFile();
				File curvesFile = pathToCurveFiles.toFile();

				String comment = "";

				// mobi_meas file
				try(BufferedReader reader = new BufferedReader(new FileReader(paramsFile))) {

					String loadingContext = "";

					while(reader.ready()) {
						String line = reader.readLine();

						// check if the line contains anything at all and is not a comment
						if((line.contains("#") == false) && (line.trim().length() > 0)) {

							if(line.contains("COMMENT")) {

							} else {
								comment += line;
							}
						}
					}

					reader.close();
				} catch(IOException e) {
//					Alert alert = new Alert(AlertType.ERROR);
//					alert.setTitle("Measurement loading error");
//					alert.setHeaderText("Unable to read .mobi_meas file");
//
//					alert.showAndWait();
				}

				// curves file
				try(BufferedReader reader = new BufferedReader(new FileReader(curvesFile))) {

					String header = reader.readLine(); // quite useless 

					while(reader.ready()) {

						String line = reader.readLine();

						if(line.trim().equals("") == false) {

							String[] data = line.trim().split("\t");

							String frequency = data[0].trim();
							String Zre = data[1].trim();
							String Zim = data[2].trim();
							String Zmag = data[3].trim();
							String Zph = data[4].trim();

							loadedMeasurement.addPoint(frequency, Zre, Zim, Zmag, Zph);
						}
					}

					reader.close();

				} catch(IOException e) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Measurement loading error");
					alert.setHeaderText("Unable to read the curves file");

					alert.showAndWait();
				}

				loadedMeasurement.setComment(comment);

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
	
	private String roundStringNumber(String num) {
		DecimalFormat df = new DecimalFormat("#.#");
		
		return String.valueOf(df.format(Double.valueOf(num)));
	}
	
	// clear current data and display the set passed in the argument
	private void loadMeasurementDataIntoGUI(ImpedanceCurveMeasurement measurement) {
		
		// charts
		updateNyquistPlot(measurement.getNyquistChartSeries(""));
		updateBodePlots(measurement.getBodeMagnitudeChartSeries(""), measurement.getBodePhaseChartSeries(""));
		
		// comment
		fxMeasCommentArea.clear();
		fxMeasCommentArea.setText(measurement.getComment());
		
		// frequencies		
		StringJoiner joiner = new StringJoiner(" ; ");
		
		for(String f : measurement.getFrequencyAxis()) {
			joiner.add(roundStringNumber(f));
		}
		
		fxFrequencyField.setText(joiner.toString());
	}
	
	// enable or disable a GUI
	private void setGUIDisable(boolean disableFlag) {
		
		if(disableFlag == true) {
			AppComponentsBank.getControllerContextButtons().lockButtons();
		} else {
			AppComponentsBank.getControllerContextButtons().unlockButtons();
		}

		fxMeasureBtn.setDisable(disableFlag);
		fxGoToCompareBtn.setDisable(disableFlag);
		fxDeleteMeasBtn.setDisable(disableFlag);
		fxSaveMeasBtn.setDisable(disableFlag);
		fxWorkspaceBtn.setDisable(disableFlag);
		fxFrequencyField.setDisable(disableFlag);
		fxMeasNameField.setDisable(disableFlag);
		fxPrecisionRadio.setDisable(disableFlag);
		
		for(RadioButton btn : channelButtons) {
			btn.setDisable(disableFlag);
		}
		
		fxMeasurementsListView.setDisable(disableFlag);
	}
	
	// load single data point into the controller
	public void passSinglePointMeasurementData(String frequency, String impReal, String impImag, 
			String impMag, String impPhase) {
		
		if(controllerEnable) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					int freqInHz = (int) Math.round(Double.valueOf(frequency) / 1000.0);

					addSystemMessage("Measurement point: " + String.valueOf(freqInHz) + "[Hz]");
					addSystemMessage("RE: " + impReal + " | " + "IM: " + impImag + " | " + "MAG: " + 
							impMag + " | "  + "PH: " + impPhase);
					
					currentMeasurement.addPoint(reformat_mHzToHz(frequency), impReal, impImag, impMag, impPhase);
					
					updateNyquistPlot(currentMeasurement.getNyquistChartSeries(""));
					updateBodePlots(currentMeasurement.getBodeMagnitudeChartSeries(""), currentMeasurement.getBodePhaseChartSeries(""));
				}
			});
		}
	}
	
	// methods for updating charts
	private void updateNyquistPlot(XYChart.Series<Number, Number> newData) {
		fxNyquistPlot.getData().clear();
		fxNyquistPlot.getData().add(newData);
	}
	
	private void updateBodePlots(XYChart.Series<Number, Number> newMagnitudeData, XYChart.Series<Number, Number> newPhaseData) {
		fxBodeMagPlot.getData().clear();
		fxBodePhPlot.getData().clear();
		
		fxBodeMagPlot.getData().add(newMagnitudeData);
		fxBodePhPlot.getData().add(newPhaseData);
	}
	
	// method for reseting the GUI
	private void resetGUI() {
		fxBodeMagPlot.getData().clear();
		fxBodePhPlot.getData().clear();
		fxNyquistPlot.getData().clear();
	}
	
	// GUI handlers
	
	@FXML
	public void fxMeasureBtn(ActionEvent event) {
		// reset GUI and the current measurement variable
		currentMeasurement = new ImpedanceCurveMeasurement();		
		resetGUI();
		setGUIDisable(true);
		
		// do the measuring...
		int channel = getSelectedChannel();
		
		addSystemMessage("Measurement in progress. Channel:  " + String.valueOf(channel));
		
		AppComponentsBank.getAppKernel().setDefaultFrequenciesForImpedanceCurve(fxFrequencyField.getText());
		
		// create an object resposible for conducting the measurement
		int measChannel = getSelectedChannel();
		
		String freqsAsStr = fxFrequencyField.getText();
		freqsAsStr = freqsAsStr.replaceAll(",", ".");
		String[] freqSplit = freqsAsStr.split(";");
		
		LinkedList<Double> freqList = new LinkedList<>();
		
		try {
			for(String f : freqSplit) {
				Double fAsDouble = Double.valueOf(f);
				freqList.add(fAsDouble);
			}
		} catch(Exception e) {
			addSystemMessage("Wrong frequency values! Check the syntax.");
		}
		
		measManager.newMeasurementInstruction(measChannel, freqList, fxPrecisionRadio.isSelected());		
		measManager.startMeasurement();
	}
	
	@FXML
	public void fxGoToCompareButton(ActionEvent event) {
		addSystemMessage("Results compare tool activated");
		
		AppComponentsBank.getCurveCompareController().activateWindow();
	}
	
	@FXML
	public void fxDeleteMeasBtn(ActionEvent event) {
		
		String measurement = fxMeasurementsListView.getSelectionModel().getSelectedItem();
		
		if(measurement.equals("CURRENT MEASUREMENT")) {
			addSystemMessage("Selected measurement can not be deleted!");
		} else {
			String mainMeasurementDatabasePath = AppComponentsBank.getAppKernel().getDefaultImpedanceCurveDataLocation();
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
		
		updateMeasurementsList();
		
		fxMeasurementsListView.getSelectionModel().select("CURRENT MEASUREMENT");
		ImpedanceCurveMeasurement curMeas = loadMeasurementDataFromFile("CURRENT MEASUREMENT");
		loadMeasurementDataIntoGUI(curMeas);
		
		addSystemMessage("Measurement deleted:   " + measurement);
	}
	
	@FXML
	public void fxSaveMeasBtn(ActionEvent event) {
		
		currentMeasurement.setComment(fxMeasCommentArea.getText());
		
		saveCurrentMeasurementToWorkspace(fxMeasNameField.getText());
		saveCurrentMeasurementToWorkspace("CURRENT MEASUREMENT");
		
		updateMeasurementsList();
		
		addSystemMessage("Measurement '" + fxMeasNameField.getText() + "' saved");
	}
	
	@FXML
	public void fxWorkspaceBtn(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(AppComponentsBank.getMainStage());
		
		if(selectedDirectory != null) {
			AppComponentsBank.getAppKernel().setDefaultImpedanceCurveDataLocation(selectedDirectory.getAbsolutePath());
			fxWorkspaceAddressField.setText(AppComponentsBank.getAppKernel().getDefaultImpedanceCurveDataLocation());
		}		
	}
	
	@FXML
	public void fxChannel1Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 1");
	}
	
	@FXML
	public void fxChannel2Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 2");
	}
	
	@FXML
	public void fxChannel3Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 3");
	}
	
	@FXML
	public void fxChannel4Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 4");
	}
	
	@FXML
	public void fxChannel5Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 5");
	}
	
	@FXML
	public void fxChannel6Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 6");
	}
	
	@FXML
	public void fxChannel7Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 7");
	}
	
	@FXML
	public void fxChannel8Radio(ActionEvent event) {
		handleChannelButtonAction("CHANNEL 8");
	}
	
}
