package util.validation;

import _test.Tags;
import _test.Tags.Pkg;
import _test.TestLogs;
import _test.TestLogs.FMT_MSG;
import _test.providers.*;
import _test.providers.InstrProvider.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import model.*;
import model.Instruction.Type;

import util.logs.ErrorLog;

import java.util.Arrays;
import java.util.HashMap;

import static _test.TestLogs.FMT_MSG._opsForOpcodeNotValid;
import static org.junit.jupiter.api.Assertions.*;

/**
 Testing a New Instruction
 <ol>
 <li> - add it's configuration to {@link InstrProvider} </li>
 <li> - Invalid_SetOperands  </li>
 <li> - Add to appropriate Instruction_Type under {@link Validate_Operands} </li>
 <li> - See the Layout Instructions for {@link Validate_Operands} </li>
 </ol>
 */
@Tag ( Pkg.UTIL )
@Tag ( Pkg.VALID )
@Tag ( Tags.INSTR )
@DisplayName ( Pkg.UTIL + " : " + Pkg.VALID + " : " + Tags.INSTR + " Test" )
public class ValidateInstructionsTest {
	
	private static final TestLogs testLogs=new TestLogs( );
	private static ErrorLog expectedErrs;
	private static OperandsValidation opsVal;
	
	@BeforeAll
	static void beforeAll ( ) {
		expectedErrs=testLogs.expectedErrors;
		opsVal=new OperandsValidation( testLogs.actualErrors, testLogs.actualWarnings );
	}
	
	@AfterEach
	void clear ( ) { testLogs.after( ); }
	
	@Nested	// Possibly Move to ValidateTest,
	@DisplayName ( "isValidOpcode : Validate Instruction" )
	class Validate_Opcode {
		
		@ParameterizedTest ( name="[{index}] Valid Opcode[{0}]" )
		@ArgumentsSource ( InstrProvider.class )
		void isValidOpcode_Valid (String opcode) {
			assertTrue( opsVal.isValidOpCode( 1, opcode ) );
		}
		
		@ParameterizedTest ( name="[{index}] Not Valid - Opcode[{0}]" )
		@ArgumentsSource ( InstrProvider.Invalid.class )
		@ArgumentsSource ( BlankProvider.class )
		void isValidOpcode_Not_Valid (String opcode) {
			assertFalse( opsVal.isValidOpCode( 230, opcode ) );
			expectedErrs.appendEx( 230, FMT_MSG.Opcode_NotSupported( opcode ) );
		}
		
	}
	
	/**
	 <ul> <li> Type </li>
	 <ul> <li> Register Formation/NumberOf </li>
	 <ul><li> Valid -> Operands Split Successfully, to Create an Instruction. [ins then needs to be assembled]
	 <ul> Assembles -> Checking The Result or trying to Assemble ({@link Instruction#assemble(ErrorLog, HashMap)})
	 <ul><li> FailAssemble.</li>
	 <li> ThrowsAssemble.</li></ul>
	 </ul></li>
	 <li> Invalid_Operands -> Invalid Formatting (with correct #of operands)
	 <ul>Should Also Test If It Reads From $Zero, Produces a Warning </ul>
	 </li>
	 <li>Additional Methods Closely Related/ This can also be put into {@link testSubModules} </li>
	 </ul>
	 </ul>
	 </ul>
	 */
	@Nested
	@DisplayName ( "splitValidInstruction : Validate Instruction" )
	class Split_Valid_Instruction {
		
		private final String FA=" -> Fail!Assemble";
		private final String TA=" -> Throws!Assemble";
		private final String A=" -> Assembles!";
		private final HashMap<String, Integer> LABELS_MAP=InstrProvider.labelsMap;
		private final Expect expect=new Expect( );
		
		// Wrapper / Helper Class for Expected Errors/Warnings .. since there was so many
		private class Expect {
			private final ErrorLog errLog=testLogs.actualErrors;
			/** {@link InstrProvider#type(String)} */
			private Type type (String opcode) { return InstrProvider.type( opcode ); }
			
			/** {@link OperandsValidation#splitValidOperands(int, String, String)} */
			private void invalidOperandsForOpcode (int LineNo, String opcode, String operands) {
				assertNull( opsVal.splitValidOperands( LineNo, opcode, operands ) );
				expectedErrs.append( LineNo, _opsForOpcodeNotValid( opcode, operands ) );
			}
			
			/** Uses Default LineNo of "-1", use {@link OperandsValidation#setLineNo(int)} before this! */
			private void invalidOperandsForOpcode (String opcode, String operands) { invalidOperandsForOpcode( -1, opcode, operands ); }
			
			/** Uses Default LineNo of "-1", use {@link OperandsValidation#setLineNo(int)} before this! */
			private void invalidOperandsForOpcode (String opcode, String... operands) {
				Arrays.stream( operands ).forEach( ops -> invalidOperandsForOpcode( opcode, ops ) );
			}
			
			/** Sets the LineNo to -1 automatically, then expects NO_OPS is an invalid operand for the opcode. */
			private void NoOps_NotValid_AndSetLineNo (String opcode) {
				opsVal.setLineNo( -1 );
				expectedErrs.append( -1, FMT_MSG._NO_OPS );
				invalidOperandsForOpcode( opcode, NO_OPS.OPS );
			}
			
