package util.logs;

import util.ansi_codes.Color;

import java.util.ArrayList;

public class ErrorLog extends Logger {
	public ErrorLog(ArrayList<String> logs) {
		super( "Errors", logs );
	}
	
	@Override
	public String toString ( ) {
		String temp = super.name;
		super.name=Color.fmt(Color.ERR_LOG, super.name );
		String out = super.toString();
		super.name=temp;
		
		return out;
	}
}
