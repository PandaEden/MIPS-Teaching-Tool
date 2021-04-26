package _test;

import control.Execution;

import model.components.DataMemory;
import model.components.RegisterBank;
import model.instr.Instruction;
import model.instr.R_Type;

import util.Convert;
import util.ansi_codes.Color;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;
import util.validation.InstrSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 Shared Testing Variables/ Methods.
 <p>
 Methods automatically clears the logs, so further tests do not also fail.
 */
public class TestLogs {
	
	public final ErrorLog actualErrors=new ErrorLog( new ArrayList<>( ) );
	public final WarningsLog actualWarnings=new WarningsLog( new ArrayList<>( ) );
	public final ExecutionLog actualExecution=new ExecutionLog( new ArrayList<>( ) );
	public final ErrorLog expectedErrors=new ErrorLog( new ArrayList<>( ) );
	public final WarningsLog expectedWarnings=new WarningsLog( new ArrayList<>( ) );
	public final ExecutionLog expectedExecution=new ExecutionLog( new ArrayList<>( ) );
	private static int testNo=0;
	public TestLogs ( ) {
		Color.colorSupport=false;
	}
	
	/**
	 Checks any ExpectedLogs match their Actual Counterpart.
	 Then Checks all Actual Logs are empty.
	 */
	public void after(){
		String errors=captureAndPrint(actualErrors);
		String warnings=captureAndPrint(actualWarnings);
		String execution=captureAndPrint(actualExecution);
		
		assertAll(
				()->assertEquals(capture2StringThenClear(expectedErrors), errors),
				()->assertEquals(capture2StringThenClear(expectedWarnings), warnings),
				()->assertEquals(capture2StringThenClear(expectedExecution), execution)
		);
		
		testNo++;
	}
	
	/**Uses {@link ErrorLog#append(String)}*/
	public void appendErrors (int lineNo, String... list) {
		String pre=expectedErrors.setLineNoPrefix( lineNo );
		Arrays.stream( list ).forEach( expectedErrors :: append );
		expectedErrors.setPrefix( pre );// reset
	}
	
	public void zeroWarning(int lineNo, String regName) {
		expectedWarnings.appendEx( lineNo, FMT_MSG.ZER0_WARN( regName ) );
	}
	private static String captureAndPrint (Logger log){
		if (log.hasEntries()) tempPrint("\n"+testNo+" "+log.getName()+" - Content: " + log.toString( ) );
		//else tempPrint("\n"+testNo+" Log : " + log.getName()+" Has No Entries!");
		return capture2StringThenClear(log);
	}
	private static String capture2StringThenClear(Logger log) {
		String actualString=log.toString( );
		log.clear( );    // Just In case it was not empty. Make it Empty so further tests do not also fail.
		return actualString;
	}
	public static void tempPrint(String txt){
		Color.colorSupport=true;
		System.out.print( Color.fmt( Color.next(), txt));
		Color.colorSupport=false;
	}
	
	// Methods starting with an underscore have the !, so use append for those, for the rest, use appendEx
	public static class FMT_MSG {
		
		public static String xAddressNot (String X, String hexAddress, String thing) { return X+" Address: \"" + hexAddress + "\" Not " + thing; }
		
		public static String ZER0_WARN(String regName) {
			return "Destination Register: \"" + regName + "\" Cannot be modified!,\t Result will be ignored";
		}
		
		public static final String _NO_OPS="\tNo Operands found!";
		
		public static String Opcode_NotSupported(String opcode) {
			return "Opcode: \"" + opcode + "\" Not Supported";
		}
		public static String DirectiveNotSupported(String directive){
			return "Directive: \"" + directive + "\" Not Supported";
		}
		public static String _opsForOpcodeNotValid (String opcode, String operands) {
			return "Operands: [" + operands + "] for Opcode: \"" + opcode + "\" Not Valid !";
		}
		public static class reg {
			
			public static String _NotRecognised (String reg) {
				return "\tRegister: \"" + reg + "\" Not Recognised!";
			}
			
			public static String notInRange(String reg) {
				return "\tRegister: \"" + reg + "\" Not In Range";
			}
			public static String wrongData(String reg) {
				return "\tRegister: \"" + reg + "\" Wrong DataType";
			}
			
		}
		
