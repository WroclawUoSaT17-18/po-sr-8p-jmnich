package kernel;

import dispatcher.CmdListener;
import dispatcher.CommandDispatcher;

public class MaintenanceCommandsHandler implements CmdListener {

	public MaintenanceCommandsHandler(CommandDispatcher dispatcher) {
		dispatcher.subscribe(CMD.messageMobiGENERAL_STATUS, this);
		dispatcher.subscribe(CMD.messageMobiFREE_HEAP_SIZE, this);
		dispatcher.subscribe(CMD.messageMobiMINIMAL_EVER_FREE_HEAP_SIZE, this);
		dispatcher.subscribe(CMD.messageMobiERROR_DUMP, this);
		dispatcher.subscribe(CMD.messageMobiAUTODIAGNOSTICS_RESULTS, this);
	}
	
	@Override
	public void passCommand(String cmd) {
		int id = Integer.valueOf(cmd.split(" ")[0]);
		
		switch(id) {
		case CMD.messageMobiGENERAL_STATUS:
		{
			AppComponentsBank.getControllerMaintenance().addToInfoWindow("Received general status message");
			break;
		}
		case CMD.messageMobiFREE_HEAP_SIZE:
		{
			AppComponentsBank.getControllerMaintenance().refreshProperty("Free heap space", getCmdArgumentValue(cmd, 0));
			break;
		}
		case CMD.messageMobiMINIMAL_EVER_FREE_HEAP_SIZE:
		{
			AppComponentsBank.getControllerMaintenance().refreshProperty("Minimal ever free heap space", getCmdArgumentValue(cmd, 0));
			break;
		}
		case CMD.messageMobiERROR_DUMP:
		{
			// TODO
			break;
		}
		case CMD.messageMobiAUTODIAGNOSTICS_RESULTS:
		{
			// TODO
			break;
		}
		default:
			// ignore - can not happen
		}
	}
	
	private int getCmdArgumentValueAsInt(String cmd, int argumentNumber) {
		return Integer.valueOf(cmd.split(" ")[argumentNumber + 2]);
	}
	
	private String getCmdArgumentValue(String cmd, int argumentNumber) {
		return cmd.split(" ")[argumentNumber + 2];
	}

}
