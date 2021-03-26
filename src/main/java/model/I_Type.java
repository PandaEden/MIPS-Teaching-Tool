package model;

import org.jetbrains.annotations.NotNull;

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
		executionLog.append( "Calculating Result:" );
		int rtVal=rsVal + IMM;
		executionLog.append( "RT = RS+IMMEDIATE = " + rsVal + "+" + IMM + " ==> " + rtVal );
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
		int rsVal=regBank.read( RS );
		int data=regBank.read( RT );
		int ADDRESS=calculateDataAddress( executionLog, rsVal );
		dataMem.writeData( ADDRESS, data );
		regBank.noAction( );
	}
	
	private Integer calculateDataAddress(ExecutionLog executionLog, int rsVal) {
		executionLog.append( "[IMMEDIATE: " + IMM + " === " + Convert.int2Hex( IMM ) + "]" );
		executionLog.append( "Calculating Address:" );
		int ADDRESS = rsVal + IMM;    // Presumed the Immediate, or rsVal are already the full Address
		executionLog.append( "ADDRESS = RS+IMMEDIATE = " + rsVal + " + " + IMM + " = "
							 + ADDRESS + " ==> " + Convert.int2Hex( ADDRESS ) );
		//TODO
		// - Result MUST BE ADDRESS ALIGNED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// - Add to Err Log -> Throw IllegalStateException.
		// Change ExecutionLog input - to a general LOGS SuperType ??
		return ADDRESS;
	}
	
}
