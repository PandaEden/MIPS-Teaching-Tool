package model.instr;

import org.jetbrains.annotations.NotNull;

import util.Util;
import util.validation.InstructionValidation;

public class J_Type extends Instruction {
	
	/**{@link InstructionValidation#J_TYPE}*/
	public J_Type(@NotNull String opcode, int IMM) {
		super( Type.JUMP, InstructionValidation.J_TYPE, opcode, null, null, null, IMM, null );
		if (!Util.notNullAndInRange( IMM, 0, 67108864))//Unsigned 26Bit
			throw new IllegalArgumentException("Immediate["+IMM+"] Not In Range!");
	}
	/**{@link InstructionValidation#J_TYPE}, Label needs to be assembled into IMM value*/
	public J_Type(@NotNull String opcode, @NotNull String label) {
		super( Type.JUMP, InstructionValidation.J_TYPE, opcode, null, null, null, null, label );
	}
	
}
