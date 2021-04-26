package control;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import model.components.Component;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;
import model.instr.Instruction;
import model.instr.J_Type;
import model.instr.MemAccess;
import model.instr.Nop;

import util.Convert;
import util.ansi_codes.Color;
import util.logs.ExecutionLog;
import util.validation.InstructionValidation;

import java.util.ArrayList;

public class Execution {
	private final ExecutionLog exLog;
	private final DataMemory dataMem;
	private final RegisterBank regBank;
	private final InstrMemory instrMemory;
	
	// Fetch / Decode Always run the same, so the title is printed with the rest of their output
	private static final String READ_OPS=Color.fmtTitle( Color.YELLOW, "Reading Operands" ) + ":";
	private static final String EXECUTE =Color.fmtTitle( Color.RED, "Execution" ) + ":";
	private static final String MEM_ACC =Color.fmtTitle( Color.MAGENTA, "Memory Access" ) + ":";
	private static final String WRITE_BACK =Color.fmtTitle( Color.WHITE, "Write Back" ) + ":";
	
	public Execution (@NotNull ExecutionLog exLog, DataMemory dataMem, RegisterBank regBank, @NotNull ArrayList<Instruction> instructions) {
		this.exLog=exLog;
		this.dataMem=dataMem;
		this.regBank=regBank;
		this.instrMemory=new InstrMemory( instructions, exLog );
		reset();
	}
	/** Instanced Version of {@link #RunToEnd(DataMemory, RegisterBank, ArrayList, ExecutionLog, StringBuilder)} */
	@VisibleForTesting
	public void RunToEnd (ArrayList<Instruction> instructions, StringBuilder output) {
		RunToEnd( dataMem, regBank, instructions, exLog, output );
	}
	
	/**Loops though the given instructions, executing them until reading an instruction that returns a null address (Exit)
	 Output is appended to the StringBuilder.@return
	 */
	@VisibleForTesting
	public static void RunToEnd (DataMemory dataMem, RegisterBank regBank,
								 ArrayList<Instruction> instructions, ExecutionLog exLog,
								 StringBuilder output){
		Execution ex = new Execution( exLog, dataMem, regBank, instructions);
		for ( Integer PC=InstrMemory.BASE_INSTR_ADDRESS;
			  PC!=null;
		) {
			output.append( regBank.format( ) ); // Register Bank
			output.append( "\n" );
			PC = ex.runStep(output);
		}
	}
	
	/**Returns Next on Error . or Exit Instruction Completed WB*/
	public Integer runStep(StringBuilder output){
		try {
			pipeline();
			output.append( exLog.toStringAndClear() ); //  ExecutionLog
		} catch ( IndexOutOfBoundsException | IllegalArgumentException e ) {
			this.NPC=this.PC=null; // Signal to exit
			
			// catch Exception -> Calling method should print the ErrLog/ WarningLog
			// after Execution Finishes to see what went wrong
			output.append( exLog.toStringAndClear() );
			output.append( Color.fmt( Color.ERR_LOG, "ERROR: " + e.getMessage() ) );
		} catch ( IllegalStateException e ) { // Not Pre-Assembled /successfully
			exLog.clear();
			throw e;
		}
		return this.PC; // == Null ∴ Exit
	}
	
	private String toHex(Integer val){
		if (val==null) return null; // pass Null forward
		return Convert.int2Hex(val);
	}
	
	private boolean notNullEq1(Integer i){
		return ( i!=null && i==1);
	}
	
	// TODO create wrapper ? to hold all the values?
	private Integer PC, NPC, IMM, RR1, RR2, AOR, LMDR, ARR;
	private Instruction ins;
	
	public void reset(){
		this.PC=InstrMemory.BASE_INSTR_ADDRESS;
		exLog.clear();
	}
	
	private void fetch(Integer ProgramCounter){
		ins = instrMemory.InstructionFetch( ProgramCounter );
		if ( ins.getImmediate()==null && (ins instanceof J_Type || ins instanceof MemAccess) ) {    // TODO , move to Read Operands
			throw new IllegalStateException( ins.getOpcode( ) + " must be Assembled before Execution " + Convert.int2Hex( PC ) );
		}
		this.NPC=Component.ADDER( ProgramCounter, 4,
								   "\tIncrement_PC: NPC = PC + 4 === " + toHex( ProgramCounter + 4), this.exLog);
	}
	
	private Integer[] decode(Instruction instruction){
		return Component.DECODER( instruction, this.exLog );
	}
	
	private void read_operands(Instruction instruction,  Integer _Destination){
		exLog.append( READ_OPS );
		int[] temp_RB_Out;
		Integer RS = instruction.getRS();
		Integer RT = instruction.getRT();
		
		if ( _Destination!=null && _Destination==0 )
			RT = null; // redundant
		
		if ( RS!=null) {
			temp_RB_Out=regBank.read( RS, RT );
			this.RR1=temp_RB_Out[ 0 ];
			this.RR2=temp_RB_Out[ 1 ];
		}
		this.IMM= instruction.getImmediate();
		if (IMM!=null)
			exLog.append( "\t[IMMEDIATE: " + IMM + " === "+toHex(IMM)+"]" );
	}
	
