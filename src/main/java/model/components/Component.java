package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import util.logs.ExecutionLog;

public class Component {
	
	/** Multiplexer::If the control_signal is true, Input 0 is forwarded, Otherwise Input1 is forwarded. */
	@Nullable
	public static Integer Mux (Integer input0, Integer input1, boolean control_signal){
		return control_signal?input0:input1;
	}
	
	/** Logs the message {@link util.logs.Logger#appendEx(String)} and returns result of adding the two inputs.
	 <p>If either input is null, addition is not attempted, null is forwarded
	 <p>And the message is not logged. */
	@Nullable
	public static Integer Adder(Integer input0, Integer input1, @NotNull String message, @NotNull ExecutionLog log){
		if ( input0==null || input1==null )
			return null;
		
		log.appendEx(message);
		return input0+input1;
	}
	
	/** Performs ALU operation based on ALU_OP,
	 <ui><li>NOP - no action</li><li>ADD</li><li>SUB</li>
	 <li>SLL - Shift Left Logical</li><li>SLT - Set On Less Than</li></ui>*/
	public static int ALU(@NotNull Integer input0, @NotNull Integer input1, @NotNull String ALU_OP, @NotNull ExecutionLog log){
		int output; // TODO remove null
		// BInvert is determined by bit[0], SUB/SLT
		
		String sign;
		switch (ALU_OP.toUpperCase()){
			case "NOP": // -- do nothing, for Nop & Exit
				return input0;
			case "ADD": // ADD
				output=input0 + input1; sign = " + ";
				break;
			case "SUB": // SUB
				output=input0 - input1; sign = " - ";
				break;
			case "SLL": //Shift Left Logical
				String biteStream = Integer.toBinaryString(input1);
				if (biteStream.length()>5)
					input1 = Integer.parseInt(biteStream.substring(biteStream.length()-5), 2);
				output=input0 << input1; sign = " << ";
				break;
			case "SLT": //Set Less Than
				output=input0 < input1?1:0; sign = " set-on < ";
				break;
			default:
				throw new IllegalStateException("ALU_OP ["+ALU_OP+"] Not Implemented!");
		}
		log.append( "\tResult = " + input0 + sign + input1 + " ==> " + output );
		return output;
	}
	
}