			/**
			 For a given list of Operands, It runs the remainder (excluding NO_OPS) of the preset Operand configurations
			 <p> After, It runs the runAfter list of operands, but does not make any assertions.
			 <p> Runs with the lineNo '-450'
			 */
			private void runAgainstOperandsList_Excluding_NoOps (String opcode, String... runAfter) {
				// This Needs a Rework
				InstrProvider.OperandsList_ExcludingNoOps( )
							 .filter( ops -> !Arrays.asList( runAfter ).contains( ops ) )
							 .forEach( ops -> invalidOperandsForOpcode( opcode, ops ) );
				Arrays.stream( runAfter ).forEach( ops -> opsVal.splitValidOperands( -450, opcode, ops ) );
			}
			
			/**
			 (J/I_MEM) Tests Instruction Assemble correctly.
			 Setting the ops.Imm==Addr.
			 <p> Then tests it throws on 2nd attempt to Assemble.
			 */
			private void assertAssemblesSuccessfully (Instruction ins, Integer postAssembleAddr) {
				assertNotNull(ins);
				assertAll(
						( ) -> assertTrue( ins.assemble( errLog, LABELS_MAP ) ),
						( ) -> assertEquals( ins.getImmediate( ), postAssembleAddr )
				);
			}
			
			/**
			 Tests Instruction Fails to Assemble, and Correct Err Msg Provided.
			 <p>Determines The error, based on if the Ops is a Jump Type
			 <p>Or If the label is not in the {@link #LABELS_MAP}
			 */
			private void assertFailAssemble_LabelPtr (Instruction ins, String label) {
				assertNotNull(ins);
				assertFalse( ins.assemble( errLog, LABELS_MAP ) );
				if ( LABELS_MAP.containsKey( label ) ) {
					int addr=LABELS_MAP.get( label );
					if ( ins instanceof J_Type ) {
						AddressValidation.isSupportedInstrAddr( addr, expectedErrs );
						expectedErrs.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Instruction" ) );
					} else {
						AddressValidation.isSupportedDataAddr( addr, expectedErrs );
						expectedErrs.appendEx( FMT_MSG.label.points2Invalid_Address( label, "Data" ) );
					}
				} else {
					expectedErrs.appendEx( FMT_MSG.label.labelNotFound( label ) );
				}
			}
			
			/**
			 Tests Operands Values of Instruction, Match the Given. Then, Assembling will cause an Error to be Thrown.
			 {@link Instruction#assemble(ErrorLog,HashMap)} */
			private <T extends Throwable> void assertNotNullOperandsEqual_And_Throws (Class<T> exception, Instruction ins,
																					 String opcode, Type type,
																					 Integer imm, int rd, int rs, int rt) {
				assertNotNull_InsEquals( ins, opcode, type, imm, rd, rs, rt );
				assertThrows( exception, ( ) -> ins.assemble( errLog, LABELS_MAP ) );
			}
			/** {@link IllegalArgumentException} */
			private void assertIMMEDIATE_Equals_AndAssembles (Instruction ins, String opcode,
															  Integer imm, int rs, int rt) {
				assertNotNull_InsEquals( ins, opcode, Type.IMMEDIATE, imm, 0, rs, rt);
				assertAssemblesSuccessfully(ins, imm);
			}
			/** {@link IllegalStateException} */
			private void assertREGISTER_Equals_AndAssembles (Instruction ins, String opcode, int rd, int rs, int rt) {
				assertNotNull_InsEquals( ins, opcode, Type.REGISTER, 0, rd, rs, rt );
				assertAssemblesSuccessfully(ins, 0);
			}
			
			private void assertNotNull_InsEquals (Instruction ins, String opcode, Type type, Integer imm, int rd, int rs, int rt) {
				assertNotNull( ins );
				assertEquals( "Instruction{ opcode= '"+opcode+"', type= " + type + ", RD= " + rd + ", RS= " + rs + ", RT= " + rt +
							 ", IMM= " + imm+" }", ins.toString() );
			}
			private void assertNotNull_InsEquals (Instruction ins, String opcode, Type type, int rt, String label) {
				assertNotNull( ins );
				assertEquals( "Instruction{ opcode= '" + opcode + "', type= " + type + ", RD= 0, RS= 0, RT= "+rt
							  +", IMM= null, label= '" + label + "' }", ins.toString() );
			}
				/** asserts the list of registers are not Recognised */
			private void notRecognised (int lineNo, String... notRecognised) {
				Arrays.stream( notRecognised ).forEach( op -> expectedErrs.append( lineNo, FMT_MSG.reg._NotRecognised( op ) ) );
			}
			
		}
		
		private int parseImm (String imm) {
			return (imm.length( )>2 && imm.charAt( 1 )=='x') ? Long.decode( imm ).intValue( ) : Integer.parseInt( imm );
		}
		
		@Nested
		class Invalid_SetOperands {
			// Need to manually print the errors that might be reported
			// For Operands the same length as the Type accepts
			
			@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\"" )
			@ArgumentsSource ( NO_OPS.class )
			@Tag ( "Invalid_Operands_Assemble" )
			void invalid (String opcode) {
				//excluding NO_OPS.OPS
				expect.runAgainstOperandsList_Excluding_NoOps( opcode, "" );
			}
			
