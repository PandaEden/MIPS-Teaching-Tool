package control;

import org.jetbrains.annotations.VisibleForTesting;

import model.Instruction;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;

import util.ansi_codes.Color;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;

public class Execute {
	private final ExecutionLog exLog;
	private final DataMemory dataMem;
	private final RegisterBank regBank;
	public Execute(ExecutionLog exLog, HashMap<Integer, Double> data, int[] values) {
		this.exLog=exLog;
		this.dataMem=new DataMemory( data, exLog );
		this.regBank=new RegisterBank( values, exLog );
	}
	
	/** Instanced Version of {@link #execute(DataMemory, RegisterBank, ArrayList, ExecutionLog, StringBuilder)} */
	@VisibleForTesting
	public void execute(ArrayList<Instruction> instructions, StringBuilder output) {
		execute( dataMem, regBank, instructions, exLog, output );
	}
	
	/**Loops though the given instructions, executing them until reading an instruction that returns a null address (Exit)
	 Output is appended to the StringBuilder.@return
	 */
	@VisibleForTesting
	public static void execute(DataMemory dataMem, RegisterBank regBank,
								 ArrayList<Instruction> instructions, ExecutionLog exLog,
								 StringBuilder output)
			throws IndexOutOfBoundsException, IllegalArgumentException {
		Instruction ins;
		InstrMemory instrMemory = new InstrMemory( instructions, exLog );
		try {
			for ( Integer PC=InstrMemory.BASE_INSTR_ADDRESS;
				  PC!=null;
			) {
				output.append( regBank.format( ) ); // Register Bank
				output.append( "\n" );
				ins=instrMemory.InstructionFetch( PC );
				PC=ins.execute( PC, dataMem, regBank, exLog );
				output.append( exLog.toString( ) ); //  ExecutionLog
				exLog.clear();
			}
		}catch ( IndexOutOfBoundsException | IllegalArgumentException e ){
			// catch Exception -> Calling method should print the ErrLog/ WarningLog
			// after Execution Finishes to see what went wrong
			output.append( exLog.toString( ) );
			output.append( Color.fmt( Color.ERR_LOG, "ERROR: " + e.getMessage() ) );
		}
	}
	
}
