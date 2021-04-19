package model;

import _test.Tags;
import _test.TestLogs;
import _test.TestLogs.FMT_MSG;

import _test.providers.BlankProvider;
import _test.providers.ImmediateProvider;
import _test.providers.InstrProvider;
import _test.providers.SetupProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

import model.components.DataMemory;
import model.components.InstrMemory;

import util.logs.ErrorLog;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag( Tags.Pkg.MOD )
@Tag( Tags.MEM )
@DisplayName( Tags.Pkg.MOD+" : "+Tags.MEM+"Builder Test" )
class MemoryBuilderTest {
	private static final String INT_ZERO="0";
	private static final String DEC_ZERO="0.0";
	private static final String ASCII_DATA="\"Example of ASCII TEXT\0\"";
	private static final String ASCIIZ_DATA="\"Example of ASCII TEXT\"";
	private static final String BLANK="      ";
	private static final String WORD=".word";
	private static final double DELTA=0.000001d; //5sf
	private static final double DATA_MAX=DataMemory.MAX_DATA_ITEMS;
	private static final List<String> inValid_For_Word_Type=
			Arrays.asList( "" + Double.MAX_VALUE, "" + Double.MIN_VALUE, DEC_ZERO, ASCII_DATA, ASCIIZ_DATA );
	private static MemoryBuilder mb;
	private static TestLogs testLogs;
	private static ErrorLog errors;
	
	@BeforeAll
	static void beforeAll() {
		testLogs = new TestLogs();
		errors=testLogs.actualErrors;
		mb=new MemoryBuilder( errors, testLogs.actualWarnings );
	}
	
	@AfterEach
	void tearDown ( ) {
		testLogs.after( );
		mb.clear();
	}
	
	@Test
	void Testing_Null_Inputs ( ) {
		// Null Label
		assertDoesNotThrow( ( ) -> mb.pushLabel( null ) );
		
		// Null DataType
		assertTrue( mb.addData( null, BLANK, errors ) );
		
		// Nul Instruction
		// Null Opcode - Already Tested
		// Null Operands - Already Tested
		
		// Null Data
		// Word - Already Tested
		// Range - Already Tested
		// CSV - Not Possible to be Null, might be blank - Tested
	}
	
	@Nested
	@Tag( Tags.MUT )
	class Add_Data {
		
		@Nested
		class Single_Word {
			@ParameterizedTest ( name="[{index}] Adding Single Valid Word[{1}]" )
			@ArgumentsSource ( ImmediateProvider._32Bit.class )
			void Add_Single_Data_Word (String hexWord, int intWord) {//Int
				assertTrue( mb.addData( WORD, ""+intWord, errors ) );
				//mb.retrieveData().size()-1  most recently added data is at the index "size()-1"
				// Data in DataManager is stored as double, so Delta is necessary
				assertEquals( Integer.parseInt( ""+intWord ), mb.retrieveData( ).get( mb.retrieveData( ).size( ) - 1 ), DELTA );
			}
			
			@ParameterizedTest ( name="[{index}] Adding Single Valid Word[{0}]" )
			@ArgumentsSource ( ImmediateProvider._32Bit.class )
			void Add_Single_Data_Word_HEX (String hexWord) {//Hex
				assertFalse( mb.addData( WORD, hexWord, errors ) );	//TODO - Hex Integer Support in Memory
				//assertEquals( Integer.decode( hexWord ).doubleValue( ), mb.retrieveData( ).get( mb.retrieveData( ).size( ) - 1 ), DELTA );
				testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValFor_WordType( hexWord ));
			}
			
