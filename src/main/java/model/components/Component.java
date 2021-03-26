package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import util.logs.ExecutionLog;

public class Component {
	
	/** If the control_signal is true, Input 0 is forwarded, Otherwise Input1 is forwarded. */
	@Nullable
	public static Integer Mutex(Integer input0, Integer input1, boolean control_signal){
		return control_signal?input0:input1;
	}
	
	/** Logs the message {@link util.logs.Logger#appendEx(String)} and returns result of adding the two inputs.
	 <p>If either input is null, addition is not attempted, null is forwarded
	 <p>And the message is not logged. */
	@Nullable
	public static Integer Adder(Integer input0, Integer input1,@NotNull String message, @NotNull ExecutionLog log){
		if ( input0==null || input1==null )
			return null;
		
		log.appendEx(message);
		return input0+input1;
	}
	
}
