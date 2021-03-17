package util.validation;

import _test.Tags;
import _test.Tags.Pkg;
import _test.TestLogs;
import _test.TestLogs.FMT_MSG;
import _test.providers.AddressProvider.Immediate;
import _test.providers.BlankProvider;
import _test.providers.InstrProvider;
import _test.providers.InstrProvider.*;
import _test.providers.SetupProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import model.DataType;
import model.instr.Operands;
import model.instr.Operands.InstrType;

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
@DisplayName ( Pkg.UTIL + " : " + Pkg.VALID + " - " + Tags.INSTR + " Test" )
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
	
	@Nested
	@DisplayName ( "isValidOpcode : Validate Operands" )
	class Validate_Opcode {
		
		@ParameterizedTest ( name="[{index}] Valid Opcode[{0}]" )
		@ArgumentsSource ( InstrProvider.class )
		void isValidOpcode_Valid (String opcode) {
			assertTrue( opsVal.isValidOpCode( 1, opcode ) );
		}
		
		@ParameterizedTest ( name="[{index}] Not Valid - Opcode[{0}]" )
		@ArgumentsSource ( Invalid.class )
		@ArgumentsSource ( BlankProvider.class )
		void isValidOpcode_Not_Valid (String opcode) {
			assertFalse( opsVal.isValidOpCode( 230, opcode ) );
			expectedErrs.appendEx( 230, FMT_MSG.Opcode_NotSupported( opcode ) );
		}
		
	}
	
	/**
	 <ul> <li> Type </li>
	 <ul> <li> Register Formation/NumberOf </li>
	 <ul><li> Valid -> Operands Split Successfully. [assemble_ValidOperands]
	 <ul> Assembles -> Checking The Result or trying to Assemble ({@link Operands#setImmediate(ErrorLog, HashMap)})
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
	@DisplayName ( "splitValidOperands : Validate Operands" )
	class Validate_Operands {
		
		private final String FA=" -> Fail!Assemble";
		private final String TA=" -> Throws!Assemble";
		private final String A=" -> Assembles!";
		private final HashMap<String, Integer> LABELS_MAP=InstrProvider.labelsMap;
		private final Expect expect=new Expect( );
		
		// Wrapper / Helper Class for Expected Errors/Warnings ..  since there was so many
		private class Expect {
			private final ErrorLog errLog=testLogs.actualErrors;
			/** {@link InstrProvider#type(String)} */
			private InstrType type (String opcode) { return InstrProvider.type( opcode ); }
			
			/** {@link OperandsValidation#splitValidOperands(int, String, String)} */
			private void operandsForOpcodeNotValid (int LineNo, String opcode, String operands) {
				assertNull( opsVal.splitValidOperands( LineNo, opcode, operands ) );
				expectedErrs.append( LineNo, _opsForOpcodeNotValid( opcode, operands ) );
			}
			
			/** Uses Default LineNo of "-1",  use {@link OperandsValidation#setLineNo(int)} before this! */
			private void operandsForOpcodeNotValid (String opcode, String operands) { operandsForOpcodeNotValid( -1, opcode, operands ); }
			
			/** Uses Default LineNo of "-1",  use {@link OperandsValidation#setLineNo(int)} before this! */
			private void operandsForOpcodeNotValid (String opcode, String... operands) {
				Arrays.stream( operands ).forEach( ops -> operandsForOpcodeNotValid( opcode, ops ) );
			}
			
			/** Sets the LineNo to -1 automatically, then expects NO_OPS is an invalid operand for the opcode. */
			private void NoOps_NotValid_AndSetLineNo (String opcode) {
				opsVal.setLineNo( -1 );
				expectedErrs.append( -1, FMT_MSG._NO_OPS );
				operandsForOpcodeNotValid( opcode, NO_OPS.OPS );
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
							 .forEach( ops -> operandsForOpcodeNotValid( opcode, ops ) );
				Arrays.stream( runAfter ).forEach( ops -> opsVal.splitValidOperands( -450, opcode, ops ) );
			}
			
			/**
			 (J/I_MEM) Tests Operands Assemble correctly.
			 Setting the ops.Imm==Addr.
			 <p> Then tests it throws on 2nd attempt to Assemble.
			 */
			private void assertAssembles_Label_Successfully (@NotNull Operands ops, Integer postAssembleAddr) {
				assertAll(
						( ) -> assertTrue( ops.setImmediate( errLog, LABELS_MAP ) ),
						( ) -> assertEquals( ops.getImmediate( ), postAssembleAddr ),
						( ) -> assertThrows( IllegalStateException.class, ( ) ->
								ops.setImmediate( errLog, LABELS_MAP ) )
				);
			}
			
			/**
			 Tests Operands Fail to Assemble, and Correct Err Msg Provided.
			 <p>Determines The error, based on if the Ops is a Jump Type
			 <p>Or If the label is not in the {@link #LABELS_MAP}
			 */
			private void assertFailAssemble_LabelPtr (@NotNull Operands ops, String label) {
				assertFalse( ops.setImmediate( errLog, LABELS_MAP ) );
				if ( LABELS_MAP.containsKey( label ) ) {
					int addr=LABELS_MAP.get( label );
					if ( ops.getInstrType( )==Operands.InstrType.J ) {
						AddressValidation.isSupportedInstrAddr( addr, expectedErrs );
						expectedErrs.appendEx( FMT_MSG.label.points2Invalid( label, "Instruction" ) );
					} else {
						AddressValidation.isSupportedDataAddr( addr, expectedErrs );
						expectedErrs.appendEx( FMT_MSG.label.points2Invalid( label, "Data" ) );
					}
				} else {
					expectedErrs.appendEx( FMT_MSG.label.labelNotFound( label ) );
				}
			}
			
			/**
			 Tests Operands Values Match Given. Then, Assembling will cause an Error to be Thrown. {@link Operands#setImmediate(ErrorLog,
					HashMap)}
			 */
			private void assertNotNullOperandsEqual_And_ThrowsAssemble (Operands ops, InstrType type, Integer imm, Integer rd, Integer rs, Integer rt) {
				assertNotNullOperandsEqual( ops, type, null, imm, rd, rs, rt );
				assertThrows( IllegalArgumentException.class, ( ) -> ops.setImmediate( errLog, LABELS_MAP ) );
			}
			private void assertNotNullOperandsEqual (Operands ops, Operands.InstrType type, String label, Integer imm, Integer rd, Integer rs, Integer rt) {
				assertNotNull( ops );
				assertEquals( type, ops.getInstrType( ) );
				assertAll( ( ) -> assertEquals( label, ops.getLabel( ) ),
						   ( ) -> assertEquals( imm, ops.getImmediate( ) ),
						   ( ) -> assertEquals( rd, ops.getRd( ) ),
						   ( ) -> assertEquals( rs, ops.getRs( ) ),
						   ( ) -> assertEquals( rt, ops.getRt( ) )
				);
			}
			private void assertNotNullOperandsEqual (Operands ops, String opcode, String label, Integer rt) {
				assertNotNullOperandsEqual( ops, type( opcode ),label,null,null,null,rt );
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
				Operands operands=opsVal.splitValidOperands( -20, opcode, I.BRANCH.OPS_LABEL );
				expect.assertFailAssemble_LabelPtr( operands, "instr" );
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
				expect.operandsForOpcodeNotValid( 500, opcode, null );
			}
			
			@Nested
			class Operand_Spacing {
				// ANY Spacing At The Beginning/ End Of the Line
				// Spacing of larger than 1  internally
				
				// In the Logger Output, Tabs and NewLn are removed ???
				
				@Test
				void R_Type__Leading_Trailing_And_Internal_Spaces ( ) {
					expect.notRecognised( 42, " r1", "  r2", "r3 " );
					expect.operandsForOpcodeNotValid( 42, "add", " r1,   r2 , r3 " );
					
				}
				
				@Test
				void SingleTabs_Valid ( ) {
					Operands operands=opsVal.splitValidOperands( 12, "sub", "s8,\tr3,\t$8" );
					expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, Operands.InstrType.R,
																		  null, 30, 3, 8 );
				}
				@Test
				void DoubleTabs_OrLeading_Trailing_Tabs_Invalid ( ) {
					expect.notRecognised( 12, "\t$24", "\t$16" );
					expect.operandsForOpcodeNotValid( 12, "sub", "\t$24,\tr20\t,\t\t$16" );
				}
				
				@Test
				void NewLine_Invalid ( ) {
					expect.notRecognised( 67, "R0" );
					expect.operandsForOpcodeNotValid( 67, "sub", "t8\t,r20,\nR0" );
				}
				
			}
			
		}
		
		@Nested
		class No_Operands {
			
			@ParameterizedTest ( name="Valid {index} - opcode\"{0}\"" + TA )
			@ArgumentsSource ( NO_OPS.class )
			void assemble_ValidOperands (String opcode) {
				Operands operands=opsVal.splitValidOperands( 12, opcode, " " );
				expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, InstrType.R, null, null, null, null );
			}
			
			@Test
			void comments_Not_Removed ( ) {
				assertThrows( IllegalStateException.class, ()-> opsVal.splitValidOperands(20,"j"," #"));
				
			}
			
			@Test
			void operands_EdgeCase (){
				assertThrows( IllegalArgumentException.class, ()-> new Operands("panda"," "));
				assertThrows( IllegalArgumentException.class, ()-> new Operands("lw", 20,""));
				assertThrows( IllegalArgumentException.class, ()-> new Operands("panda", 20,20,20));
				
				Operands temp = new Operands("lw", 2,null,null);
				expect.assertNotNullOperandsEqual_And_ThrowsAssemble(temp,expect.type( "lw" ),null,null,2,null  );
				
				assertEquals("Operands{ instrType= R, immediateSet= false, "+
							 "rs= null, rt= null, rd= null, immediate= null, label= 'null' }",
							 Operands.getExit().toString() );
			}
			
		}
		
		@Nested
		class Register_Type {
			
			@ParameterizedTest ( name="Valid {index} - opcode\"{0}\"" + TA )
			@ArgumentsSource ( RD_RS_RT.class )
			void assemble_ValidOperands (String opcode) {
				Operands operands=opsVal.splitValidOperands( 12, opcode, RD_RS_RT.OPS );
				expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, InstrType.R, null, 1, 1, 1 );
			}
			@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" -  Different Register Names" + TA )
			@ArgumentsSource ( RD_RS_RT.class )
			void testOperands_Add_Sub ( ) {
				Operands operands=opsVal.splitValidOperands( 12, "add", "$8,r31, $s2" );
				expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, Operands.InstrType.R,
																	  null, 8, 31, 18 );
			}
			
			@ParameterizedTest ( name="Valid {index} - opcode [sub], operands \"{0}\" -  Mixed Spacing" + TA )
			@ValueSource ( strings={ "$24 , $s4, $s0", "t8,r20,\t$16" } )
			void testOperands_R_Type_Spacing (String ops) {
				Operands operands=opsVal.splitValidOperands( 12, "sub", ops );
				expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, Operands.InstrType.R,
																	  null, 24, 20, 16 );
			}
			
			@Nested
			class Invalid_Operands {
				
				@ParameterizedTest ( name="Multiple Invalid {index} - opcode\"{0}\" And ZeroWriteWarning" )
				@ArgumentsSource ( RD_RS_RT.class )
				void multipleInvalid_ZWW ( ) {
					expect.notRecognised( 76, "$panda", "31" );
					expect.operandsForOpcodeNotValid( 76, "add", "zero, $panda, 31" );
					testLogs.zeroWarning( 76, "zero" );
				}
			}
			
		}
		
		@Nested
		class Immediate_Type {
			
			@Nested
			@DisplayName ( "RT, RS, IMM" )
			class RT_RS_IMM {
				
				@ParameterizedTest ( name="Valid {index} - opcode\"{0}\"" + TA )
				@ArgumentsSource ( I.RT_RS_IMM.class )
				void assemble_ValidOperands (String opcode) {
					Operands operands=opsVal.splitValidOperands( 12, opcode, I.RT_RS_IMM.OPS );
					expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, InstrType.I_rt_write, -40, null, 1, 1 );
				}
				
				@Nested
				class Invalid_Operands {
					
					@Test
					@DisplayName ( "Invalid Operands I_RT_RS_IMM (ADDI), given I_RT_Imm" )
					void invalidOperands_I_RT_RS ( ) { expect.operandsForOpcodeNotValid( 52, "addi", "$1, 0x20" ); }
					
					@ParameterizedTest ( name="Multiple Invalid {index} - opcode\"{0}\" And ZeroWriteWarning" )
					@ArgumentsSource ( I.RT_MEM.class )
					void multipleInvalid_ZWW ( ) {
						Operands operands=opsVal.splitValidOperands( 30, "addi", "$0, $panda, 32769" );
						// Errors with all Operands
						assertNull( operands );
						testLogs.expectErrors(
								30,
								FMT_MSG.reg._NotRecognised( "$panda" ),
								FMT_MSG.imm.notSigned16Bit( 32769 ) + "!",
								_opsForOpcodeNotValid( "addi", "$0, $panda, 32769" )
						);
						testLogs.zeroWarning( 30, "$0" );
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
				private void possibleZeroWarning (String opcode, int lineNo, String regName) {
					if ( expect.type( opcode )==InstrType.I_rt_write )
						testLogs.zeroWarning( lineNo, regName );
				}
				
				@Nested
				@DisplayName ( "Valid" )
				class Valid {
					
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Label" + A )
					@ArgumentsSource ( I.RT_MEM.class )
					void assemble_ValidOperands_Label (String opcode) {
						Operands operands=opsVal.splitValidOperands( 12, opcode, "$9, data" );
						expect.assertNotNullOperandsEqual( operands, opcode , "data", 9 );
						expect.assertAssembles_Label_Successfully( operands, 0x10010000/4 );
						//MAX
						Operands operands2=opsVal.splitValidOperands( 12, opcode, "r2, data_top" );
						expect.assertNotNullOperandsEqual( operands2, opcode , "data_top", 2 );
						expect.assertAssembles_Label_Successfully( operands2, 0x100107F8/4 );
					}
					
					
					@Nested
					class Throws_Assemble {
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Imm(RS)" + TA )
						@ArgumentsSource ( I.RT_MEM.class )
						void assemble_ValidOperands (String opcode) {
							Operands operands=opsVal.splitValidOperands( 12, opcode, I.RT_MEM.OPS_IMM_RS );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, expect.type( opcode ),
																				  -8, null, 1, 1 );
						}
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: NoImm" + TA )
						@ArgumentsSource ( I.RT_MEM.class )
						@Tag ( Tags.MULTIPLE )
						@DisplayName ( "Test Operands, Base+Offset" )
						void assemble_ValidOperands_BaseOffset_NoImm (String opcode) {
							// No Imm
							Operands operands=opsVal.splitValidOperands( 0, opcode, "$6, ($1)" );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, expect.type( opcode ), 0, null, 1, 6 );
							// No Imm or RS
							Operands operands1=opsVal.splitValidOperands( 0, opcode, "$8,  ()" );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands1, expect.type( opcode ), 0, null, 0, 8 );
						}
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: Int" + TA )
						@ArgumentsSource ( I.RT_MEM.class )
						@Tag ( Tags.MULTIPLE )
						@DisplayName ( "Test Operands, Base+Offset" )
						void assemble_ValidOperands_BaseOffset_INT (String opcode) {
							Operands operands=opsVal.splitValidOperands( 0, opcode, "$5, 20 ($1)" );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, expect.type( opcode ), 20, null, 1, 5 );
							// No RS
							Operands operands1=opsVal.splitValidOperands( 0, opcode, "$7, -800 ()" );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands1, expect.type( opcode ), -800, null, 0,
																				  7 );
						}
						
						
						@ParameterizedTest ( name="Valid {index} - opcode\"{0}\" Base+Offset :: HEX" + TA )
						@ArgumentsSource ( I.RT_MEM.class )
						@Tag ( Tags.MULTIPLE )
						@DisplayName ( "Test Operands, Base+Offset" )
						void assemble_ValidOperands_BaseOffset_Hex (String opcode) {
							Operands operands=opsVal.splitValidOperands( 0, opcode, "$5, 0x290($8)" );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands, expect.type( opcode ), 656, null, 8, 5 );
							// No RS
							Operands operands1=opsVal.splitValidOperands( 0, opcode, "$7, 0xFFFF8000 ()" );
							expect.assertNotNullOperandsEqual_And_ThrowsAssemble( operands1, expect.type( opcode ), -32768, null, 0,
																				  7 );
						}
						
					}
					
				}
				
				@Nested
				class Invalid_Operands {
					
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", NonData Label" + FA )
					@ArgumentsSource ( I.RT_MEM.class )
					void NonData_Label (String opcode) {
						for ( String label : InstrProvider.KeysExcluding( "data","data_top" ) ) {
							Operands operands=opsVal.splitValidOperands( 12, opcode, "$2," + label );
							expect.assertNotNullOperandsEqual( operands, opcode , label,2 );
							expect.assertFailAssemble_LabelPtr( operands, label );
						}
					}
					
					@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\" LabelNotFound And ZeroWriteWarning" )
					@ArgumentsSource ( I.RT_MEM.class )
					void LabelNotFound_ZWW (String opcode) {
						Operands operands=opsVal.splitValidOperands( 30, opcode, "$0, panda" );
						
						expect.assertNotNullOperandsEqual( operands, opcode ,"panda",0 );
						expect.assertFailAssemble_LabelPtr( operands, "panda" );
						possibleZeroWarning( opcode, 30, "$0" );
					}
					
					
					@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\" Invalid Integer" )
					@ArgumentsSource ( I.RT_MEM.class )
					void invalid_Integer (String opcode) {
						Operands operands=opsVal.splitValidOperands( 23, opcode, "$1, 1.5" );
						// Errors with all Operands
						expectedErrs.appendEx( 23, FMT_MSG.imm.notValInt( "1.5" ) );
						expectedErrs.append( 23, _opsForOpcodeNotValid( opcode, "$1, 1.5" ) );
						
						expectedErrs.appendEx( 23, FMT_MSG.imm.notValInt( "1.5" ) );
						expect.operandsForOpcodeNotValid( 23, opcode, "$1, 1.5" );
					}
					
					@ParameterizedTest ( name="Invalid {index} - opcode\"{0}\" No ImmRS Found" )
					@ArgumentsSource ( I.RT_MEM.class )
					void NoImmRS_NotFound (String opcode) {
						expectedErrs.appendEx( 78, "\t\tNo Imm(RS) found" );
						expect.operandsForOpcodeNotValid( 78, opcode, "$1," );
					}
					
				}
				
				@Nested
				@DisplayName ( "Imm(RS) method" )
				class immRS {
					@BeforeEach
					void setUp ( ) {
						opsVal.setLineNo( -1 );
						opsVal.setOpcode( "lw" );// valid opcode for ImmRS
					}
					
					@Nested
					@DisplayName ( "Valid Imm(RS)" )
					class validImmRS {
						
						@ParameterizedTest ( name="Valid no RS, no IMM {index}, Just Brackets[{0}]] -> Default" )
						@ValueSource ( strings={ "( )", "()" } )
						void onlyBrackets (String immRS) {
							assertImmRS( opsVal.rt_ImmRs( 9, immRS ), 0, 0 );
						}
						private void assertImmRS (Operands ops, int imm, int rs) {
							assertNotNull( ops );
							assertAll(
									( ) -> assertEquals( imm, ops.getImmediate( ) ),
									( ) -> assertEquals( rs, ops.getRs( ) )
							);
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
							Operands ops=opsVal.rt_ImmRs( 7, immRS );    // Imm
							assertNull( ops );
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
					Operands operands=opsVal.splitValidOperands( 60, opcode, "instr" );
					expect.assertNotNullOperandsEqual(operands, expect.type(opcode), "instr",
													  null, null, null, null);
					expect.assertAssembles_Label_Successfully( operands, 0x00400000/4);
					//MAX
					Operands operands2=opsVal.splitValidOperands( 60, opcode, "instr_top" );
					expect.assertAssembles_Label_Successfully( operands2, 0x00500000/4);
				}
				
				@Nested
				class Throws_Assemble {
					
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Jump_Type :: Imm" + TA )
					@ArgumentsSource ( J.class )
					void assemble_ValidOperands_Imm (String opcode) {
						Operands operands=opsVal.splitValidOperands( 0, opcode, "1048576" );
						expect.assertNotNullOperandsEqual_And_ThrowsAssemble(operands, expect.type(opcode),
																			 1048576,null,null,null);
						//MAX
						Operands operands2=opsVal.splitValidOperands( 0, opcode, "1310720" );
						expect.assertNotNullOperandsEqual_And_ThrowsAssemble(operands2, expect.type(opcode),
																			 1310720,null,null,null);
					}
					@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", Jump_Type :: Hex" + TA )
					@ArgumentsSource ( J.class )
					void assemble_ValidOperands_Hex (String opcode) {
						Operands operands=opsVal.splitValidOperands( 0, opcode, "0x00100000" );
						expect.assertNotNullOperandsEqual_And_ThrowsAssemble(operands, expect.type(opcode),
																			 1048576,null,null,null);
						//MAX
						Operands operands2=opsVal.splitValidOperands( 0, opcode, "0x00140000" );
						expect.assertNotNullOperandsEqual_And_ThrowsAssemble(operands2, expect.type(opcode),
																			 1310720,null,null,null);
					}
					
				}
				
			}
			
			@Nested
			class Invalid_Operands {
				
				@ParameterizedTest ( name="IO {index}Jump _Immediate[{0}] - Out of Range" )
				@ArgumentsSource (  Immediate.ConvertInvalid.OutOfRange.class )
				void testInvalid_OperandsJump_ImmOutOfRange (String hex, int imm) {
					Operands operands=opsVal.splitValidOperands( 0, "j", "" + imm );
					assertNull( operands );
					
					expectedErrs.appendEx( 0, FMT_MSG.imm.notUnsigned26Bit(imm) );
					expectedErrs.append( 0, _opsForOpcodeNotValid( "j", "" + imm ) );
				}
				
				
				// Immediate Values <(0x00100000) & >(0x00140000) Convert To Valid Addresses, but not Valid for Jump
				@ParameterizedTest ( name="[{index}] Invalid 26BitImm[{2}, {3}] for for Jump Instruction" )
				@ArgumentsSource ( Immediate.Instr_Imm.Invalid.class )
				@ArgumentsSource ( Immediate.u_26Bit.class )
				@Tag( Tags.MULTIPLE )
				void testInvalid_OperandsJump_ValImm (String hexAddr, long addr, String hexImm, long imm) {
					assertEquals(addr, imm*4); // Test Variables Invalid if False
					
					int a=(int) imm*4;
					String err="Instruction Address: \"" +hexAddr + "\" Not "+ ((a>=0x10000000 || a<0x00400000) ? "Valid" : "Supported");
					expectedErrs.appendEx( err );
					expect.operandsForOpcodeNotValid(97, "j", "" + imm);
					
					//Hex
					expectedErrs.appendEx( err );
					expect.operandsForOpcodeNotValid(120, "j", "" + imm);
				}
				
				@Test
				@DisplayName ( "Test Invalid Operands, Jump _TooManyOperands" )
				void testInvalid_OperandsJump_TooManyOperands ( ) {
					expect.operandsForOpcodeNotValid( "j", "0x100009, 50" );
				}
				
				@ParameterizedTest ( name="Valid {index} - opcode\"{0}\", NonInstr Label" + FA )
				@ArgumentsSource ( J.class )
				void NonData_Label (String opcode) {
					for ( String label : InstrProvider.KeysExcluding( "instr","instr_top" ) ) {
						Operands operands=opsVal.splitValidOperands( 12, opcode, label );
						expect.assertNotNullOperandsEqual( operands, opcode , label,null );
						expect.assertFailAssemble_LabelPtr( operands, label );
					}
				}
			}
			
			
			@Nested
			class Instr_Label_Or_Imm {
				
				@ParameterizedTest ( name="[{index}] Immediate[{0}], Valid for Jump" )
				@ArgumentsSource(Immediate.Instr_Imm.class )
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
		
		@ParameterizedTest ( name="NO_zeroWarning[{index}]  on Read, Reg[{0}]" )
		@ArgumentsSource ( Registers.ZERO.class )
		void zeroWarning_Read (String regName) {
			//isValidLoadRegister
			assertNotNull( opsVal.convertRegister( regName, DataType.NORMAL ) );
		}
		
		@ParameterizedTest ( name="zeroWarning[{index}] on Write, Reg[{0}]" )
		@ArgumentsSource ( Registers.ZERO.class )
		void zeroWarning_Write (String regName) {
			opsVal.setLineNo( -1 );
			assertNotNull( opsVal.convertWriteRegister( regName, DataType.NORMAL ) );
			testLogs.zeroWarning( -1, regName );
		}
		
	}
	
}
