package model.instr;

import org.jetbrains.annotations.NotNull;

import model.components.Component;
import model.components.DataMemory;
import model.components.RegisterBank;

import util.Util;
import util.logs.ExecutionLog;
import util.validation.InstructionValidation;

import java.util.List;

public class I_Type extends Instruction {
	
	/**{@link InstructionValidation#I_TYPE}*/
	protected I_Type (@NotNull List<String> codes, @NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{
		super( Type.IMMEDIATE, codes, opcode, RS, RT, null, IMM, null );
		
		if (!Util.notNullAndInRange( IMM, -32768, 32767))//Signed 16Bit
			throw new IllegalArgumentException("Immediate["+IMM+"] Not In Range!");
		super.regNotInRange_Register( RS );
		super.regNotInRange_Register( RT );
	}
	
	/**{@link InstructionValidation#I_TYPE}, Label needs to be assembled into IMM value*/
	protected I_Type (@NotNull List<String> codes, @NotNull String opcode, int RS, int RT, @NotNull String label) throws IllegalArgumentException{
		super( Type.IMMEDIATE, codes, opcode, RS, RT, null, null, label );
		if ( RS!=0 )
			throw new IllegalArgumentException("RS:["+RS+"], must be 0, when IMM is null");
		
		super.regNotInRange_Register( RS ); // redundant
		super.regNotInRange_Register( RT );
	}
	
	/**{@link InstructionValidation#I_TYPE_RT_RS_IMM}*/
	public I_Type (@NotNull String opcode, int RS, int RT, int IMM) throws IllegalArgumentException{	// Refactor to factory ?
		this( InstructionValidation.I_TYPE_RT_RS_IMM, opcode, RS, RT, IMM );
	}
	
	@Override
	protected void action(@NotNull DataMemory dataMem, @NotNull RegisterBank regBank,
						  @NotNull ExecutionLog executionLog)
			throws IndexOutOfBoundsException, IllegalArgumentException{
		if ( opcode.equals( "addi" ) ){
			int rsVal=regBank.read( RS ,null)[0];
			
			executionLog.append( "[IMMEDIATE: " + IMM + "]" );
			executionLog.append( "\tCalculating Result:" );
			int rtVal=Component.ALU( rsVal, IMM, 2, executionLog);
			dataMem.noAction( );
			regBank.write( RT, rtVal );
		} else
			throw new IllegalStateException( "Instruction [" + opcode + "] Not Implemented");
	}
	
}