		public static class imm {
			public static String notValInt(String imm) {
				return "\tImmediate Value: \""+imm+"\" Not Valid Integer";
			}
			public static final String RS_MissingClosingBracket = "\tMissing Closing Bracket: \")\" ";
			public static final String RS_MissingOpeningBracket = "\tMissing Opening Bracket: \"(\" ";
			public static String notSigned16Bit(int imm) {
				return "\tImmediate Value: \"" + imm + "\" Not In (Signed 16Bit) Range";
			}
			public static String notUnsigned26Bit(int imm) {
				return "\tImmediate Value: \"" + imm + "\" Not In (Unsigned 26Bit) Range";
			}
			public static String cantConvert (int imm){
				return "Immediate Value: \""+imm+"\", Cannot Be Converted To A Valid Address";
			}
		}
		
		public static class label {
			public static String notSupp(String label) {
				return "Label: \""+label+"\" Not Supported";
			}
			public static String points2Invalid_Address (String label, String type) {
				return "Label: \"" + label + "\" points to Invalid " + type + " Address";
			}
			public static String labelNotFound(String label) {
				return "Label: \"" + label + "\" Not Found";
			}
			
		}
		
		public static class data {
			public static String NotValSignedInt(String value){
				return "Data Value: [" + value + "], Not Valid Signed Integer";
			}
			public static String NotValSignedInt(String value, int index){
				return "Data Value: [" + value + "], Index: \""+index+"\", Not Valid Signed Integer";
			}
			public static String N_MustBePosInt(int N){
				return  "<Int_N>: [" + N + "], Must Be A Positive Integer!\tFormat: \"<Int_Val> : <Int_N>\"";
			}
			
			public static String NotValFor_WordType (String data){
				return "Data: [" + data + "], Not Valid For DataType: \".word\"";
			}
			public static final String NoDataGiven_Word = "No Data Given! For DataType: \".word\"";
		}
		
		public static final String FailedAssemble = "Failed To Assemble Instructions";
		
		public static class _Execution {
			private final ExecutionLog expectedExLog;
			private final ArrayList<Instruction> instructions;
			private final Execution execution;
			
			public _Execution (int[] values, HashMap<Integer, Double> data, ExecutionLog actual, ExecutionLog expected) {
				this.expectedExLog=expected;
				instructions = new ArrayList<>();
				execution= new Execution( actual, new DataMemory( data, actual ), new RegisterBank( values, actual ),
										  instructions);
			}
			public static void _pipeline(Instruction ins, int PC, DataMemory dm, RegisterBank rb, ExecutionLog lg){
				int index= Convert.instrAddr2Index( PC );
				ArrayList<Instruction> instrs = new ArrayList<>();
				
				Instruction nop = new R_Type( "add",0,0,0 );
				for ( int i=0; i<=index;i++ ){ instrs.add( nop ); }
				
				Execution ex = new Execution(lg,dm,rb, instrs);
				instrs.add( Convert.instrAddr2Index( PC ), ins );
				StringBuilder out = new StringBuilder();
				for ( int p=0; p<index; p++ ){
					ex.runStep(out);
				}
				ex.pipeline();
			}
			
			public Integer pipeline(Instruction instruction){
				instructions.add(0, instruction );
				execution.reset();
				return execution.pipeline();
			}
			public void runToEnd(ArrayList<Instruction> instructions, StringBuilder out){
				execution.RunToEnd(instructions, out );
			}
			
			public void _fetching(int pc){
				expectedExLog.append("Fetching: Instruction At Address [" + Convert.int2Hex( pc ) + "]");
			}
			public void _fetch (int pc){
				_fetching(pc);
				expectedExLog.append("\tIncrement_PC: NPC = PC + 4 === " + Convert.int2Hex( pc+4 ));
			}
			
