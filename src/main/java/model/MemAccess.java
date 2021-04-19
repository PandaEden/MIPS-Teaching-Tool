package model;

import org.jetbrains.annotations.NotNull;

import model.components.Component;
import model.components.DataMemory;
import model.components.RegisterBank;

import util.Convert;
import util.logs.ExecutionLog;
import util.validation.OperandsValidation;

public class MemAccess extends I_Type {
	/**{@link util.validation.OperandsValidation#I_TYPE_MEM_ACCESS}, Label needs to be assembled into IMM value*/
	public MemAccess (@NotNull String ins, int RT, @NotNull String label) throws IllegalArgumentException{
		super( OperandsValidation.I_TYPE_MEM_ACCESS, ins, 0, RT, label );
	}
	
	/**{@link util.validation.OperandsValidation#I_TYPE_RT_IMM_RS}*/
	public MemAccess (@NotNull String ins, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( OperandsValidation.I_TYPE_RT_IMM_RS, ins, RS, RT, IMM );
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog)
			throws IndexOutOfBoundsException, IllegalArgumentException{
		switch ( opcode ) {
			case "lw":
				lw( dataMem, regBank, executionLog );
				break;
			case "sw":
				sw( dataMem, regBank, executionLog );
				break;
		}
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
		int ADDRESS=calculateDataAddress( executionLog, values[0] );
		dataMem.writeData( ADDRESS, values[1] );
		regBank.write( null,null );
	}
	
	private Integer calculateDataAddress(ExecutionLog executionLog, int rsVal) {
		executionLog.append( "[IMMEDIATE: " + IMM + " === " + Convert.int2Hex( IMM ) + "]" );
		executionLog.append( "\tCalculating Address:" );
		//TODO
		// - Result MUST BE ADDRESS ALIGNED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// - Add to Err Log -> Throw IllegalStateException.
		// Change ExecutionLog input - to a general LOGS SuperType ??
		int result = Component.ALU( rsVal, IMM, "add", executionLog);
		executionLog.append( "\t[Result: " + result + " === " + Convert.int2Hex( result ) + "]" );
		return result;
	}
	
}