			@Test
			@Tag ( Tags.MULTIPLE )
			void Invalid_Wrong_DataType ( ) {
				ArrayList<String> invalid=new ArrayList<>( inValid_For_Word_Type );
				invalid.add( BLANK + INT_ZERO + BLANK ); // ValidWord with multiple spaces surrounding it makes it invalid
				invalid.add( DEC_ZERO );
				invalid.add( ASCII_DATA );
				invalid.add( ASCIIZ_DATA );
				for ( String inv : invalid ) {
					assertFalse( mb.addData( WORD, inv, errors ) );
					testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValFor_WordType( inv ));
				}
				assertTrue( mb.retrieveData( ).isEmpty( ) ); // No Data Successfully Added
			}
			
			@ParameterizedTest ( name="[{index}] Adding Single Valid Word[{1}]" )
			@ArgumentsSource ( ImmediateProvider._32Bit.Invalid.class )
			void Invalid_Not_Signed_Int (String hexWord, long longWord) {
				assertFalse( mb.addData( WORD, ""+longWord, errors ) );
				testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValSignedInt( ""+longWord ));
				assertTrue( mb.retrieveData( ).isEmpty( ) ); // Data Not Added
			}
			
			@ParameterizedTest ( name="[{index}] Invalid AddData Blank/Null Input: [{0}]" )
			@ArgumentsSource ( BlankProvider.NullNwLn.class )
			void Invalid_AddData_BlankOrNull (String input) {
				assertFalse( mb.addData( WORD, input, errors ) );
				testLogs.expectedErrors.appendEx( FMT_MSG.data.NoDataGiven_Word );
			}
			
		}
		
		@Nested // Add Range   <int_val>:<int_n>
		class Range_Word {
			
			@ParameterizedTest ( name="[{index}] Add Data-Range int_N:[{0}]" )
			@ValueSource ( strings={ "20", "200", "50", "5" } )
			void AddData_Range_Word (int range_N) {
				assertTrue( mb.addData( WORD, 20 + ":" + range_N, errors ) ); // Added Successfully
				
				assertEquals( 20.0, mb.retrieveData( ).get( mb.retrieveData( ).size( ) - 1 ), DELTA );
			}
			
			@Test
			void Invalid_AddData_Range_Zero ( ) {
				assertTrue( mb.addData( WORD, 20 + ":" + INT_ZERO, errors ) );
				// Adding a Range of "0" Elements, should return true, but not actually affect the Data.
				assertTrue( mb.retrieveData( ).isEmpty( ) );
				// No Errors for this scenario
				//TODO - A Warning could be issued for this scenario
			}
			
			@ParameterizedTest ( name="[{index}] Invalid AddRange Negative:<int_N>:[{0}]" )
			@ArgumentsSource ( ImmediateProvider._32Bit.Negative.class )
			void Invalid_AddData_Range_Negative (String hexWord, int intWord) {
				assertFalse( mb.addData( WORD, 20 + ":" + intWord, errors ) );
				testLogs.expectedErrors.append( FMT_MSG.data.N_MustBePosInt(intWord) );
			}
			
			@Test
			void Invalid_AddRange_TooLarge( ) {
				// after MAX_DATA Items, no more values should be stored, and should return False.
				String five="5"; // set value to 5, so it is verifiable it has changed from default 0.
				
				assertFalse( mb.addData( WORD, five + ":" + Integer.MAX_VALUE, errors ) );
				
				HashMap<Integer, Double> data=mb.retrieveData( );
				
				assertEquals( DataMemory.MAX_DATA_ITEMS, data.size( ) ); // size stops increasing at the limit
				// first 256 items should be set // All
				for ( int i=0; i<data.size( ); i++ ) {
					assertEquals( 5.0, data.get( i ), DELTA );
				}
				
			}
			
			@Test
			void InvalidFormat_AddData_Range ( ) {
				String data="-567 75"; // missing :
				assertFalse( mb.addData( WORD, data, errors ) );
				testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValFor_WordType( data ));
			}
			
			@Test
			void InvalidFormat_AddData_Range_Null ( ) {
				assertFalse( mb.addData( WORD, ":", errors ) );
				testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValSignedInt("" ));
				testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValSignedInt("" ));
			}
		}
		
		@Nested
		class CSVArray_Word {
			
			@Test
			void AddData_CSVArray ( ) {
				// build array of MAX_DATA items
				Random random = new Random();
				ArrayList<Double> copy=new ArrayList<>( );
				int val=random.nextInt( );
				StringBuilder arrCSV=new StringBuilder( "" + val );
				copy.add( (double) val );    // value 1
				for ( int c=1; c<DATA_MAX; c++ ) {    // N-1 commas = N values
					val=random.nextInt( );
					arrCSV.append( " , " ).append( val );
					copy.add( (double) val );
				}
				
				assertTrue( mb.addData( WORD, arrCSV.toString( ), errors ) );
				// Data in DataManager is stored as double, so Delta is necessary
				HashMap<Integer, Double> data=mb.retrieveData( );
				
				for ( int i=0; i<data.size( ); i++ ) {
					assertEquals( copy.get( i ), data.get( i ), DELTA );
				}
			}
			
			@Test
			void Invalid_AddData_CSVArray_TooLarge( ) {
				// Trying to use MAX_VALUE leads to Java running out of memory
				int TEST_LIMIT=(int) (DATA_MAX*2); // value of DATA_MAX(256) *4 causes StackOverflow
				// build array of MAX_DATA items
				ArrayList<Double> copy=new ArrayList<>( );
				int val=0;
				StringBuilder arrCSV=new StringBuilder( "" + val );
				copy.add( (double) val );    // value 1
				for ( int c=0; c<TEST_LIMIT; c++ ) {    // N-1 commas = N values
					arrCSV.append( ", " ).append( val+1 );
					
					if ( c<DATA_MAX ) // only copy up to DATA_MAX
						copy.add( (double) val+1 );
					val++;
				}
				
				assertFalse( mb.addData( WORD, arrCSV.toString( ), errors ) );
				testLogs.expectedErrors.appendEx( "CSV Too Large, stopped parsing at 256th Segment" );
				// Data in DataManager is stored as double, so Delta is necessary
				HashMap<Integer, Double> data=mb.retrieveData( );
				
				assertEquals( DataMemory.MAX_DATA_ITEMS, data.size( ) ); // expect stops  adding items at size limit
				
				for ( int i=0; i<data.size( ); i++ ) {
					assertEquals( copy.get( i ), data.get( i ), DELTA );
				}
			}
			
			@Test
			void InvalidFormat_CSVArray ( ) {
				String input="-567, 800, 5, 75 89, 100, - 9"; // missing , between 75 & 89
				assertFalse( mb.addData( WORD, input, errors ) );
				
				testLogs.expectedErrors.appendEx(FMT_MSG.data.NotValSignedInt( "75 89",3 ));
				// Still Catches further errors in the line
				testLogs.expectedErrors.appendEx(FMT_MSG.data.NotValSignedInt( "- 9",5 ));
				
				// reminder of values should have been placed
				HashMap<Integer, Double> data=mb.retrieveData( );
				assertEquals( 4, data.size( ) );
				assertEquals( -567, data.get( 0 ), DELTA );
				assertEquals( 800, data.get( 1 ), DELTA );
				assertEquals( 5, data.get( 2 ), DELTA );
				assertEquals( 100, data.get( 3 ), DELTA );
			}
			
			@Test
			void InvalidFormat_CSVArray_BlankSegment ( ) {
				String input="-567,,"; // missing , between 75 & 89
				assertFalse( mb.addData( WORD, input, errors ) );
				
				testLogs.expectedErrors.appendEx(FMT_MSG.data.NotValSignedInt( "",1 ));
				// Still Catches further errors in the line
				testLogs.expectedErrors.appendEx(FMT_MSG.data.NotValSignedInt( "",2 ));
				
				// reminder of values should have been placed
				HashMap<Integer, Double> data=mb.retrieveData( );
				assertEquals( 1, data.size( ) );
				assertEquals( -567, data.get( 0 ), DELTA );
			}
		}
		
		
		@ParameterizedTest ( name="{index} - {arguments} _dataType" )
		@ArgumentsSource ( SetupProvider.InvalidDataTypes.class )
		void Invalid_AddData_Invalid_DataType (String dataType) {
			assertThrows( IllegalStateException.class, ( ) -> mb.addData( dataType, "20", errors ) );
			assertTrue( mb.retrieveData( ).isEmpty( ) );
		}
		
	}
	
	@Nested
	@Tag( Tags.MUT )
	class Add_Instruction {
		
		@Test
		void Add_Successfully ( ) {
			assertTrue( mb.addInstruction( 2,"exit", null) );
			
			ArrayList<Instruction> instrList = mb.assembleInstr( errors );
			assertEquals( 1, instrList.size() );
			assertEquals( new Nop("exit"), instrList.get( 0 ));
		}
		
		@Test
		void Null_Opcode ( ) {
			assertTrue( mb.addInstruction( 2,null, null) );
			mb.assembleInstr( errors );
			testLogs.expectedErrors.appendEx( "No Instructions Found" );
		}
		
		@Test
		void Invalid_Opcode ( ) {
			assertTrue( mb.addInstruction( 5,"panda", "$1,$1,$1") );
			testLogs.expectedErrors.appendEx( 5, FMT_MSG.Opcode_NotSupported( "panda" ) );
			
			mb.assembleInstr( errors );
			testLogs.expectedErrors.appendEx( "No Instructions Found" );
		}
		
		@Test
		void Null_Operands ( ) {
			// Null Operands is invalid for Some instructions
			assertTrue( mb.addInstruction( 3,"add", null ) );	// Only returns false, if Instr Limit has been reached
			testLogs.appendErrors( 3, FMT_MSG._NO_OPS,
								   FMT_MSG._opsForOpcodeNotValid( "add", null ) );
		}
		@Test
		void Null_Operands_NOP ( ) {
			// Null Operands valid for NOP
			assertTrue( mb.addInstruction( 2,"exit", null) );
			
			ArrayList<Instruction> instrList = mb.assembleInstr( errors );
			assertEquals( 1, instrList.size() );
			assertEquals( new Nop("exit"), instrList.get( 0 ));
		}
		
		@Test
		void Invalid_Operands ( ) {
			assertTrue( mb.addInstruction( 20,"add", "panda" ) );	// Only returns false, if Instr Limit has been reached
			testLogs.appendErrors( 20, FMT_MSG._opsForOpcodeNotValid( "add", "panda" ) );
			
			mb.assembleInstr( errors );
			testLogs.expectedErrors.appendEx( "No Instructions Found" );
		}
		
		@Test
		void Add_Instructions_TooMany ( ) {
			for ( int i=0; i<InstrMemory.MAX_INSTR_COUNT; i++ ) {
				mb.addInstruction( 14,"exit", null );
			}
			assertFalse(mb.addInstruction( 15,"exit",null ));
		}
		
	}
	
	@Nested
	class Push_Labels {
		
		@Test
		void PushLabels_ValidData ( ) {
			// Upon Pushing a bunch of Labels,
			// Pushing a Valid Word collects the labels N points them to the addr
			mb.pushLabel("panda");// -> #0
			mb.pushLabel("brown_bear");// -> #0
			mb.addData(WORD,"25:3",errors);	// 0x10010000 #0
			
			mb.pushLabel("polar_bear");// -> #4
			mb.addData(WORD,"50", errors);	// 0x10010020 #4
			// Data In Correct Places
			assertEquals(25, mb.retrieveData().get(0));
			assertEquals(50, mb.retrieveData().get(3));
			assertTrue(mb.getLabels().isEmpty()); // No Labels Remaining
			// Labels Point to Expected Addresses
			assertEquals(0x10010000, mb.getLabelMap().get("panda"));
			assertEquals(0x10010000, mb.getLabelMap().get("brown_bear"));
			assertEquals(0x10010018, mb.getLabelMap().get("polar_bear"));
		}
		
		@Test
		void PushLabels_ValidInstr ( ) {
			mb.pushLabel("panda");
			mb.addInstruction(4,"add", InstrProvider.RD_RS_RT.OPS );
			
			assertTrue(mb.getLabels().isEmpty());
			assertEquals(0x00400000, mb.getLabelMap().get("panda"));
		}
		
		@Test
		void PushLabels_Invalid_Followed_By_Valid ( ) {
			mb.pushLabel("panda");
			// Invalid Data
			mb.addData(WORD,"2.0", errors);	// 0x10010000 #0
			testLogs.expectedErrors.appendEx( FMT_MSG.data.NotValFor_WordType( "2.0" ));
			// Valid Instr
			mb.addInstruction(5,"add",InstrProvider.RD_RS_RT.OPS  );
			// Label Points to Instr added After Data
			assertTrue(mb.getLabels().isEmpty());
			assertEquals(0x00400000, mb.getLabelMap().get("panda"));
		}
		
	}
	
	@Nested
	class Assembly {
		
		@Test
		void SuccessfulAssembly ( ) {
			mb.pushLabel("data");
			mb.addData(WORD,"20", errors);
			mb.pushLabel("instr");
			mb.addInstruction(6,"add", InstrProvider.RD_RS_RT.OPS  );
			
			mb.addInstruction(7,"lw", InstrProvider.I.RT_MEM.OPS_LABEL );
			mb.addInstruction(8,"j", InstrProvider.J.OPS_LABEL );
			mb.assembleInstr( errors );
			// Assembles without Errors
		}
		
		@Test
		void FailedAssembly_Label_Points_To_Invalid ( ) {
			ErrorLog expected = testLogs.expectedErrors;
			mb.pushLabel("data");
			mb.addData(WORD,"20", errors);
			mb.pushLabel("instr");
			mb.addInstruction( 9,"add", InstrProvider.RD_RS_RT.OPS );
			
			mb.addInstruction(10,"lw", "$1,instr" );
			mb.addInstruction(11,"j","data" );
			mb.assembleInstr( errors );
			
			expected.appendEx( TestLogs.FMT_MSG.xAddressNot( "Data","0x00400000", "Valid") );
			expected.appendEx( TestLogs.FMT_MSG.label.points2Invalid_Address("instr","Data"  ) );
			
			expected.appendEx( TestLogs.FMT_MSG.xAddressNot( "Instruction","0x10010000", "Valid") );
			expected.appendEx( TestLogs.FMT_MSG.label.points2Invalid_Address("data","Instruction"  ) );
			expected.appendEx( FMT_MSG.FailedAssemble );
			
			//TODO - Highlight Specific Error That Failed Assembly
			// Possibly Adding LineNo to Instr
		}
		@Test
		void FailedAssembly_InvalidInstructionOperands ( ) {
			mb.addInstruction(12,"add", "0"); // Error at Parsing
			mb.assembleInstr( errors );
			testLogs.appendErrors( 12, FMT_MSG._opsForOpcodeNotValid( "add", "0" ) );
			testLogs.expectedErrors.appendEx( "No Instructions Found" );
		}
		
		@Test
		void FailedAssembly_No_Instructions ( ) {
			mb.assembleInstr( errors );
			testLogs.expectedErrors.appendEx( "No Instructions Found" );
		}
		@Test
		void FailedAssembly_Missing_Label ( ) {
			ErrorLog expected = testLogs.expectedErrors;
			mb.addInstruction(13,"j", "x" );
			mb.assembleInstr( errors );
			expected.appendEx( FMT_MSG.label.labelNotFound( "x" ) );
			expected.appendEx( FMT_MSG.FailedAssemble );
		}
	}
	
}
