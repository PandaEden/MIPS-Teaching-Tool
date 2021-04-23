package model.instr;

import org.jetbrains.annotations.NotNull;

import util.logs.ErrorLog;
import util.validation.InstructionValidation;

import java.util.HashMap;

public class Nop extends Instruction {
	/**{@link InstructionValidation#NO_OPERANDS_OPCODE}*/
	public Nop (@NotNull String ins) {
		super( Type.NOP, InstructionValidation.NO_OPERANDS_OPCODE, ins, null, null, null, null, null );
	}
	@Override
	public boolean assemble (@NotNull ErrorLog log, @NotNull HashMap<String, Integer> labelMap) throws IllegalArgumentException {
		return true;
	}
}
