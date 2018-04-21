package kernel;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import jssc.SerialPortList;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortEvent;

public class CommunicationManager implements SerialPortEventListener {
	
	// ENUMS
	public enum ConnectResult {
		CONNECTED, CONNECT_ERROR
	}
	
	public enum DisconnectResult {
		DISCONNECTED, DISCONNECT_ERROR
	}
	
	// FILEDS
	private SerialPort sp = null;
	
	private ExecutorService executorService = null;
	private LinkedBlockingQueue<String> rxQueue = null;

	// CONSTRUCTOR
	public CommunicationManager() {
		executorService = Executors.newSingleThreadExecutor();
		rxQueue = new LinkedBlockingQueue<String>();
		
		// execute rx buffer handler
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				String buf = "";
				
				while(true) {					
					
					try {
						String data = rxQueue.poll(10, TimeUnit.MILLISECONDS);
						
						if(data != null) {
							buf += data;
							
							if(buf.indexOf("\r\n") > 0) {
								String command = buf.substring(0, buf.indexOf("\r\n") + 1);
								command = command.replaceAll("\r", "");
								command = command.replaceAll("\n", "");
								buf = buf.substring(buf.indexOf("\r\n") + 1);
								AppComponentsBank.getControllerCommunication().addToRxLog(command);
								AppComponentsBank.getAppKernel().getCommandDispatcher().notify(command);
							}
						}
						
					} catch (InterruptedException e) {						
						e.printStackTrace();
						AppComponentsBank.getControllerCommunication().addToRxLog("<system> --- RX QUEUE ERROR ---");
					}
				}				
			}			
		});		
	}
	
	// SERIAL PORT EVENT HANDLER
	@Override
	public void serialEvent(SerialPortEvent serialPortEvent) {
		if(serialPortEvent.isRXCHAR() && serialPortEvent.getEventValue() > 0) {
			
			try {
				rxQueue.add(sp.readString(serialPortEvent.getEventValue()));
			} catch (Exception e) {
				e.printStackTrace();
				AppComponentsBank.getControllerCommunication().addToRxLog("<system> --- CRITICAL RX ERROR ---");
			}
		}			
	}
	
	// METHODS

	// ===========================================================================
	
	private boolean validateRxData(String data) {
		if(data.matches("[0-9\\s-+]+")) {
			return true;
		} else {
			return false;
		}
	}
	
	// ===========================================================================
	
	/**
	 * Lists available COM ports
	 * @return LinkedList of COM port names
	 */
	public LinkedList<String> getComList() {
		String[] ports = SerialPortList.getPortNames();
		
		LinkedList<String> coms = new LinkedList<String>();
		
		for(String s : ports) {
			coms.add(s);
		}		
	
		return coms;
	}
	
	// ===========================================================================
	
	/**
	 * Plugs into provided COM port
	 * @param com COM port name
	 * @return operation result
	 */
	public ConnectResult connectToCOM(String com) {
		try {
			
			// check if the SerialPort object 
			if(sp != null) {
				if(sp.isOpened())
					return ConnectResult.CONNECT_ERROR;
			}
				
			
			sp = new SerialPort(com);
			
			sp.openPort();
			
			sp.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			// make sure the connection is OK
			if(sp.isOpened()) {
				// add event listener do the serial port
				sp.addEventListener(this, SerialPort.MASK_RXCHAR);
				return ConnectResult.CONNECTED;
			}
			else {
				return ConnectResult.CONNECT_ERROR;
			}			
			
		} catch(Exception e) {
			return ConnectResult.CONNECT_ERROR;
		}
	}
	
	// ===========================================================================
	
	/**
	 * Disconnects from the current COM port
	 * @return operation result
	 */
	public DisconnectResult disconnectFromCOM() {
		try {
			sp.closePort();
			return DisconnectResult.DISCONNECTED;
		} catch(Exception e) {
			return DisconnectResult.DISCONNECT_ERROR;
		}
	}
	
	// ===========================================================================
	
	/**
	 * Attempts to transmit a String through connected COM port.
	 * 
	 * @param aData					string for transmission
	 */
	public void sendString(String aData) {
		
		try {
			sp.writeString(aData);
			AppComponentsBank.getControllerCommunication().addToTxLog(aData);
		} catch (SerialPortException e) {
			AppComponentsBank.getControllerCommunication().addToTxLog("\nCRITICAL TX ERROR - TRANSMISSION FAILED\n");
			e.printStackTrace();
		}
	}
	
	// ===========================================================================
	
	/**
	 * Removes data from RxBuffer.
	 */
//	public void clearRxBuffer() {
//		rxBuffer.setLength(0);
//	}
	
	// ===========================================================================
	
	/**
	 * RxBuffer getter.
	 * 
	 * @return	reference to rxBuffer (<code>StringBuffer</code> object)
	 */
//	public StringBuffer getRxBuffer() {
//		return rxBuffer;
//	}
	
}