			private static String _decodeTitle(String opcode, String type){
				return "Decoding:\t----\t" + type + " Instruction :: " + opcode.toUpperCase() + " :: " + InstrSpec.findSpec( opcode ).getNAME();
			}
			private static String _decodeControl(String[] name){
				return "\n\t\tALUSrc1["+name[1]+"], ALUSrc2["+name[2]+"], ALUOp["+name[3]+"],\tRegDest["+name[0]+"]"+
					   "\n\t\tMemOp["+name[4]+"], MemToReg["+name[5]+"],\tPCWrite["+name[6]+"], BranchCond["+name[7]+"]";
			}
			
			public static String _control_RType (String opcode, String ALUOp){
				return _decodeTitle( opcode, "REGISTER" )+
					   _decodeControl(new String[]{"RD", "AIR1","AIR2",ALUOp, "-","No:AOR", "NPC","-"});
			}
			public static String _control_IType (String opcode, String ALUOp){
				return _decodeTitle(opcode, "IMMEDIATE")+
					   _decodeControl(new String[]{"RT", "AIR1","IMM",ALUOp, "-","No:AOR", "NPC","-"});
			}
			public static String _control_Branch (String opcode,int cond, String ALUOp){
				return _decodeTitle(opcode, "IMMEDIATE")+
					   _decodeControl(new String[]{"-", "AIR1","AIR2",ALUOp, "-","-", "COND", cond==0?"Zero":"Not~Zero"});
			}
			public static String _control_Load (){
				return _decodeTitle("lw", "IMMEDIATE")+
					   _decodeControl(new String[]{"RT", "AIR1","IMM","ADD", "READ->LMDR","Yes:LMDR", "NPC","-"});
			}
			public static String _control_Store (){
				return _decodeTitle("sw", "IMMEDIATE")+
					   _decodeControl(new String[]{"-", "AIR1","IMM","ADD", "WRITE<-SVR","-", "NPC","-"});
			}
			public static String _control_Jump (){
				return _decodeTitle("j", "JUMP")+
					   _decodeControl(new String[]{"-", "-","-","-", "-","-", "IMM","-"});
			}
			public static String _control_JumpAndLink (){
				return _decodeTitle("jal", "JUMP")+
					   _decodeControl(new String[]{"$ReturnAddress:31", "NPC","-","NOP", "-","No:AOR", "IMM","-"});
			}
			public static String _control_Nop (String opcode, String PCWrite){
				return _decodeTitle(opcode, "NOP")+
					   _decodeControl(new String[]{"-", "-","-","-", "-","-", PCWrite,"-"});
			}
			
			private void _read () { expectedExLog.append("Reading Operands:"); }
			private void _execute () { expectedExLog.append("Execution:"); }
			private void _memory () { expectedExLog.append("Memory Access:"); }
			private void _write_back () { expectedExLog.append("Write Back:"); }
			private void __ () { expectedExLog.append("--------------------------------" ); }
			
			private void rb_read(int val, int reg){
				expectedExLog.appendEx( "\tRegisterBank:\tReading Value[" + val + "]\tFrom Register Index[R" + reg + "]");
			}
			private void rb_read_Modified(int val, int reg){
				expectedExLog.appendEx( "\tRegisterBank:\tReading Value[" + val + "]\tFrom Register Index[*R" + reg + "]");
			}
			private void rb_write(int val, int reg){
				expectedExLog.appendEx( "\tRegisterBank:\tWriting Value[" + val + "]\tTo Register Index[*R" + reg + "]");
			}
			private void IMM(int imm){ expectedExLog.append( "\t[IMMEDIATE: " + imm +" === " + Convert.int2Hex(imm) + "]"); }
			private void ALU (String aluAction){
				expectedExLog.append( "\tALU Result = "+aluAction );
			}
			
			private void imm_add (int imm, int rs_val, int addr){
				IMM( imm );
				_execute();
				ALU( rs_val+" + "+imm+" ==> "+addr);
			}
			private void shift_imm(int imm, int addr){
				expectedExLog.append( "\tLeft Shifting IMMEDIATE By 2: "+Convert.int2Hex(imm)
										+" << 2 ==> ["+addr+" === "+Convert.int2Hex(addr)+"]");
			}
			private void dm_read(int val, int addr){
				expectedExLog.appendEx( "\tDataMemory:\tReading Value[" + val + "]\tFrom Memory Address["
										+Convert.int2Hex(addr) +"]");
			}
			private void dm_write(int val, int addr){
				expectedExLog.appendEx( "\tDataMemory:\tWriting Value[" + val + "]\tTo Memory Address["
										+Convert.int2Hex(addr) +"]");
			}
			private void rtn_addr(int addr){
				expectedExLog.appendEx( "Returning Jump Address: "+ Convert.int2Hex(addr) );
			}
			
