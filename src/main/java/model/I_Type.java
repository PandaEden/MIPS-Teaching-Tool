package model;

import org.jetbrains.annotations.NotNull;

import model.components.Component;
import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Operands;

import util.Convert;
import util.logs.ExecutionLog;

public class I_Type extends Instruction {
	
	I_Type(String ins, Operands operands) {
		super( ins, operands );
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog)
			throws IndexOutOfBoundsException, IllegalArgumentException{
		switch ( ins ) {
			case "addi":
				addi( dataMem, regBank, executionLog );
				break;
			case "lw":
				lw( dataMem, regBank, executionLog );
				break;
			case "sw":
				sw( dataMem, regBank, executionLog );
				break;
		}
	}
	
	private void addi(DataMemory dataMem, RegisterBank regBank, ExecutionLog executionLog) {
		int rsVal=regBank.read( RS );
		
		executionLog.append( "[IMMEDIATE: " + IMM + "]" );
		executionLog.append( "\tCalculating Result:" );
		int rtVal=Component.ALU( rsVal, IMM, "add", executionLog);
		dataMem.noAction( );
		regBank.write( RT, rtVal );
	}
	
	private void lw(DataMemory dataMem, RegisterBank regBank, ExecutionLog executionLog)
			throws IndexOutOfBoundsException, IllegalArgumentException{
		int rsVal=regBank.read( RS );
		int ADDRESS=calculateDataAddress( executionLog, rsVal );
		int data=dataMem.readData( ADDRESS );
		regBank.write( RT, data );
	}
	
	private void sw(DataMemory dataMem, RegisterBank regBank, ExecutionLog executionLog)
			throws IndexOutOfBoundsException, IllegalArgumentException{
		int[] values = regBank.read( RS, RT );
		int rsVal=values[0];
		int rtVal=values[1];
		int ADDRESS=calculateDataAddress( executionLog, rsVal );
		dataMem.writeData( ADDRESS, rtVal );
		regBank.write( null,null );
	}
	
	private Integer calculateDataAddress(ExecutionLog executionLog, int rsVal) {
		executionLog.append( "[IMMEDIATE: " + IMM + " === " + Convert.int2Hex( IMM ) + "]" );
		executionLog.append( "\tCalculating Address:" );
		//TODO
		// - Result MUST BE ADDRESS ALIGNED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// - Add to Err Log -> Throw IllegalStateException.
		// Change ExecutionLog input - to a general LOGS SuperType ??
		return Component.ALU( rsVal, IMM, "add", executionLog);
	}
	
}
