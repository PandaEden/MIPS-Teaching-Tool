package Util.Logs;

import java.util.ArrayList;

public class WarningsLog extends Logger{
	public WarningsLog(ArrayList<String> logs){
		super("Warnings", logs);
	}
	
	@Override
	public String toString(){
		return Color.formatColored(Color.YELLOW_ANSI,super.toString());
	}
}

