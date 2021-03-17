package util.logs;

import java.util.ArrayList;

public class ErrorLog extends Logger {
	public ErrorLog(ArrayList<String> logs) {
		super( "Errors", logs );
	}
	
	@Override
	public String toString() {
		return Color.fmtColored( Color.ERR_LOG, super.toString( ) );
	}
	
}
