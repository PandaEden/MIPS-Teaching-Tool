package util.logs;

import java.util.ArrayList;

public class ExecutionLog extends Logger {
	public ExecutionLog(ArrayList<String> logs) {
		super( "Execution", logs );
	}
	
	public String toStringAndClear(){
		String temp = toString();
		clear();
		return temp;
	}
	
	// TODO Add Line Information
}