	private void execute (Integer RegisterResult1, Integer NextProgramCounter, Integer RegisterResult2, Integer ImmediateRegister,
						  Integer _ALUSource1, Integer _ALUSource2, Integer _ALU_Operation, Integer _ProgramCounterWrite){
		exLog.append( EXECUTE );
		Integer ALUInputRegister1 = Component.MUX( _ALUSource1,"ALUSrc1", RegisterResult1, NextProgramCounter);
		
		Integer ALUInputRegister2 = Component.MUX( _ALUSource2,"ALUSrc2", RegisterResult2, ImmediateRegister);
		
		if ( _ProgramCounterWrite!=null && (_ProgramCounterWrite==1 || _ProgramCounterWrite==2) ){
			int ADDR = Convert.imm2Address( ImmediateRegister );
			exLog.append( "\tLeft Shifting IMMEDIATE By 2: " + toHex( ImmediateRegister ) + " << " + 2 + " ==> [" + ADDR + " === " + toHex( ADDR ) + "]");
			
			if ( _ProgramCounterWrite==2 ){
				//noinspection ConstantConditions
				ADDR = Component.ADDER( Convert.imm2Address( ImmediateRegister ), NextProgramCounter,
										"\t\tTarget Address = "+toHex( ADDR )+" + NPC ==> "+toHex( ADDR+NPC )+"\n",
										exLog);
			}
			this.IMM= ADDR;
		}
		
		this.AOR=Component.ALU( ALUInputRegister1, ALUInputRegister2, _ALU_Operation, exLog );
	}
	
	private void memory(Integer StoreValueRegister, Integer ALUOutputRegister, Integer _MemoryAction,
						Integer NextProgramCounter, Integer ImmediateRegister, Integer _ProgramCounter_Write, Integer _BranchCondition){
		exLog.append( MEM_ACC );
		
		Integer branchPC = NextProgramCounter;
		
		if ( _BranchCondition!=null ){
			String branchPrint="AOR["+ALUOutputRegister+"]";
			String T = " True:Branch Taken";
			String F = " False:Branch NOT~Taken";
			
			if ( _BranchCondition==0) {
				branchPrint+=" == Zero?";
				if ( ALUOutputRegister==0 ){
					branchPC=ImmediateRegister;
					branchPrint+=T;
				}else
					branchPrint+=F;
			}else if ( _BranchCondition==1) {
				branchPrint+=" == Not~Zero?";
				if ( ALUOutputRegister!=0 ){
					branchPC=ImmediateRegister;
					branchPrint+=T;
				}else
					branchPrint+=F;
			}
			
			exLog.appendEx( branchPrint );
		}
		
		this.NPC=Component.MUX( _ProgramCounter_Write, "NPC", NextProgramCounter, ImmediateRegister, branchPC);
		
		if ( _MemoryAction!=null ) {
			if ( _MemoryAction==0 ) // Load
				this.LMDR=dataMem.readData( ALUOutputRegister );
			else if ( _MemoryAction==1 ) // Store
				dataMem.writeData( ALUOutputRegister, StoreValueRegister );
		}
		this.ARR=ALUOutputRegister;
	}
	
	private void write_back(Integer AluResultRegister, Integer LoadDataMemoryRegister, Integer _MemToReg, Integer _Destination){
		exLog.append( WRITE_BACK );
		Integer WB_Data = Component.MUX( _MemToReg, "WriteBack", AluResultRegister, LoadDataMemoryRegister );
		Integer DestinationRegister = Component.MUX( _Destination, "RegDest", ins.getRT(), ins.getRD(), 31 );
		
		if ( ins instanceof Nop && InstructionValidation.NO_OPERANDS_OPCODE.contains( ins.getOpcode() ) ) // Might be redundant
			this.NPC= null;
		else if ( DestinationRegister!=null )
			regBank.write( DestinationRegister, WB_Data );
		
		this.PC=this.NPC;
		exLog.append( "--------------------------------" );
	}
	
	@VisibleForTesting
	public Integer pipeline(){
		Integer[] control = new Integer[7];
		//TODO refactor methods to return arrays, instead of using global variables
		fetch(this.PC );
		control = decode(this.ins);
		read_operands(this.ins, control[0]);
		execute( this.RR1, this.NPC, this.RR2, this.IMM, control[1], control[2], control[3], control[6]);
		memory( this.RR2, this.AOR,control[4], this.NPC, this.IMM, control[6], control[7]); // SVR = RR2
		write_back( this.ARR, this.LMDR, control[5], control[0]);
		return this.PC;
	}
}