			public void exit_output (int pc, String opcode){
				_fetch( pc );
				expectedExLog.append(_control_Nop( opcode, "-" ));
				_read();
				_execute();
				_memory();
				_write_back();
				__();
			}
			public void auto_exit_output (int pc){
				_fetching( pc );
				expectedExLog.append( "\tRun Over Provided Instructions -- Auto Exit!" );
				expectedExLog.append("\tIncrement_PC: NPC = PC + 4 === " + Convert.int2Hex( pc+4 ));
				expectedExLog.append(_control_Nop( "exit", "-" ));
				_read();
				_execute();
				_memory();
				_write_back();
				__();
			}
			
			public void R_output(int pc, String opcode, int RS, int rs_val, int RT, int rt_val, int RD, int rd_val, String sign){
				sign=" "+sign+" ";
				_fetch( pc );
				expectedExLog.append(_control_RType( opcode, opcode.toUpperCase() ));
				_read();
				rb_read( rs_val, RS );
				rb_read( rt_val, RT );
				_execute();
				ALU( rs_val + sign + rt_val + " ==> " + rd_val );
				_memory();
				_write_back();
				rb_write( rd_val, RD );
				__();
			}
			
			/** 0-RS modifies, 1-RT modified, else- both*/
			public void modified_R_output(int pc, String opcode, int RS, int rs_val, int RT, int rt_val, int RD, int rd_val,
										  String sign, int modified){
				sign=" "+sign+" ";
				_fetch( pc );
				expectedExLog.append(_control_RType( opcode, opcode.toUpperCase() ));
				_read();
				if ( modified == 0) {
					rb_read_Modified( rs_val, RS );
					rb_read( rt_val, RT );
				} else if (modified == 1){
					rb_read( rs_val, RS );
					rb_read_Modified( rt_val, RT );
				} else{
					rb_read_Modified( rs_val, RS );
					rb_read_Modified( rt_val, RT );
				}
				_execute();
				ALU( rs_val + sign + rt_val + " ==> " + rd_val );
				_memory();
				_write_back();
				rb_write( rd_val, RD );
				__();
			}
			public void I_output (int pc, String opcode, int RS, int rs_val, int RT, int rt_val, int IMM, String sign){
				sign=" "+sign+" ";
				_fetch( pc );
				expectedExLog.append(_control_IType( opcode, opcode.substring(0,3).toUpperCase() ));
				_read();
				rb_read( rs_val, RS );
				IMM( IMM );
				_execute();
				ALU( rs_val + sign + IMM + " ==> " + rt_val );
				_memory();
				_write_back();
				rb_write( rt_val, RT );
				__();
			}
			
			public void Branch_output(int pc, String opcode, int RS, int rs_val, int RT, int rt_val, int imm, String sign, boolean zero, boolean taken){
				sign=" "+sign+" ";
				_fetch( pc );
				
				String op="";
				int res = rs_val^rt_val;
				
				if ( sign.equals(" ^ ") ) {
					op="XOR";
				}else if ( sign.equals( " < " ) ) {
					op="SLT";
					sign = " set-on < ";
					res = ( rs_val<rt_val )?1:0;
				} else if ( sign.equals( " <= " ) ) {
					op="SLT|E";
					sign = " set-on <= ";
					res = ( rs_val<=rt_val )?1:0;
				}
				expectedExLog.append(_control_Branch(opcode,zero?0:1,op));
				
				_read();
				rb_read( rs_val, RS );
				rb_read( rt_val, RT );
				IMM( imm );
				_execute();
				shift_imm( imm, imm*4 );
				expectedExLog.append("\t\tTarget Address = "+Convert.int2Hex( imm*4 )
									   +" + NPC ==> "+Convert.int2Hex(imm*4+pc+4 )+"\n" );
				if ( sign.equals(" ^ ") )
					expectedExLog.append( "\t (binary) '" + Integer.toBinaryString(rs_val) +"' xor '"
										+  Integer.toBinaryString(rt_val) + "' ==> '"
										  +Integer.toBinaryString(res) +"'");
				ALU( rs_val + sign + rt_val + " ==> " + res );
				_memory();
				expectedExLog.appendEx("\tAOR["+res+"] == "+(zero?"Zero":"NOT~Zero") +"? "+(taken?"True:Branch Taken":"False:Branch NOT~Taken") );
				_write_back();
				__();
			}
			
