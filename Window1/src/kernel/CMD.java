package kernel;

public class CMD {
	// DIRECTION: HOST -> MOBI
	// general maintenance
	public static final int messageHostGENERAL_STATUS_REQUEST 					= 0;
	public static final int messageHostFREE_HEAP_SIZE_REQUEST					= 1;
	public static final int messageHostMINIMAL_EVER_FREE_HEAP_SIZE_REQUEST		= 2;
	public static final int messageHostERROR_DUMP_REQUEST						= 3;
	public static final int messageHostRESET_ERROR_LOG							= 4;
	public static final int messageHostPERFORM_I2C_RESET_SEQUENCE				= 5;
	// measurement
	public static final int messageHostSINGLE_FREQ_IMPEDANCE_MANUAL_RAW			= 100;
	public static final int messageHostSINGLE_FREQ_IMPEDANCE_AUTO_RAW			= 101;
	public static final int messageHostELECTRODE_CODE_REQUEST					= 102;
	public static final int messageHostSINGLE_FREQ_MANUAL_FILTERED				= 103;
	public static final int messageHostSINGLE_FREQ_MANUAL_IMPEDANCE				= 104;
	public static final int messageHostSINGLE_FREQ_AUTO_IMPEDANCE				= 105;
	public static final int messageHostSINGLE_FREQ_FULL_AUTO_IMPEDANCE			= 106;
	// diagnostics
	public static final int messageHostRUN_AUTODIAGNOSTICS						= 200;
	
	// DIRECTION: MOBI -> HOST
	// general maintenance
	public static final int messageMobiGENERAL_STATUS							= 1000;
	public static final int messageMobiFREE_HEAP_SIZE							= 1001;
	public static final int messageMobiMINIMAL_EVER_FREE_HEAP_SIZE				= 1002;
	public static final int messageMobiERROR_DUMP								= 1003;
	public static final int messageMobiCOMMAND_CRC_CORRECT						= 1042;
	public static final int messageMobiCOMMAND_ID_UNKNOWN						= 1043;
	public static final int messageMobiCOMMAND_INTEGRITY_ERROR					= 1044;
	// measurement
	public static final int messageMobiSINGLE_FREQ_IMPEDANCE_MANUAL_RAW__RESULTS	= 1100;
	public static final int messageMobiSINGLE_FREQ_IMPEDANCE_AUTO_RESULTS			= 1101;
	public static final int messageMobiELECTRODE_CODE								= 1102;
	public static final int messageMobiSINGLE_FREQ_MANUAL_FILTERED_RESULTS			= 1103;
	public static final int messageMobiSINGLE_FREQ_MANUAL_IMPEDANCE_RESULTS			= 1104;
	public static final int messageMobiSINGLE_FREQ_AUTO_IMPEDANCE_RESULTS			= 1105;
	public static final int messageMobiSINGLE_FREQ_FULL_AUTO_IMPEDANCE_RESULTS		= 1106;
	// diagnostics
	public static final int messageMobiAUTODIAGNOSTICS_RESULTS					= 1200;
}
