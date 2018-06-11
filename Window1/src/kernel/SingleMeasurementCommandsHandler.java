package kernel;

import java.util.Arrays;
import java.util.LinkedList;

import dispatcher.CmdListener;
import dispatcher.CommandDispatcher;

public class SingleMeasurementCommandsHandler implements CmdListener {

	// COMMANDS
	private final static String cmd100 = "<100> IMP MAN RAW";
	private final static String cmd101 = "<101> IMP AUTO RAW";
	private final static String cmd102 = "<102> ELEC CODE";
	private final static String cmd103 = "<103> MAN FIL";
	private final static String cmd104 = "<104> MAN IMP";
	private final static String cmd105 = "<105> AUT IMP";
	private final static String cmd106 = "<106> FULL AUT";
	
	public SingleMeasurementCommandsHandler(CommandDispatcher dispatcher) {
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_AUTO_IMPEDANCE_RESULTS, this);
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_FULL_AUTO_IMPEDANCE_RESULTS, this);
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_IMPEDANCE_AUTO_RESULTS, this);
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_IMPEDANCE_MANUAL_RAW__RESULTS, this);
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_MANUAL_FILTERED_RESULTS, this);
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_MANUAL_IMPEDANCE_RESULTS, this);
		
	}
	
	@Override
	public void passCommand(String cmd) {
		int id = Integer.valueOf(cmd.split(" ")[0]);
		
		switch(id) {
		case CMD.messageMobiSINGLE_FREQ_AUTO_IMPEDANCE_RESULTS:
		{
			String[] splitCmd = cmd.split(" ");
			String originalRequestID = getRequestIDForResponseID(splitCmd[0]);
			
			// parameters
			LinkedList<String> extractedParameters = extractParameters(13, splitCmd);
			
			LinkedList<MeasurementParameter> parsedParams = new LinkedList<>();
			parsedParams.add(new MeasurementParameter("CHANNEL", extractedParameters.get(0)));
			parsedParams.add(new MeasurementParameter("FREQUENCY", extractedParameters.get(1)));
			parsedParams.add(new MeasurementParameter("PEAK-PEAK", extractedParameters.get(2)));
			parsedParams.add(new MeasurementParameter("MAIN OFFSET", extractedParameters.get(3)));
			parsedParams.add(new MeasurementParameter("TIA RESISTANCE", extractedParameters.get(4)));
			parsedParams.add(new MeasurementParameter("CURRENT OFFSET", extractedParameters.get(5)));
			parsedParams.add(new MeasurementParameter("CURRENT GAIN RESISTOR", extractedParameters.get(6)));
			parsedParams.add(new MeasurementParameter("VOLTAGE GAIN RESISTOR", extractedParameters.get(7)));
			parsedParams.add(new MeasurementParameter("PRE-MEASUREMENT DELAY", extractedParameters.get(8)));
			parsedParams.add(new MeasurementParameter("POINTS PER PERIOD", extractedParameters.get(9)));
			parsedParams.add(new MeasurementParameter("ELECTRODE CODE", extractedParameters.get(10)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-REAL", extractedParameters.get(11)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-IMAGINARY", extractedParameters.get(12)));
			
			// curves
			MeasurementCurve rawVoltageCurve = new MeasurementCurve("RAW VOLTAGE");
			MeasurementCurve rawCurrentCurve = new MeasurementCurve("RAW CURRENT");
			MeasurementCurve processedVoltageCurve = new MeasurementCurve("PROCESSED VOLTAGE");
			MeasurementCurve processedCurrentCurve = new MeasurementCurve("PROCESSED CURRENT");
			
			
			LinkedList<String> extractedCurves = extractCurveData(13, splitCmd);
			
			for(int i = 0 ; i < extractedCurves.size(); i += 4) {
				rawVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i)));
				processedVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i+1)));
				rawCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+2)));
				processedCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+3)));
			}
			
			LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();
			LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
			
			voltageCurves.add(rawVoltageCurve);
			voltageCurves.add(processedVoltageCurve);
			currentCurves.add(rawCurrentCurve);
			currentCurves.add(processedCurrentCurve);
			
			AppComponentsBank.getControllerImpSingle().passMeasurementData(originalRequestID, parsedParams, voltageCurves, currentCurves);
			
			break;
		}
		case CMD.messageMobiSINGLE_FREQ_FULL_AUTO_IMPEDANCE_RESULTS:
		{
			String[] splitCmd = cmd.split(" ");
			String originalRequestID = getRequestIDForResponseID(splitCmd[0]);
			
			// parameters
			LinkedList<String> extractedParameters = extractParameters(6, splitCmd);
			
			LinkedList<MeasurementParameter> parsedParams = new LinkedList<>();
			parsedParams.add(new MeasurementParameter("CHANNEL", extractedParameters.get(0)));
			parsedParams.add(new MeasurementParameter("FREQUENCY", extractedParameters.get(1)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-REAL", extractedParameters.get(2)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-IMAGINARY", extractedParameters.get(3)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-MAGNITUDE", extractedParameters.get(4)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-PHASE[DEG]", extractedParameters.get(5)));			
				
			LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();
			LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
	
			AppComponentsBank.getControllerImpSingle().passMeasurementData(originalRequestID, parsedParams, voltageCurves, currentCurves);
			
			break;
		}
		case CMD.messageMobiSINGLE_FREQ_IMPEDANCE_AUTO_RESULTS:
		{
			String[] splitCmd = cmd.split(" ");
			String originalRequestID = getRequestIDForResponseID(splitCmd[0]);
			
			// parameters
			LinkedList<String> extractedParameters = extractParameters(11, splitCmd);
			
			LinkedList<MeasurementParameter> parsedParams = new LinkedList<>();
			parsedParams.add(new MeasurementParameter("CHANNEL", extractedParameters.get(0)));
			parsedParams.add(new MeasurementParameter("FREQUENCY", extractedParameters.get(1)));
			parsedParams.add(new MeasurementParameter("PEAK-PEAK", extractedParameters.get(2)));
			parsedParams.add(new MeasurementParameter("MAIN OFFSET", extractedParameters.get(3)));
			parsedParams.add(new MeasurementParameter("TIA RESISTANCE", extractedParameters.get(4)));
			parsedParams.add(new MeasurementParameter("CURRENT OFFSET", extractedParameters.get(5)));
			parsedParams.add(new MeasurementParameter("CURRENT GAIN RESISTOR", extractedParameters.get(6)));
			parsedParams.add(new MeasurementParameter("VOLTAGE GAIN RESISTOR", extractedParameters.get(7)));
			parsedParams.add(new MeasurementParameter("PRE-MEASUREMENT DELAY", extractedParameters.get(8)));
			parsedParams.add(new MeasurementParameter("POINTS PER PERIOD", extractedParameters.get(9)));
			parsedParams.add(new MeasurementParameter("ELECTRODE CODE", extractedParameters.get(10)));
			
			// curves
			MeasurementCurve rawVoltageCurve = new MeasurementCurve("RAW VOLTAGE");
			MeasurementCurve rawCurrentCurve = new MeasurementCurve("RAW CURRENT");
			MeasurementCurve processedVoltageCurve = new MeasurementCurve("PROCESSED VOLTAGE");
			MeasurementCurve processedCurrentCurve = new MeasurementCurve("PROCESSED CURRENT");
			
			
			LinkedList<String> extractedCurves = extractCurveData(11, splitCmd);
			
			for(int i = 0 ; i < extractedCurves.size(); i += 4) {
				rawVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i)));
				processedVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i+1)));
				rawCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+2)));
				processedCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+3)));
			}
			
			LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();
			LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
			
			voltageCurves.add(rawVoltageCurve);
			voltageCurves.add(processedVoltageCurve);
			currentCurves.add(rawCurrentCurve);
			currentCurves.add(processedCurrentCurve);
			
			AppComponentsBank.getControllerImpSingle().passMeasurementData(originalRequestID, parsedParams, voltageCurves, currentCurves);
			
			break;
		}
		case CMD.messageMobiSINGLE_FREQ_IMPEDANCE_MANUAL_RAW__RESULTS:
		{
			String[] splitCmd = cmd.split(" ");
			String originalRequestID = getRequestIDForResponseID(splitCmd[0]);
			
			// parameters
			LinkedList<String> extractedParameters = extractParameters(11, splitCmd);
			
			LinkedList<MeasurementParameter> parsedParams = new LinkedList<>();
			parsedParams.add(new MeasurementParameter("CHANNEL", extractedParameters.get(0)));
			parsedParams.add(new MeasurementParameter("FREQUENCY", extractedParameters.get(1)));
			parsedParams.add(new MeasurementParameter("PEAK-PEAK", extractedParameters.get(2)));
			parsedParams.add(new MeasurementParameter("MAIN OFFSET", extractedParameters.get(3)));
			parsedParams.add(new MeasurementParameter("TIA RESISTANCE", extractedParameters.get(4)));
			parsedParams.add(new MeasurementParameter("CURRENT OFFSET", extractedParameters.get(5)));
			parsedParams.add(new MeasurementParameter("CURRENT GAIN RESISTOR", extractedParameters.get(6)));
			parsedParams.add(new MeasurementParameter("VOLTAGE GAIN RESISTOR", extractedParameters.get(7)));
			parsedParams.add(new MeasurementParameter("PRE-MEASUREMENT DELAY", extractedParameters.get(8)));
			parsedParams.add(new MeasurementParameter("POINTS PER PERIOD", extractedParameters.get(9)));
			parsedParams.add(new MeasurementParameter("ELECTRODE CODE", extractedParameters.get(10)));
			
			// curves
			MeasurementCurve rawVoltageCurve = new MeasurementCurve("RAW VOLTAGE");
			MeasurementCurve rawCurrentCurve = new MeasurementCurve("RAW CURRENT");
			
			LinkedList<String> extractedCurves = extractCurveData(11, splitCmd);
			
			for(int i = 0 ; i < extractedCurves.size(); i += 2) {
				rawVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 2.0), extractedCurves.get(i)));
				rawCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 2.0), extractedCurves.get(i+1)));
			}
			
			LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();
			LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
			
			voltageCurves.add(rawVoltageCurve);
			currentCurves.add(rawCurrentCurve);
			
			AppComponentsBank.getControllerImpSingle().passMeasurementData(originalRequestID, parsedParams, voltageCurves, currentCurves);
			
			break;
		}
		case CMD.messageMobiSINGLE_FREQ_MANUAL_FILTERED_RESULTS: 
		{
			String[] splitCmd = cmd.split(" ");
			String originalRequestID = getRequestIDForResponseID(splitCmd[0]);
			
			// parameters
			LinkedList<String> extractedParameters = extractParameters(11, splitCmd);
			
			LinkedList<MeasurementParameter> parsedParams = new LinkedList<>();
			parsedParams.add(new MeasurementParameter("CHANNEL", extractedParameters.get(0)));
			parsedParams.add(new MeasurementParameter("FREQUENCY", extractedParameters.get(1)));
			parsedParams.add(new MeasurementParameter("PEAK-PEAK", extractedParameters.get(2)));
			parsedParams.add(new MeasurementParameter("MAIN OFFSET", extractedParameters.get(3)));
			parsedParams.add(new MeasurementParameter("TIA RESISTANCE", extractedParameters.get(4)));
			parsedParams.add(new MeasurementParameter("CURRENT OFFSET", extractedParameters.get(5)));
			parsedParams.add(new MeasurementParameter("CURRENT GAIN RESISTOR", extractedParameters.get(6)));
			parsedParams.add(new MeasurementParameter("VOLTAGE GAIN RESISTOR", extractedParameters.get(7)));
			parsedParams.add(new MeasurementParameter("PRE-MEASUREMENT DELAY", extractedParameters.get(8)));
			parsedParams.add(new MeasurementParameter("POINTS PER PERIOD", extractedParameters.get(9)));
			parsedParams.add(new MeasurementParameter("ELECTRODE CODE", extractedParameters.get(10)));
			
			// curves
			MeasurementCurve rawVoltageCurve = new MeasurementCurve("RAW VOLTAGE");
			MeasurementCurve rawCurrentCurve = new MeasurementCurve("RAW CURRENT");
			MeasurementCurve processedVoltageCurve = new MeasurementCurve("PROCESSED VOLTAGE");
			MeasurementCurve processedCurrentCurve = new MeasurementCurve("PROCESSED CURRENT");
			
			LinkedList<String> extractedCurves = extractCurveData(11, splitCmd);
			
			for(int i = 0 ; i < extractedCurves.size(); i += 4) {
				rawVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i)));
				processedVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i+1)));
				rawCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+2)));
				processedCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+3)));
			}
			
			LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();
			LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
			
			voltageCurves.add(rawVoltageCurve);
			voltageCurves.add(processedVoltageCurve);
			currentCurves.add(rawCurrentCurve);
			currentCurves.add(processedCurrentCurve);
			
			AppComponentsBank.getControllerImpSingle().passMeasurementData(originalRequestID, parsedParams, voltageCurves, currentCurves);
			
			
			break;
		}
		case CMD.messageMobiSINGLE_FREQ_MANUAL_IMPEDANCE_RESULTS:
		{
			String[] splitCmd = cmd.split(" ");
			String originalRequestID = getRequestIDForResponseID(splitCmd[0]);
			
			// parameters
			LinkedList<String> extractedParameters = extractParameters(13, splitCmd);
			
			LinkedList<MeasurementParameter> parsedParams = new LinkedList<>();
			parsedParams.add(new MeasurementParameter("CHANNEL", extractedParameters.get(0)));
			parsedParams.add(new MeasurementParameter("FREQUENCY", extractedParameters.get(1)));
			parsedParams.add(new MeasurementParameter("PEAK-PEAK", extractedParameters.get(2)));
			parsedParams.add(new MeasurementParameter("MAIN OFFSET", extractedParameters.get(3)));
			parsedParams.add(new MeasurementParameter("TIA RESISTANCE", extractedParameters.get(4)));
			parsedParams.add(new MeasurementParameter("CURRENT OFFSET", extractedParameters.get(5)));
			parsedParams.add(new MeasurementParameter("CURRENT GAIN RESISTOR", extractedParameters.get(6)));
			parsedParams.add(new MeasurementParameter("VOLTAGE GAIN RESISTOR", extractedParameters.get(7)));
			parsedParams.add(new MeasurementParameter("PRE-MEASUREMENT DELAY", extractedParameters.get(8)));
			parsedParams.add(new MeasurementParameter("POINTS PER PERIOD", extractedParameters.get(9)));
			parsedParams.add(new MeasurementParameter("ELECTRODE CODE", extractedParameters.get(10)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-REAL", extractedParameters.get(11)));
			parsedParams.add(new MeasurementParameter("IMPEDANCE-IMAGINARY", extractedParameters.get(12)));
			
			// curves
			MeasurementCurve rawVoltageCurve = new MeasurementCurve("RAW VOLTAGE");
			MeasurementCurve rawCurrentCurve = new MeasurementCurve("RAW CURRENT");
			MeasurementCurve processedVoltageCurve = new MeasurementCurve("PROCESSED VOLTAGE");
			MeasurementCurve processedCurrentCurve = new MeasurementCurve("PROCESSED CURRENT");
			
			LinkedList<String> extractedCurves = extractCurveData(13, splitCmd);
			
			for(int i = 0 ; i < extractedCurves.size(); i += 4) {
				rawVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i)));
				processedVoltageCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i  / 4.0), extractedCurves.get(i+1)));
				rawCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+2)));
				processedCurrentCurve.addPointToCurve(new MeasurementPoint(String.valueOf(i / 4.0), extractedCurves.get(i+3)));
			}
			
			LinkedList<MeasurementCurve> voltageCurves = new LinkedList<>();
			LinkedList<MeasurementCurve> currentCurves = new LinkedList<>();
			
			voltageCurves.add(rawVoltageCurve);
			voltageCurves.add(processedVoltageCurve);
			currentCurves.add(rawCurrentCurve);
			currentCurves.add(processedCurrentCurve);
			
			AppComponentsBank.getControllerImpSingle().passMeasurementData(originalRequestID, parsedParams, voltageCurves, currentCurves);
			
			break;
			
		}
		default:
			// ignore - can not happen
		}
	}
	
	private String getRequestIDForResponseID(String responseID) {
		
		String requestID = "";
		
		switch(responseID) {
		case "1100":
		{
			requestID = "100";
			break;
		}
		case "1101":
		{
			requestID = "101";
			break;
		}
		case "1102":
		{
			requestID = "102";
			break;
		}
		case "1103":
		{
			requestID = "103";
			break;
		}
		case "1104":
		{
			requestID = "104";
			break;
		}
		case "1105":
		{
			requestID = "105";
			break;
		}
		case "1106":
		{
			requestID = "106";
			break;
		}}
		
		return requestID;
	}
	
	private LinkedList<String> extractCurveData(int parametersCount, String[] splitCommand) {
		
		LinkedList<String> curveData = new LinkedList<>();
		
		// 1 and 2 positions in the command are ID and arg count so skip them
		LinkedList<String> dataAsList = new LinkedList<>(Arrays.asList(splitCommand));
		return new LinkedList<String>(dataAsList.subList(parametersCount + 2, dataAsList.size()));
	}
	
	private LinkedList<String> extractParameters(int parametersCount, String[] splitCommand) {
		
		LinkedList<String> paramList = new LinkedList<>();
		
		// 1 and 2 positions in the command are ID and arg count so skip them
		for(int i = 0; i < parametersCount; i++) {
			paramList.add(splitCommand[i + 2]);
		}
		
		return paramList;
	}
	
	private int getCmdArgumentValueAsInt(String cmd, int argumentNumber) {
		return Integer.valueOf(cmd.split(" ")[argumentNumber + 2]);
	}
	
	private String getCmdArgumentValue(String cmd, int argumentNumber) {
		return cmd.split(" ")[argumentNumber + 2];
	}
}
