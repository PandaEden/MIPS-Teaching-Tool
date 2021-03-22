package _test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Instruction;
import model.components.DataMemory;
import model.components.InstrMemory;
import model.components.RegisterBank;
import model.instr.Operands;

import setup.Parser;

import util.Convert;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

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
	
	/**
	 Checks any ExpectedLogs match their Actual Counterpart.
	 Then Checks all Actual Logs are empty.
	 */
	public void after(){
		after(expectedErrors,	actualErrors );
		after(expectedWarnings, actualWarnings);
		after(expectedExecution, actualExecution);
		testNo++;
	}
	
	private static void after(Logger expected, Logger actual) {
		if ( actual.hasEntries( ) )
			TestMethods.logsMatch( expected, actual );
		else
			TestMethods.noEntries( expected );
	}
	
	public void expectErrors (int lineNo, String... list) {
		String pre=expectedErrors.setLineNoPrefix( lineNo );
		Arrays.stream( list ).forEach( expectedErrors :: append );
		expectedErrors.setPrefix( pre );// reset
	}
	public void zeroWarning(int lineNo, String regName) {
		expectedWarnings.appendEx( lineNo, FMT_MSG.ZER0_WARN( regName ) );
	}
	
	@Test
	@DisplayName ("static variables are correct")
	void staticVars() {
		Assertions.assertAll(    // Alt+F7 : Find Usages
								 //Data
								 () -> assertEquals( 512, Parser.MAX_LINES ),
		
								 () -> assertEquals( 256, DataMemory.MAX_DATA_ITEMS ),
								 () -> assertEquals( 8, DataMemory.DATA_ALIGN ),
								 () -> assertEquals( 268500992, DataMemory.BASE_DATA_ADDRESS ),
								 () -> assertEquals( 268503040, DataMemory.OVER_SUPPORTED_DATA_ADDRESS ),
								 () -> assertEquals( 268697600, DataMemory.OVER_DATA_ADDRESS ),
		
								 () -> assertEquals( 256, InstrMemory.MAX_INSTR_COUNT ),
								 () -> assertEquals( 4, InstrMemory.ADDR_SIZE ),
								 () -> assertEquals( 4194304, InstrMemory.BASE_INSTR_ADDRESS ),
								 () -> assertEquals( 5242880, InstrMemory.OVER_SUPPORTED_INSTR_ADDRESS ),
								 () -> assertEquals( 268435456, InstrMemory.OVER_INSTR_ADDRESS )
		);
	}
	
	private static class TestMethods {
		/**Used to prevent repeated calls failing. */
		private static String capture2StringThenClear(Logger log) {
			String actualString=log.toString( );
			log.clear( );    // Just In case it was not empty. Make it Empty so further tests do not also fail.
			return actualString;
		}
		private static String capturePrintRtn (Logger log){
			if (log.hasEntries()) tempPrint("\n"+testNo+" Log - Content: " + log.toString( ) );
			//else tempPrint("\n"+testNo+" Log : " + log.getName()+" Has No Entries!");
			return capture2StringThenClear(log);
		}
		
		/**
		 Checks the msg against the contents of Log.
		 <p>Automatically appends "{logMame}\n\t" Prefix, and "\n" Suffix</p>
		 <b>After the test, the Logger is Cleared!</b>
		 */
		public static void logsMatch(Logger expected, Logger actual) {
			Assertions.assertAll(
					() -> assertEquals( capture2StringThenClear( expected ), capturePrintRtn( actual ) )
			);
		}
		
		/**
		 Checks that Logger Contains No Entries. -> If it does, It prints them.
		 <b>After the test, the Logger is Cleared!</b>
		 */
		public static void noEntries(Logger log) {assertEquals( "", capturePrintRtn( log )); }
	}
	public static void tempPrint(String txt){
		Logger.Color.colorSupport=true;
		System.out.print(Logger.Color.fmtColored(Logger.Color.next(), txt));
		Logger.Color.colorSupport=false;
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
		/** used with .append  - non EX */
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
			public static String RS_MissingClosingBracket() {
				return "\tMissing Closing Bracket: \")\" ";
			}
			public static String RS_MissingOpeningBracket() {
				return "\tMissing Opening Bracket: \"(\" ";
			}
			public static String notSigned16Bit(int imm) {
				return "\tImmediate Value: \"" + imm + "\" Not In (Signed 16Bit) Range";
			}
			public static String notUnsigned26Bit(int imm) {
				return "\tImmediate Value: \"" + imm + "\" Not In (Unsigned 26Bit) Range";
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
				return "Label \"" + label + "\" Not Found";
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
			public static String NoDataGiven_Word = "No Data Given! For DataType: \".word\"";
		}
		
		public static final String FailedAssemble = "Failed To Assemble Instructions";
		
		public static class _Execution {
			private final RegisterBank actualRegisterBank;
			private final DataMemory actualDataMemory;
			private final ExecutionLog actualExLog;
			private final ExecutionLog expectedExLog;
			public _Execution (int[] values, HashMap<Integer, Double> data, ExecutionLog actual, ExecutionLog expected) {
				this.actualRegisterBank=new RegisterBank( values,actual );
				this.actualDataMemory=new DataMemory( data, actual );
				this.actualExLog=actual;
				this.expectedExLog=expected;
			}
			
			public Integer execute(Integer PC, Instruction instruction){
				return instruction.execute( PC, actualDataMemory, actualRegisterBank, actualExLog );
			}
			public static String _fetch (int pc){
				return "Fetching Instruction At Address [" + Convert.int2Hex(pc) + "]";
			}
			private void rb_noAct (){ expectedExLog.appendEx( "RegisterBank:\tNo Action"); }
			private void dm_noAct(){ expectedExLog.appendEx( "DataMemory:\tNo Action"); }
			public void decode (String hexPC, String opcode, String type){
				expectedExLog.append( "\n\t ---- " + hexPC + " ---- " + type + " Type Instruction >> \"" + opcode + "\":");
			}
			private void storeNPC(int npc){
				expectedExLog.append( "Storing Next Program Counter! : "+Convert.int2Hex(npc) );
			}
			
			public void rb_read(int val, int reg){
				expectedExLog.appendEx( "RegisterBank:\tReading Value[" + val + "]\tFrom Register Index[R" + reg + "]");
			}
			private void rb_write(int val, int reg){
				expectedExLog.appendEx( "RegisterBank:\tWriting Value[" + val + "]\tTo Register Index[*R" + reg + "]");
			}
			private void IMM(int imm){ expectedExLog.append( "[IMMEDIATE: " + imm + "]"); }
			public void cal_result(String aluAction){
				expectedExLog.append( "Calculating Result:" );
				expectedExLog.append( aluAction );
			}
			
			public void imm_cal_addr (int imm, int rs_val, int addr){
				expectedExLog.append( "[IMMEDIATE: " + imm + " === " + Convert.int2Hex(imm) + "]");
				expectedExLog.append( "Calculating Address:" );
				expectedExLog.append( "ADDRESS = RS+IMMEDIATE = "+rs_val+" + "+imm+" = "+addr+" ==> "+Convert.int2Hex(addr) );
			}
			private void shift_imm(int imm, int addr){
				expectedExLog.append( "Left Shifting IMMEDIATE By 2 = "+Convert.int2Hex(imm)
										+" << 2 ==> ["+addr+" === "+Convert.int2Hex(addr)+"]");
			}
			private void dm_read(int val, int addr){
				expectedExLog.appendEx( "DataMemory:\tReading Value[" + val + "]\tFrom Memory Address["
										+Convert.int2Hex(addr) +"]");
			}
			private void dm_write(int val, int addr){
				expectedExLog.appendEx( "DataMemory:\tWriting Value[" + val + "]\tTo Memory Address["
										+Convert.int2Hex(addr) +"]");
			}
			private void rtn_addr(int addr){
				expectedExLog.appendEx( "Returning Jump Address: "+ Convert.int2Hex(addr) );
			}
			
			public void exit_output (String hexPc, String opcode){
				decode( hexPc, opcode, "EXIT" );
				rb_noAct();
				dm_noAct();
			}
			
			public void R_output(String hexPC, String opcode, int RS, int rs_val, int RT, int rt_val, int RD, int rd_val){
				String sign="   ";
				switch ( opcode ){
					case "add": sign = "+"; break;
					case "sub": sign = "-"; break;
				}
				decode( hexPC, opcode, "REGISTER" );
				rb_read( rs_val, RS );
				rb_read( rt_val, RT);
				cal_result( "RD = RS"+sign+"RT = "+rs_val+sign+rt_val+" ==> "+rd_val );
				dm_noAct( );
				rb_write( rd_val, RD );
			}
			public void I_output(String hexPC, String opcode, int RS, int rs_val,int IMM, int RT, int rt_val){
				String sign="   ";
				switch ( opcode ){
					case "addi": sign = "+"; break;
				}
				decode( hexPC, opcode, "IMMEDIATE" );
				rb_read( rs_val, RS );
				IMM( IMM );
				cal_result( "RT = RS"+sign+"IMMEDIATE = "+rs_val+sign+IMM+" ==> "+rt_val );
				dm_noAct( );
				rb_write( rt_val, RT );
			}
			
			public void load_output(String hexPC, int RS, int rs_val, int IMM, int RT, int rt_val){
				decode( hexPC, "lw", "IMMEDIATE" );
				rb_read( rs_val, RS );
				imm_cal_addr( IMM, rs_val, IMM+rs_val );
				dm_read( rt_val, IMM+rs_val );
				rb_write( rt_val, RT );
			}
			public void store_output(String hexPC, int RS, int rs_val, int IMM, int RT, int rt_val){
				decode( hexPC, "sw", "IMMEDIATE" );
				rb_read( rs_val, RS );
				rb_read( rt_val, RT );
				imm_cal_addr( IMM, rs_val, IMM+rs_val );
				dm_write( rt_val, IMM+rs_val );
				rb_noAct( );
			}
			public void J_output (String hexPC, int imm){
				decode( hexPC, "j", "JUMP" );
				rb_noAct( );
				shift_imm( imm, imm*4 );
				dm_noAct( );
				rtn_addr( imm*4 );
			}
			
			public void jal_output(String hexPC, int imm){
				int npc = Convert.hex2uInt(hexPC)+4;
				decode( hexPC, "jal", "JUMP" );
				storeNPC( npc );
				rb_write( npc, 31 );
				shift_imm( imm, imm*4 );
				dm_noAct( );
				rtn_addr( imm*4 );
			}
			public void run_over(){
				expectedExLog.append( "\tRun Over Provided Instructions!" );
			}
		}
		
	}
}
