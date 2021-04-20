package model.instr;

import org.jetbrains.annotations.NotNull;

import model.components.DataMemory;
import model.components.RegisterBank;

import util.Convert;
import util.Util;
import util.logs.ExecutionLog;
import util.validation.InstructionValidation;

public class J_Type extends Instruction {
	
	/**{@link InstructionValidation#J_TYPE}*/
	public J_Type(@NotNull String opcode, int IMM) {
		super( Type.JUMP, InstructionValidation.J_TYPE, opcode, 0, 0, 0, IMM, null );
		if (!Util.notNullAndInRange( IMM, 0, 67108864))//Unsigned 26Bit
			throw new IllegalArgumentException("Immediate["+IMM+"] Not In Range!");
	}
	/**{@link InstructionValidation#J_TYPE}, Label needs to be assembled into IMM value*/
	public J_Type(@NotNull String opcode, @NotNull String label) {
		super( Type.JUMP, InstructionValidation.J_TYPE, opcode, 0, 0, 0, null, label );
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