			public void load_output(int pc, int RS, int rs_val, int IMM, int RT, int rt_val){
				_fetch( pc );
				expectedExLog.append(_control_Load());
				_read();
				rb_read( rs_val, RS );
				imm_add( IMM, rs_val, IMM+rs_val );
				_memory();
				dm_read( rt_val, IMM+rs_val );
				_write_back();
				rb_write( rt_val, RT );
				__();
			}
			public void store_output(int pc, int RS, int rs_val, int IMM, int RT, int rt_val){
				_fetch( pc );
				expectedExLog.append(_control_Store());
				_read();
				rb_read( rs_val, RS );
				rb_read( rt_val, RT );
				imm_add( IMM, rs_val, IMM+rs_val );
				_memory();
				dm_write( rt_val, IMM+rs_val );
				_write_back();
				__();
			}
			//TODO - implement the modified versions a bit better,   perhaps changing the int RS/RT/RD inputs to String
			public void modified_load_output(int pc, int RS, int rs_val, int IMM, int RT, int rt_val){
				_fetch( pc );
				expectedExLog.append(_control_Load());
				_read();
				rb_read_Modified( rs_val, RS );
				imm_add( IMM, rs_val, IMM+rs_val );
				_memory();
				dm_read( rt_val, IMM+rs_val );
				_write_back();
				rb_write( rt_val, RT );
				__();
			}
			/** 0-RS modifies, 1-RT modified, else- both*/
			public void modified_store_output(int pc, int RS, int rs_val, int IMM, int RT, int rt_val, int modified){
				_fetch( pc );
				expectedExLog.append(_control_Store());
				_read();
				if ( modified == 0) {
					rb_read_Modified( rs_val, RS );
					rb_read( rt_val, RT );
				} else if (modified ==1){
					rb_read( rs_val, RS );
					rb_read_Modified( rt_val, RT );
				} else{
					rb_read_Modified( rs_val, RS );
					rb_read_Modified( rt_val, RT );
				}
				imm_add( IMM, rs_val, IMM+rs_val );
				_memory();
				dm_write( rt_val, IMM+rs_val );
				_write_back();
				__();
			}
			
			public void J_output (int pc, int imm){
				_fetch( pc );
				expectedExLog.append(_control_Jump());
				_read();
				IMM( imm );
				_execute();
				shift_imm( imm, imm*4 );
				_memory();
				_write_back();
				__();
			}
			
			public void jal_output(int pc, int imm){
				int npc = pc+4;
				_fetch( pc );
				expectedExLog.append(_control_JumpAndLink());
				_read();
				IMM( imm );
				_execute();
				shift_imm( imm, imm*4 );
				ALU( npc+ " ==> "+ npc );
				_memory();
				_write_back();
				rb_write( npc, 31 );
				__();
			}
			
			public void load_output_before_exception(int pc, int RS, int rs_val, int IMM, int ADDR){
				_fetch( pc );
				expectedExLog.append(_control_Load());
				_read();
				rb_read( rs_val, RS );
				imm_add( IMM, rs_val, ADDR );
				_memory();
			}
			public void store_output_before_exception(int pc, int RS, int rs_val, int IMM, int RT, int rt_val, int ADDR){
				_fetch( pc );
				expectedExLog.append(_control_Store());
				_read();
				rb_read( rs_val, RS );
				rb_read( rt_val, RT );
				imm_add( IMM, rs_val, IMM+rs_val );
				_memory();
			}
		}
		
	}
}
