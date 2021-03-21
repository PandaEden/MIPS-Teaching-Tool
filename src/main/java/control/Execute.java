package control;

import model.Instruction;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;

import util.logs.ExecutionLog;
import util.logs.Logger;

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
	
	/** Instanced Version of {@link #execute(DataMemory, RegisterBank, InstrMemory, ExecutionLog, StringBuilder)} */
	public void execute(ArrayList<Instruction> instructions, StringBuilder output) {
		execute( dataMem, regBank, new InstrMemory( instructions, exLog ), exLog, output );
	}
	
	/**Loops though the given instructions, executing them until reading an instruction that returns a null address (Exit)
	 Output is appended to the StringBuilder.@return
	 */
	public static String execute(DataMemory dataMem, RegisterBank regBank,
								 InstrMemory instrMemory, ExecutionLog exLog,
								 StringBuilder output)
			throws IndexOutOfBoundsException, IllegalArgumentException {
		Instruction ins;
		try {
			for ( Integer PC=InstrMemory.BASE_INSTR_ADDRESS;
				  PC!=null;
			) {
				output.append( regBank.format( ) ); // Register Bank
				ins=instrMemory.InstructionFetch( PC );
				PC=ins.execute( PC, dataMem, regBank, exLog );
				output.append( exLog.toString( ) ); //  ExecutionLog
				exLog.clear();
			}
		}catch ( IndexOutOfBoundsException | IllegalArgumentException e ){
			// catch Exception -> Calling method should print the ErrLog/ WarningLog
			// after Execution Finishes to see what went wrong
			output.append( exLog.toString( ) );
			output.append( Logger.Color.fmtColored( Logger.Color.ERR_LOG, "ERROR: "+e.getMessage() ) );
		}
		return output.toString();
	}
	
}