			@ParameterizedTest ( name="Invalid Operands[{index}] For R_Type[\"{0}\"]" )
			@ArgumentsSource ( RD_RS_RT.class )
			@Tag ( "Invalid_Operands_Assemble" )
			void invalidOperands_R_Type (String opcode) {
				expect.NoOps_NotValid_AndSetLineNo( opcode );
				
				expect.runAgainstOperandsList_Excluding_NoOps( opcode, RD_RS_RT.OPS,
															   I.RT_RS_IMM.OPS );
				
				// I_type RT, RS, IMM
				expect.notRecognised( -450, "-40" );
				expectedErrs.append( -450, _opsForOpcodeNotValid( opcode, I.RT_RS_IMM.OPS ) );
			}
			
			
			@ParameterizedTest ( name="Invalid Operands[{index}] For I_Type RT_RS_IMM[\"{0}\"]" )
			@ArgumentsSource ( I.RT_RS_IMM.class )
			@Tag ( "Invalid_Operands_Assemble" )
			void invalidOperands_I_Type_RT_RS_IMM (String opcode) {
				expect.NoOps_NotValid_AndSetLineNo( opcode );
				
				expect.runAgainstOperandsList_Excluding_NoOps( opcode, I.RT_RS_IMM.OPS,
															   RD_RS_RT.OPS );
				
				// R_type RD, RS, RT
				expectedErrs.appendEx( -450, FMT_MSG.imm.notValInt( "$at" ) );
				expectedErrs.append( -450, _opsForOpcodeNotValid( opcode, RD_RS_RT.OPS ) );
				
			}
			
			@Tag ( "Invalid_Operands_Assemble" )
			@ParameterizedTest ( name="Invalid Operands[{index}] For I_Type RT_MEM[\"{0}\"]" )
			@ArgumentsSource ( I.RT_MEM.class )
			void invalidOperands_I_Type_RT_MEM (String opcode) {
				expect.NoOps_NotValid_AndSetLineNo( opcode );
				
				expect.runAgainstOperandsList_Excluding_NoOps( opcode, I.RT_MEM.OPS_IMM_RS, I.RT_MEM.OPS_LABEL,
															   I.BRANCH.OPS_IMM, I.BRANCH.OPS_LABEL );
				// Branch IMM
				AddressValidation.isSupportedDataAddr( (-2)*4, expectedErrs );
				expectedErrs.append( -450, _opsForOpcodeNotValid( opcode, I.BRANCH.OPS_IMM ) );
				
				// Branch Label	-> Fail Assembly
				Instruction ins=opsVal.splitValidOperands( -20, opcode, I.BRANCH.OPS_LABEL );
				expect.assertFailAssemble_LabelPtr( ins, "instr" );
			}
			
			@Tag ( "Invalid_Operands_Assemble" )
			@ParameterizedTest ( name="Invalid Operands[{index}] For J_Type[\"{0}\"]" )
			@ArgumentsSource ( J.class )
			void invalidOperands_Jump (String opcode) {
				expect.NoOps_NotValid_AndSetLineNo( opcode );
				
				expect.runAgainstOperandsList_Excluding_NoOps( opcode,
															   J.OPS_IMM, J.OPS_LABEL );
			}
			@Test    // Null -> Does Nothing
			@DisplayName ( "Null Opcode" )
			void nullOpcode ( ) {
				assertNull( opsVal.splitValidOperands( 230, null, null ) );
			}
			@ParameterizedTest ( name="[{index}] Invalid Opcode[\"{0}\"] -> Returns Null" )
			@ArgumentsSource ( InstrProvider.Invalid.Limit_Two.class )
			@ArgumentsSource ( BlankProvider.class )
			void invalidOpcode (String opcode) {
				expectedErrs.append( 500, FMT_MSG._NO_OPS );
				expect.invalidOperandsForOpcode( 500, opcode, null );
			}
			
			@Nested
			class Operand_Spacing {
				// ANY Spacing At The Beginning/ End Of the Line
				// Spacing of larger than 1  internally
				
				// In the Logger Output, Tabs and NewLn are removed ???
				
				@Test
				void R_Type__Leading_Trailing_And_Internal_Spaces ( ) {
					expect.notRecognised( 42, " r1", "  r2", "r3 " );
					expect.invalidOperandsForOpcode( 42, "add", " r1,   r2 , r3 " );
					
				}
				
				@Test
				void SingleTabs_Valid ( ) {
					Instruction ins=opsVal.splitValidOperands( 12, "sub", "s8,\tr3,\t$8" );
					expect.assertREGISTER_Equals_AndAssembles( ins,"sub",30, 3, 8 );
				}
				@Test
				void DoubleTabs_OrLeading_Trailing_Tabs_Invalid ( ) {
					expect.notRecognised( 12, "\t$24", "\t$16" );
					expect.invalidOperandsForOpcode( 12, "sub", "\t$24,\tr20\t,\t\t$16" );
				}
				
				@Test
				void NewLine_Invalid ( ) {
					expect.notRecognised( 67, "R0" );
					expect.invalidOperandsForOpcode( 67, "sub", "t8\t,r20,\nR0" );
				}
				
			}
			
		}
		
		@Nested
		class No_Operands {
			
			@ParameterizedTest ( name="Valid {index} - opcode\"{0}\"" + TA )
			@ArgumentsSource ( NO_OPS.class )
			void assemble_ValidOperands (String opcode) {
				Instruction ins=opsVal.splitValidOperands( 12, opcode, " " );
				expect.assertNotNull_InsEquals( ins, opcode, Type.NOP, 0, 0, 0, 0 );
				expect.assertAssemblesSuccessfully(ins, 0);
			}
			
