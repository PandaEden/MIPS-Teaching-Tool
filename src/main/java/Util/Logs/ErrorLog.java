package Util.Logs;

import java.util.ArrayList;

public class ErrorLog extends Logger{
	public ErrorLog(ArrayList<String> logs){
		super("Error", logs);
	}
	
	@Override
	public String toString(){
		return Color.formatColored(Color.RED_ANSI,super.toString());
	}
}