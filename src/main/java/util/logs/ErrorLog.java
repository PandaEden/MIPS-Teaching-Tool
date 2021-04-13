package util.logs;

import util.ansi_codes.Color;

import java.util.ArrayList;

public class ErrorLog extends Logger {
	public ErrorLog(ArrayList<String> logs) {
		super( Color.fmt( Color.ERR_LOG, "Errors"), logs );
	}
}