			@Test
			void comments_Not_Removed ( ) {
				assertThrows( IllegalStateException.class, ()-> opsVal.splitValidOperands(20,"j"," #"));
				
			}
			
			@Test
			void Invalid_OpCode (){
				assertThrows( IllegalArgumentException.class, ()-> new Nop(" "));
			}
			
		}
		
		@Nested
		class Register_Type {
			
			@ParameterizedTest ( name="Valid {index} - opcode\"{0}\"" + A )
			@ArgumentsSource ( RD_RS_RT.class )
			void assemble_ValidOperands (String opcode) {
				Instruction ins=opsVal.splitValidOperands( 12, opcode, RD_RS_RT.OPS );
				expect.assertREGISTER_Equals_AndAssembles( ins, opcode, 1, 1, 1 );
			}
			@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" -  Different Register Names" + A )
			@ArgumentsSource ( RD_RS_RT.class )
			void testOperands_Add_Sub ( ) {
				Instruction ins=opsVal.splitValidOperands( 12, "add", "$8,r31, $s2" );
				expect.assertREGISTER_Equals_AndAssembles( ins, "add", 8, 31, 18 );
			}
			
			@ParameterizedTest ( name="Valid {index} - opcode [sub], operands \"{0}\" -  Mixed Spacing" + A )
			@ValueSource ( strings={ "$24 , $s4, $s0", "t8,r20,\t$16" } )
			void testOperands_R_Type_Spacing (String ops) {
				Instruction ins=opsVal.splitValidOperands( 12, "sub", ops );
				expect.assertREGISTER_Equals_AndAssembles( ins, "sub", 24, 20, 16 );
			}
			
			@Nested
			class Invalid_Operands {
				
