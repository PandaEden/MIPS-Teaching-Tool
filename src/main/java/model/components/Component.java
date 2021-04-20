package model.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import model.instr.Instruction;

import util.logs.ExecutionLog;

import java.util.Map;

public class Component {
	
	public static Map<Integer,String> ALU_codes =Map.of(-1,"NOP", 0,"SLL",2,"ADD",6, "SUB",7, "SLT");
	public static Integer searchALUCode(@Nullable String Op){
		if ( Op==null )
			Op="NOP";
		for ( Map.Entry<Integer,String> e: ALU_codes.entrySet() )
			if ( e.getValue().equals( Op.toUpperCase() ) )
				return e.getKey();
		throw new IllegalArgumentException("ALU Code for Op["+Op+"], not found!");
	}
	
	/** Integer Multiplexer::Forwards the index matching the control signal, Or null*/
	@Nullable
	public static Integer MUX(@Nullable Integer control_signal,Integer... input){
		if ( control_signal==null )
			return null;
		else if ( control_signal < input.length)
			return input[control_signal];
		else
			throw new IndexOutOfBoundsException("Control Signal["+control_signal+"] Invalid for Mux["+input+"]");
	}
	
	/** Logs the message {@link util.logs.Logger#appendEx(String)} and returns result of adding the two inputs.
	 <p>If either input is null, addition is not attempted, null is forwarded
	 <p>And the message is not logged. */
	@Nullable
	public static Integer ADDER(Integer input0, Integer input1, @NotNull String message, @NotNull ExecutionLog log){
		if ( input0==null || input1==null )
			return null;
		
		log.appendEx(message);
		return input0+input1;
	}
	
	/** Performs ALU operation based on ALU_OP,
	 <ui>
	   <li>null:[-1] - NOP - no action/output</li>
	   <li>000:[0] - SLL - Shift Left Logical</li>
	   <li>010:[2] - ADD - Addition</li>
	   <li>110:[3] - SUB - Subtraction (Binvert Addition)</li>
	   <li>111:[4] - SLT - Set On Less Than</li>
	 </ui>*/
	public static int ALU(@NotNull Integer input0, @NotNull Integer input1, Integer ALUCtrl, @NotNull ExecutionLog log){
		int output; // TODO remove null
		// BInvert is determined by bit[0], SUB/SLT
		
		String ALU_OP = ALU_codes.get( ALUCtrl==null?-1:ALUCtrl );
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
	
	/**
	 <ul><li>[0]Destination: 0-RT, 1-RD, 2-$RA</li>
	 <li>[1]ALUSrc1: 0-AIR1, 1-NPC</li>
	 <li>[2]ALUSrc2: 0-AIR2, 1-IMM</li>
	 <li>[3]ALUOpp: {@link #ALU_codes}, {@link #searchALUCode(String)}</li>
	 <li>[4]MemAction: 0-Read, 1-Write</li>
	 <li>[5]MemToReg: 0-AOR, 1-LMDR</li>
	 </ul>
	 */
	public static Integer[] DECODE(Instruction ins, ExecutionLog log){
		return new Integer[]{};
	}
	
}
