package model;

import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Operands;
import org.jetbrains.annotations.NotNull;
import util.Convert;
import util.logs.ExecutionLog;

public class J_Type extends Instruction{
	
	/**
	 @param ins - NotNull use code 'no_ins' if not an instruction
	 */
	J_Type(String ins, Operands operands){
		super(ins, operands);
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog){
		final int RETURN_ADDRESS_REGISTER = 31;
		if (ins.equals("jal")) {
			executionLog.append("Storing Next Program Counter! : "+Convert.uInt2Hex(NPC));
			regBank.write(RETURN_ADDRESS_REGISTER, NPC);
		} else
			regBank.noAction();
		
		dataMem.noAction();
		NPC = Convert.imm2Address(IMM);
		executionLog.append("Returning Jump Address: "+Convert.uInt2Hex(NPC)+"!");
	}
}