				@ParameterizedTest ( name="Multiple Invalid {index} - opcode\"{0}\" And ZeroWriteWarning" )
				@ArgumentsSource ( RD_RS_RT.class )
				void multipleInvalid_ZWW ( ) {
					expect.notRecognised( 76, "$panda", "31" );
					expect.invalidOperandsForOpcode( 76, "add", "zero, $panda, 31" );
					testLogs.zeroWarning( 76, "zero" );
				}
			}
			
		}
		
		@Nested
		class Immediate_Type {
			
			@Nested
			@DisplayName ( "RT, RS, IMM" )
			class RT_RS_IMM {
				
				@ParameterizedTest ( name="Valid {index} - opcode\"{0}\"" + A )
				@ArgumentsSource ( I.RT_RS_IMM.class )
				void assemble_ValidOperands (String opcode) {
					Instruction ins=opsVal.splitValidOperands( 12, opcode, I.RT_RS_IMM.OPS );
					expect.assertIMMEDIATE_Equals_AndAssembles( ins, opcode, -40, 1, 1 );
				}
				
				@ParameterizedTest ( name="{index} - Immediate\"{2}\" Valid 16Bit :: Hex" )
				@ArgumentsSource ( ImmediateProvider._16Bit.class )
				void Valid_16Bit_Hex (String addr, Integer address, String hex, Integer imm) {
					Instruction ins = opsVal.splitValidOperands(30,"addi","$2, $2, "+hex);
					expect.assertIMMEDIATE_Equals_AndAssembles( ins, "addi", imm, 2, 2);
				}
				
				@ParameterizedTest ( name="{index} - Immediate\"{3}\" Valid 16Bit :: Imm" )
				@ArgumentsSource ( ImmediateProvider._16Bit.class )
				void Valid_16Bit_Imm (String addr, Integer address, String hex, Integer imm) {
					Instruction ins = opsVal.splitValidOperands(30,"addi","$2, $2, "+imm);
					expect.assertIMMEDIATE_Equals_AndAssembles( ins, "addi", imm, 2, 2);
				}
				
				@Nested
				class Invalid_Operands {
					
					@Test
					@DisplayName ( "Invalid Operands I_RT_RS_IMM (ADDI), given I_RT_Imm" )
					void invalidOperands_I_RT_RS ( ) { expect.invalidOperandsForOpcode( 52, "addi", "$1, 0x20" ); }
					
					@ParameterizedTest ( name="Multiple Invalid {index} - opcode\"{0}\" And ZeroWriteWarning" )
					@ArgumentsSource ( I.RT_MEM.class )
					void multipleInvalid_ZWW ( ) {
						Instruction ins=opsVal.splitValidOperands( 30, "addi", "$0, $panda, 32769" );
						// Errors with all Operands
						assertNull( ins );
						testLogs.appendErrors(
								30,
								FMT_MSG.reg._NotRecognised( "$panda" ),
								FMT_MSG.imm.notSigned16Bit( 32769 ) + "!",
								_opsForOpcodeNotValid( "addi", "$0, $panda, 32769" )
						);
						testLogs.zeroWarning( 30, "$0" );
					}
					
					@ParameterizedTest ( name="{index} - Immediate\"{2}\" Not Valid 16Bit :: Hex" )
					@ArgumentsSource ( ImmediateProvider._16Bit.Invalid.class )
					void Invalid_16Bit_Hex (String addr, Integer address, String hex, Integer imm) {
						expectedErrs.appendEx(30,FMT_MSG.imm.notSigned16Bit( imm ));
						expect.invalidOperandsForOpcode( 30, "addi", "$2, $2, " + hex);
					}
					
					@ParameterizedTest ( name="{index} - Immediate\"{3}\" Not Valid 16Bit :: Imm" )
					@ArgumentsSource ( ImmediateProvider._16Bit.Invalid.class )
					void Invalid_16Bit_Imm (String addr, Integer address, String hex, Integer imm) {
						expectedErrs.appendEx(30,FMT_MSG.imm.notSigned16Bit( imm ));
						expect.invalidOperandsForOpcode( 30, "addi", "$2, $2, " + imm);
					}
				}
				
			}
			
			@Nested
			@DisplayName ( "Memory" )
			class Memory {
				
				/*
				 TODO - Inconsistency with Base+Offset
				 	When having just an Imm - it is checked if it is a valid address.
				 	When Imm with Empty Brackets (Register $0),   It does not !. -> This should be caught during Execution
				 */
				
				/** If opcode belongs to rt_write, it pushed ZeroWriteWarning */
				private void possibleZeroWarning (int lineNo, String opcode, String regName) {
					if ( OperandsValidation.I_MEM_WRITE.contains( opcode ) )
						testLogs.zeroWarning( lineNo, regName );
				}
				
				@Nested
				class Valid {
					
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Label" + A )
					@ArgumentsSource ( I.RT_MEM.class )
					void assemble_ValidOperands_Label (String opcode) {
						Instruction ins=opsVal.splitValidOperands( 12, opcode, "$9, data" );
						expect.assertNotNull_InsEquals( ins, opcode, Type.IMMEDIATE, 9, "data" );
						expect.assertAssemblesSuccessfully( ins, 0x10010000 );
						//MAX
						Instruction ins1=opsVal.splitValidOperands( 12, opcode, "r2, data_top" );
						expect.assertNotNull_InsEquals( ins1, opcode, Type.IMMEDIATE, 2, "data_top" );
						expect.assertAssemblesSuccessfully( ins1, 0x100107F8 );
					}
					
					@Nested
					@DisplayName( "Base+Offset/Imm(RS)" )
					class Base_Offset {
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Imm(RS)" + A )
						@ArgumentsSource ( I.RT_MEM.class )
						void assemble_ValidOperands (String opcode) {
							Instruction ins=opsVal.splitValidOperands( 12, opcode, I.RT_MEM.OPS_IMM_RS );
							expect.assertIMMEDIATE_Equals_AndAssembles( ins, opcode,-8, 1, 1 );
						}
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: Int" + A )
						@ArgumentsSource ( I.RT_MEM.class )
						void assemble_ValidOperands_BaseOffset_INT (String opcode) {
							Instruction ins=opsVal.splitValidOperands( 0, opcode,"$5, 20 ($1)" );
							expect.assertIMMEDIATE_Equals_AndAssembles( ins, opcode,20, 1, 5 );
						}
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: NoImm" + A )
						@ArgumentsSource ( I.RT_MEM.class )
						void assemble_ValidOperands_BaseOffset_NoImm (String opcode) {
							Instruction ins=opsVal.splitValidOperands( 0, opcode, "$6, ($1)" );
							expect.assertIMMEDIATE_Equals_AndAssembles( ins, opcode, 0, 1, 6 );
						}
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: HEX" + TA )
						@ArgumentsSource ( I.RT_MEM.class )
						@Tag ( Tags.MULTIPLE )
						@DisplayName ( "Test Operands, Base+Offset" )
						void assemble_ValidOperands_BaseOffset_Hex (String opcode) {
							Instruction ins=opsVal.splitValidOperands( 0, opcode, "$5, 0x290($8)" );
							expect.assertIMMEDIATE_Equals_AndAssembles( ins, opcode, 656, 8, 5 );
						}
						
						@Nested
						class no_RS {
							
							@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: NoImm, noRS" + A )
							@ArgumentsSource ( I.RT_MEM.class )
							void assemble_ValidOperands_BaseOffset_NoImm_noRS (String opcode) {
								// TODO check the IMM is a valid address, when RS=0
								Instruction ins1=opsVal.splitValidOperands( 0, opcode, "$8,  ()" );
								expect.assertIMMEDIATE_Equals_AndAssembles( ins1, opcode, 0, 0, 8 );
							}
							
							@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: Int, noRS" + A )
							@ArgumentsSource ( I.RT_MEM.class )
							void assemble_ValidOperands_BaseOffset_INT_noRS (String opcode) {
								// TODO check the IMM is a valid address, when RS=0
								Instruction ins1=opsVal.splitValidOperands( 0, opcode,"$7, -800 ()" );
								expect.assertIMMEDIATE_Equals_AndAssembles( ins1, opcode, -800, 0, 7 );
							}
							
							@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: HEX, noRS" + A )
							@ArgumentsSource ( I.RT_MEM.class )
							void assemble_ValidOperands_BaseOffset_Hex_noRS (String opcode) {
								// TODO check the IMM is a valid address, when RS=0
								Instruction ins1=opsVal.splitValidOperands( 0, opcode, "$9, 0xFFFF8000 ()" );
								expect.assertIMMEDIATE_Equals_AndAssembles( ins1, opcode, -32768, 0, 9);
							}
						}
						
					}
					
				}
				
				@Nested
				class Invalid_Operands {
					
					@ParameterizedTest ( name="Valid - opcode\"{0}\", NonData Label" + FA )
					@ArgumentsSource ( I.RT_MEM.class )
					void NonData_Label (String opcode) {
						for ( String label : InstrProvider.KeysExcluding( "data","data_top" ) ) {
							Instruction ins=opsVal.splitValidOperands( 12, opcode, "$2," + label );
							expect.assertNotNull_InsEquals( ins, opcode, Type.IMMEDIATE, 2, label );
							expect.assertFailAssemble_LabelPtr( ins, label );
						}
					}
					
					@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\" LabelNotFound And ZeroWriteWarning" )
					@ArgumentsSource ( I.RT_MEM.class )
					void LabelNotFound_ZWW (String opcode) {
						Instruction ins=opsVal.splitValidOperands( 30, opcode, "$0, panda" );
						
						expect.assertNotNull_InsEquals( ins, opcode, Type.IMMEDIATE, 0, "panda" );
						expect.assertFailAssemble_LabelPtr( ins, "panda" );
						possibleZeroWarning( 30, opcode, "$0" );
					}
					
					
					@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\" Invalid Integer" )
					@ArgumentsSource ( I.RT_MEM.class )
					void invalid_Integer (String opcode) {
						Instruction ins=opsVal.splitValidOperands( 23, opcode, "$1, 1.5" );
						// Errors with all Operands
						expectedErrs.appendEx( 23, FMT_MSG.imm.notValInt( "1.5" ) );
						expectedErrs.append( 23, _opsForOpcodeNotValid( opcode, "$1, 1.5" ) );
						
						expectedErrs.appendEx( 23, FMT_MSG.imm.notValInt( "1.5" ) );
						expect.invalidOperandsForOpcode( 23, opcode, "$1, 1.5" );
					}
					
					@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\" No ImmRS Found" )
					@ArgumentsSource ( I.RT_MEM.class )
					void NoImmRS_NotFound (String opcode) {
						expectedErrs.appendEx( 78, "\t\tNo Imm(RS) found" );
						expect.invalidOperandsForOpcode( 78, opcode, "$1," );
					}
					
				}
				
				@Nested
				@DisplayName ( "Imm(RS) method" )
				class immRS_method {
					@BeforeEach
					void setUp ( ) {
						opsVal.setLineNo( -1 );	// TODO change back to parameters being passed into every method
						opsVal.setOpcode( "lw" );// valid opcode for ImmRS
					}
					
					@Nested
					@DisplayName ( "Valid Imm(RS)" )
					class validImmRS {
						
						private void assertImmRS (Instruction ins, int imm, int rs) {
							assertNotNull( ins );
							System.out.println( ins );
							assertAll(
									( ) -> assertTrue( ins.toString().contains( "IMM= "+imm ) ),
									( ) -> assertTrue( ins.toString().contains( "RS= "+rs ) )
							);
						}
						
						@ParameterizedTest ( name="Valid no RS, no IMM {index}, Just Brackets[{0}]] -> Default" )
						@ValueSource ( strings={ "( )", "()" } )
						void onlyBrackets (String immRS) {
							assertImmRS( opsVal.rt_ImmRs( 9, immRS ), 0, 0 );
						}
						
						@ParameterizedTest ( name="Valid {index} - \"{0}\" -> noIMM -> Default" )
						@ValueSource ( strings={ "($2)", "( $2 )" } )
						void noIMM (String immRS) {
							assertImmRS( opsVal.rt_ImmRs( 8, immRS ), 0, 2 );// RS = $2 ==>2
						}
						
						@ParameterizedTest ( name="Valid {index} - \"{0}\" -> noRS -> Default" )
						@ValueSource ( strings={ "8()", "8 ()", "8 ( )", "0x8 ()" } )
						void noRS (String immRS) {
							assertImmRS( opsVal.rt_ImmRs( 7, immRS ), 8, 0 );// Imm = 8
						}
						
					}
					
					@Nested
					@DisplayName ( "Invalid Imm(RS)" )
					class invalidImmRS {
						
						@ParameterizedTest ( name="invalidImmRS {index} - \"{0}\" -> MissingOpeningBracket" )
						@ValueSource ( strings={ "1.5 )", "8$2)", "8 $2)", "8)", ")", "$2)", "0x2 $4)" } )
						void missingOpeningBracket (String immRS) {
							assertNull( opsVal.rt_ImmRs( 5, immRS ) );
							expectedErrs.appendEx( -1, FMT_MSG.imm.RS_MissingOpeningBracket( ) );
						}
						@ParameterizedTest ( name="invalidImmRS {index} - \"{0}\" -> MissingClosingBracket" )
						@ValueSource ( strings={ "1.5 (", "8($2", "8 ($2", "8(", "(", "($2", "0x2 ($4" } )
						void missingClosingBracket (String immRS) {
							assertNull( opsVal.rt_ImmRs( 4, immRS ) );
							expectedErrs.appendEx( -1, FMT_MSG.imm.RS_MissingClosingBracket( ) );
						}
						
					}
					
					@Nested
					@DisplayName ( "Label_MEM-Address" )
					class I_Label_MEM {
						
						@ParameterizedTest ( name="Imm {index} - \"{0}\" -> Not Valid Data Address" )
						@ValueSource ( strings={ "8", "0x8" } )
						void noRS (String immRS) {
							Instruction ins=opsVal.rt_ImmRs( 7, immRS );    // Imm
							assertNull( ins );
							AddressValidation.isSupportedDataAddr( 4*parseImm( immRS ), expectedErrs );
						}
						
						@Nested
						@DisplayName ( "Invalid Imm" )
						class invalid_Imm {
							
							@ParameterizedTest ( name="{index}, invalid Imm[{0}]]" )
							@ValueSource ( strings={ "1.5", "0x 2" } )
							void invalidImm (String imm) {
								assertNull( opsVal.rt_ImmRs( 4, imm ) );
								expectedErrs.appendEx( -1, FMT_MSG.imm.notValInt( imm ) );
								
								assertNull( opsVal.rt_ImmRs( 4, imm + " ()" ) );// + Brackets
								expectedErrs.appendEx( -1, FMT_MSG.imm.notValInt( imm ) );
								
							}
							
							@ParameterizedTest ( name="{index}, invalid Imm[{0}]] Not 16Bit" )
							@ValueSource ( strings={ "67108863", "-32769", "32768", "0xFFFF7FFF", "0x00008000" } )// TODO ARG SOURCE
							void Not_16bit (String imm) {
								assertNull( opsVal.rt_ImmRs( 4, imm ) );
								expectedErrs.appendEx( -1, FMT_MSG.imm.notSigned16Bit( parseImm( imm ) ) );
								
								assertNull( opsVal.rt_ImmRs( 4, imm + " ()" ) );// + Brackets
								expectedErrs.appendEx( -1, FMT_MSG.imm.notSigned16Bit( parseImm( imm ) ) );
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		@Nested
		@DisplayName ( "J_Type" )
		class Jump {
			
			@Nested
			@Tag( Tags.MULTIPLE )
			class Valid {
				
				
				@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Jump_Type :: Imm" + A )
				@ArgumentsSource ( J.class )
				void assemble_ValidOperands_Label (String opcode) {
					Instruction ins=opsVal.splitValidOperands( 60, opcode, "instr" );
					expect.assertNotNull_InsEquals( ins, opcode, Type.JUMP, 0, "instr");
					expect.assertAssemblesSuccessfully( ins, 0x00400000/4);
					//MAX
					Instruction ins1=opsVal.splitValidOperands( 60, opcode, "instr_top" );
					expect.assertAssemblesSuccessfully( ins1, 0x00500000/4);
				}
				
				@Nested
				class Throws_Assemble {
					
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Jump_Type :: Imm" + TA )
					@ArgumentsSource ( J.class )
					void assemble_ValidOperands_Imm (String opcode) {
						Instruction ins=opsVal.splitValidOperands( 0, opcode, "1048576" );
						expect.assertNotNull_InsEquals( ins, opcode, Type.JUMP, 1048576,0,0,0);
						//MAX
						Instruction ins1=opsVal.splitValidOperands( 0, opcode, "1310720" );
						expect.assertNotNull_InsEquals( ins1, opcode, Type.JUMP, 1310720,0,0,0);
					}
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Jump_Type :: Hex" + TA )
					@ArgumentsSource ( J.class )
					void assemble_ValidOperands_Hex (String opcode) {
						Instruction ins=opsVal.splitValidOperands( 0, opcode, "0x00100000" );
						expect.assertNotNull_InsEquals( ins, opcode, Type.JUMP, 1048576, 0,0, 0);
						//MAX
						Instruction ins1=opsVal.splitValidOperands( 0, opcode, "0x00140000" );
						expect.assertNotNull_InsEquals( ins1, opcode, Type.JUMP, 1310720, 0,0, 0);
					}
					
				}
				
			}
			
			@Nested
			class Invalid_Operands {
				
				@ParameterizedTest ( name="IO {index}Jump _Immediate[{0}] - Out of Range" )
				@ArgumentsSource (  ImmediateProvider.ConvertInvalid.OutOfRange.class )
				void testInvalid_OperandsJump_ImmOutOfRange (String hex, int imm) {
					Instruction ins=opsVal.splitValidOperands( 0, "j", "" + imm );
					
					assertNull( ins );
					expectedErrs.appendEx( 0, FMT_MSG.imm.notUnsigned26Bit(imm) );
					expectedErrs.append( 0, _opsForOpcodeNotValid( "j", "" + imm ) );
				}
				
				
				// Immediate Values <(0x00100000) & >(0x00140000) Convert To Valid Addresses, but not Valid for Jump
				@ParameterizedTest ( name="[{index}] Invalid 26BitImm[{2}, {3}] for for Jump Instruction" )
				@ArgumentsSource ( ImmediateProvider.Instr_Imm.Invalid.class )
				@ArgumentsSource ( ImmediateProvider.u_26Bit.class )
				@Tag( Tags.MULTIPLE )
				void testInvalid_OperandsJump_ValImm (String hexAddr, long addr, String hexImm, long imm) {
					assertEquals(addr, imm*4); // Test Variables Invalid if False
					
					int a=(int) imm*4;
					String err="Instruction Address: \"" +hexAddr + "\" Not "+ ((a>=0x10000000 || a<0x00400000) ? "Valid" : "Supported");
					expectedErrs.appendEx( err );
					expect.invalidOperandsForOpcode( 97, "j", "" + imm);
					
					//Hex
					expectedErrs.appendEx( err );
					expect.invalidOperandsForOpcode( 120, "j", "" + imm);
				}
				
				@Test
				@DisplayName ( "Test Invalid Operands, Jump _TooManyOperands" )
				void testInvalid_OperandsJump_TooManyOperands ( ) {
					expect.invalidOperandsForOpcode( "j", "0x100009, 50" );
				}
				
				@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", NonInstr Label" + FA )
				@ArgumentsSource ( J.class )
				void NonData_Label (String opcode) {
					for ( String label : InstrProvider.KeysExcluding( "instr","instr_top" ) ) {
						Instruction ins=opsVal.splitValidOperands( 12, opcode, label );
						expect.assertNotNull_InsEquals( ins, opcode, Type.JUMP, 0, label );
						expect.assertFailAssemble_LabelPtr( ins, label );
					}
				}
			}
			
			
			@Nested
			class Instr_Label_Or_Imm {
				
				@ParameterizedTest ( name="[{index}] Immediate[{0}], Valid for Jump" )
				@ArgumentsSource(ImmediateProvider.Instr_Imm.class )
				void Jump_LabelAddress (String hexAddr, long addr, String hexImm, long imm) {
					assertNotNull( opsVal.splitValidOperands( -1, "j", ""+imm ) );
					assertNotNull( opsVal.splitValidOperands( -1, "j", hexImm ));
					
				}
				
				@ParameterizedTest ( name="[{index}] Label[{0}], Valid Label/Address for Jump" )
				@ArgumentsSource(SetupProvider.ValidLabels.class )
				void Invalid_LabelAddress (String label) { assertNotNull( opsVal.splitValidOperands( -1, "j", label ) ); }
				
				@ParameterizedTest ( name="[{index}] Immediate[{0}], Valid for Mem" )
				@Tag ( Tags.MULTIPLE )
				@ValueSource ( strings={ "20", "0x2" } )
				void Mem_LabelAddress (String labelAddr) {
					assertNotNull( opsVal.splitValidOperands( -1, "lw", "$1," + labelAddr + "($0)" ) );
					assertNotNull( opsVal.splitValidOperands( -1, "sw", "$1," + labelAddr + "($0)" ) );
				}
				
				@Nested
				class Invalid {
					
					@ParameterizedTest ( name="[{index}] Label[{0}], Invalid Label/Address for Jump" )
					@ArgumentsSource( SetupProvider.InvalidLabels.class )
					void Invalid_LabelAddress (String label) {
						assertNull( opsVal.splitValidOperands( -1, "j", label ) );
						expectedErrs.appendEx( -1, FMT_MSG.label.notSupp( label ) );
						expectedErrs.append( -1, _opsForOpcodeNotValid( "j", label ) );
						
					}
					
				}
				
			}
		}
		
	}
	
	@Nested
	@DisplayName ( "Test Sub Modules" )
	class testSubModules {
		
		@Test
		@Tag ( Tags.MULTIPLE )
		@DisplayName ( "Valid notNullInRange" )
		void notNullInRange ( ) {
			assertTrue( OperandsValidation.notNullAndInRange( 4, 4, 5 ) );
			
			assertTrue( OperandsValidation.notNullAndInRange( 0, 0, 31 ) );
			
			assertFalse( OperandsValidation.notNullAndInRange( 3, 4, 5 ) );
			assertFalse( OperandsValidation.notNullAndInRange( null, 0, 5 ) );
			
			//noinspection ResultOfMethodCallIgnored
			assertThrows( IllegalArgumentException.class, ( ) -> OperandsValidation.notNullAndInRange( 3, 5, 4 ) );
			
			//noinspection ResultOfMethodCallIgnored
			assertThrows( IllegalArgumentException.class, ( ) -> OperandsValidation.notNullAndInRange( null, 5, 4 ) );
		}
		
		
		
		@Test
		@DisplayName ( "Convert Invalid Register" )
		void validateConvertRegister ( ) {
			opsVal.setLineNo( -1 );
			assertNull( opsVal.convertRegister( "$f0", DataType.FLOATING_POINT ) );
			expectedErrs.appendEx( -1, FMT_MSG.reg.wrongData( "$f0" ) );
			
			assertNull( opsVal.convertRegister( "$-40", DataType.NORMAL ) );
			expectedErrs.appendEx( -1, FMT_MSG.reg.notInRange( "$-40" ) );
			
			assertNull( opsVal.convertRegister( "$50", DataType.NORMAL ) );
			expectedErrs.appendEx( -1, FMT_MSG.reg.notInRange( "$50" ) );
		}
		
		@ParameterizedTest ( name="NO_zeroWarning[{index}] on Read, Reg[{0}]" )
		@ArgumentsSource ( RegisterProvider.ZERO.class )
		void zeroWarning_Read (String regName) {
			//isValidLoadRegister
			assertNotNull( opsVal.convertRegister( regName, DataType.NORMAL ) );
		}
		
		@ParameterizedTest ( name="zeroWarning[{index}] on Write, Reg[{0}]" )
		@ArgumentsSource ( RegisterProvider.ZERO.class )
		void zeroWarning_Write (String regName) {
			opsVal.setLineNo( -1 );
			assertNotNull( opsVal.convertWriteRegister( regName, DataType.NORMAL ) );
			testLogs.zeroWarning( -1, regName );
		}
		
	}
	
}
