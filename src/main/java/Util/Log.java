package Util;

/**
 Provides logging capacity for the application.
 */
public class Log{
	
	/**
	 Adds the message to the ErrorsLog, null will be replaces with an empty line "".
	 */
	public void pushError(String msg){
	
	}
	
	/**
	 Adds the message to the WarningsLog, null will be replaces with an empty line "".
	 */
	public void pushWarning(String msg){
	
	}
	
	/**
	 @return a String representing the state of the {@link #ErrorsLog}.
	 */
	public String formatErrors(){
		return null;
	}
	
	/**
	 @return a String representing the state of the {@link #WarningsLog}.
	 */
	public String formatWarnings(){
		return null;
	}
	
	/**
	 @return a String representing the state of the {@link #ErrorsLog}, and {@link #WarningsLog}.
	 */
	public String formatLogs(){
		return null;
	}
	
	/**
	 Clears the {@link #ErrorsLog}.
	 */
	public void clearErrors(){
	
	}
	
	/**
	 Clears the {@link #WarningsLog}
	 */
	public void clearWarnings(){
	
	}
	
	/**
	 Clears both the {@link #ErrorsLog}, and {@link #WarningsLog}
	 */
	public void resetLogs(){
	
	}
	
	/**
	 @return whether {@link #ErrorsLog} contains at least 1 entry.
	 */
	public boolean hasErrors(){
		return false;
	}
	
	/**
	 @return whether {@link #WarningsLog} contains an entry, AND {@link #ErrorsLog} is empty.
	 */
	public boolean hasWarningsOnly(){
		return false;
	}
}
