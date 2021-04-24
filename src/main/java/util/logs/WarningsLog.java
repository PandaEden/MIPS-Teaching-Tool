package util.logs;

import util.ansi_codes.Color;

import java.util.ArrayList;

public class WarningsLog extends Logger {
	public WarningsLog(ArrayList<String> logs) {
		super( "Warnings", logs );
	}
	
	@Override
	public String toString() {
		return Color.fmt( Color.WARN_LOG, super.toString( ) );
	}
	
}
