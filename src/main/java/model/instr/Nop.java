package model.instr;

import org.jetbrains.annotations.NotNull;

import model.components.DataMemory;
import model.components.RegisterBank;

import util.logs.ErrorLog;
import util.logs.ExecutionLog;
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
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog) {
		//EXIT do nothing, - set NPC to null
		regBank.noAction( );
		dataMem.noAction( );
		NPC=null;
	}
}
