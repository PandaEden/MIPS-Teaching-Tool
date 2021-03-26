package model.components;

import org.jetbrains.annotations.Nullable;

public class Component {
	
	/** If the control_signal is true, Input 0 is forwarded, Otherwise Input1 is forwarded. */
	@Nullable
	public static Integer Mutex(Integer input0, Integer input1, boolean control_signal){
		return control_signal?input0:input1;
	}
	
}
