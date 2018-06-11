package kernel;

import java.util.LinkedList;
import java.util.StringJoiner;

import controllers.ControllerImpedanceCurve;
import dispatcher.CmdListener;
import dispatcher.CommandDispatcher;

public class CurveMeasurementManager implements CmdListener {
	
	LinkedList<Double> measurementFrequenciesList;			// [Hz] not [mHz] !!!
	int measurementChannel;
	LinkedList<String> mobiCommandsQueue;
	CommandDispatcher cmdDispatcher;
	
	boolean precisionModeFlag = false;
	
	int avgSamples = 5;
	
	LinkedList<String> bufRE;
	LinkedList<String> bufIM;
	LinkedList<String> bufPH;
	LinkedList<String> bufMAG;
	
	public CurveMeasurementManager(CommandDispatcher dispatcher) {		
		cmdDispatcher = dispatcher;
		
		bufRE = new LinkedList<>();
		bufIM = new LinkedList<>();
		bufPH = new LinkedList<>();
		bufMAG = new LinkedList<>();
		
		dispatcher.subscribe(CMD.messageMobiSINGLE_FREQ_FULL_AUTO_IMPEDANCE_RESULTS, this);
	}
	
	@Override
	public void passCommand(String cmd) {
		
		int id = Integer.valueOf(cmd.split(" ")[0]);
		
		if(id != CMD.messageMobiSINGLE_FREQ_FULL_AUTO_IMPEDANCE_RESULTS) {
			// should never happen but ignore it if it does
			return;
		}
		
		String[] dataSplit = cmd.split(" ");
		
		String f = dataSplit[3];
		String zre = dataSplit[4];
		String zim = dataSplit[5];
		String zmag = dataSplit[6];
		String zph = dataSplit[7];
		
		// pass the new data to GUI or do averaging
		if(precisionModeFlag) {
			
			bufRE.add(zre);
			bufIM.add(zim);
			bufMAG.add(zmag);
			bufPH.add(zph);
			
			if(bufRE.size() >= avgSamples) {
				
				String procZRe = processBuffer(bufRE);
				String procZIm = processBuffer(bufIM);
				String procZMag = processBuffer(bufMAG);
				String procZPh = processBuffer(bufPH);
				
				AppComponentsBank.getControllerImpCurve().passSinglePointMeasurementData(f, procZRe, 
						procZIm, procZMag, procZPh);
				
				bufRE.clear();
				bufIM.clear();
				bufMAG.clear();
				bufPH.clear();
			}
			
		} else {
			AppComponentsBank.getControllerImpCurve().passSinglePointMeasurementData(dataSplit[3], dataSplit[4], 
					dataSplit[5], dataSplit[6], dataSplit[7]);
		}
		
		// transmit next command after a short delay
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Curve measurement manager - thread sleep interrupted");
		}
		
		transmitNextCommand();
	}	

	private String processBuffer(LinkedList<String> buf) {
		
		// find and reject the worst reading and then do averaging
		
		Double sum = 0.0;
		Double mean = 0.0;
		Double maxErr = 0.0;
		Double worstValue = 0.0;
		
		for(String val : buf) {
			sum += Double.valueOf(val);
		}
		
		mean = sum / avgSamples;
		
		for(String val : buf) {
			Double v = Double.valueOf(val);
			
			Double err = Math.abs(v - mean);
			
			if(err > maxErr) {
				maxErr = err;
				worstValue = v;
			}
		}
		
		sum = sum - worstValue;
		
		return String.valueOf(sum / (avgSamples - 1));
	}
	
	public void newMeasurementInstruction(int channel, LinkedList<Double> frequencies, boolean precisionMode) {
		measurementChannel = channel;
		measurementFrequenciesList = frequencies;	
		
		bufRE.clear();
		bufIM.clear();
		bufMAG.clear();
		bufPH.clear();
		
		mobiCommandsQueue = assembleCommands(channel, frequencies, precisionMode);
		precisionModeFlag = precisionMode;
	}
	
	public void startMeasurement() {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Curve measurement manager - thread sleep interrupted");
		}
		
		transmitNextCommand();
	}
	
	private LinkedList<String> assembleCommands(int channel, LinkedList<Double> frequencies, boolean precisionMode) {
		
		if(channel < 1) {
			channel = 1;
		}
		else if(channel > 8) {
			channel = 8;
		}
		
		String channelAsStr = String.valueOf(channel);
		
		LinkedList<String> commandsQueue = new LinkedList<>();
		
		for(Double freq : frequencies) {
			
			String cmdID = "106";
			String argCnt = "2";
			String freqAsStr = String.valueOf(Math.round(freq * 1000.0));
			
			StringJoiner joiner = new StringJoiner(" ");
			joiner.add(cmdID);
			joiner.add(argCnt);
			joiner.add(channelAsStr);
			joiner.add(freqAsStr);
			
			String commandFull = joiner.toString();
			
			if(precisionMode) {
				for(int i = 0; i < avgSamples; i++) {
					commandsQueue.add(commandFull);
				}				
			} else {
				commandsQueue.add(commandFull);
			}			
		}
		
		return commandsQueue;
	}
	
	private void transmitNextCommand() {
		
		if(mobiCommandsQueue.size() > 0) {
			AppComponentsBank.getAppKernel().requestCommandTransmission(mobiCommandsQueue.removeFirst());
			
		} else {
			AppComponentsBank.getControllerImpCurve().reportMeasurementFinished();			
		}
	}

}
