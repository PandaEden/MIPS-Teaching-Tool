package model;

import org.jetbrains.annotations.NotNull;

import model.components.DataMemory;
import model.components.RegisterBank;

import util.Convert;
import util.logs.ExecutionLog;
import util.validation.OperandsValidation;

public class J_Type extends Instruction {
	
	/**{@link util.validation.OperandsValidation#J_TYPE}*/
	public J_Type(@NotNull String ins, int IMM) {
		super( Type.JUMP, OperandsValidation.J_TYPE, ins, 0, 0, 0, IMM, null );
		if (!OperandsValidation.notNullAndInRange(IMM, 0, 67108864))//Unsigned 26Bit
			throw new IllegalArgumentException("Immediate["+IMM+"] Not In Range!");
	}
	/**{@link util.validation.OperandsValidation#J_TYPE}, Label needs to be assembled into IMM value*/
	public J_Type(@NotNull String ins, @NotNull String label) {
		super( Type.JUMP, OperandsValidation.J_TYPE, ins, 0, 0, 0, null, label );
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog) {
		final int RETURN_ADDRESS_REGISTER=31;
		
		if ( opcode.equals( "jal" ) ) {
			regBank.read( null, null );
			executionLog.append( "Storing Next Program Counter! : " + Convert.int2Hex( NPC ) );
			regBank.write( RETURN_ADDRESS_REGISTER, NPC );
		} else
			regBank.noAction( );
		
		NPC = shiftImm(executionLog);
		dataMem.noAction( );
		executionLog.append( "Returning Jump Address: " + Convert.int2Hex( NPC ) + "!" );
	}
	
}
