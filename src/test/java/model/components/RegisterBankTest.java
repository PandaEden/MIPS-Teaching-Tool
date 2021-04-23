package model.components;

import _test.Tags;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import util.ansi_codes.Color;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Tag( _test.Tags.Pkg.MOD )
@Tag( _test.Tags.Pkg.COM )
@DisplayName ( _test.Tags.Pkg.MOD + " : " + Tags.Pkg.COM + " :  RegisterBank Test")
class RegisterBankTest {
	private static final String NAME="\n\t\tRegisterBank:\t";
	private static final String PREFIX="Execution:"+NAME;
	private static final String NO_ACTION=PREFIX + "No Action!\n";
	private final Random random=new Random( );
	private final ExecutionLog log=new ExecutionLog( new ArrayList<>( ) );
	private int[] regs;
	private RegisterBank rb;
	
	@BeforeEach
	void setUp() {
		Color.colorSupport=false;
		RegisterBank.regFormat=RegisterBank.RegFormat.R;
		RegisterBank.fmtUpperCase=true;
		
		regs=new int[ 32 ];        // Setup Register values to any random integer
		for ( int i=1; i<32; i++ ) {    // skip index 0. that should always be ==0;
			int r=random.nextInt( );
			regs[ i ]=r;
		}
		rb=new RegisterBank( regs, log );
	}
	
	@AfterEach
	void tearDown() {
		log.clear( );
	}
	
	// Construction failure
	@Test
	@DisplayName ("Construct With Invalid Size IntArray")
	void constructWithInvalidSizeIntArray() {
		regs=new int[ 30 ]; // !=32
		assertThrows( IllegalArgumentException.class, () -> new RegisterBank( regs, log ) );
	}
	
	@Test
	@DisplayName ("Construct with index0, not Equal 0")
	void constructWithIndexZero_NotEqualZero() {
		regs[ 0 ]=50; // regs[0] !=0
		assertThrows( IllegalArgumentException.class, () -> new RegisterBank( regs, log ) );
	}
	
	@Test
	@DisplayName ("Test NoAction _ RegBank")
	void NoActionRegBank() {
		rb.noAction( );
		assertEquals( NO_ACTION, log.toString( ) );
	}
	
	@Nested
	@Tag ("Output")
	@DisplayName ("Output")
	class Output {
		RegisterBank.RegFormat defaultFormat;
		
		@BeforeEach
		void setUp() { defaultFormat=RegisterBank.regFormat; }    // Save Format
		
		@AfterEach
		void tearDown() { RegisterBank.regFormat=defaultFormat; } // Restore Format
		
		@Test
		@DisplayName ("Format Output")
		void format() {
			RegisterBank.regFormat=RegisterBank.RegFormat.R;
			// Make everything =5 so output is predicable
			String fmt="";
			fmt+="-------- -------- -------- REGISTER-BANK -------- -------- -------- -------- \n";
			fmt+="|R0: 0\tR4: 5\tR8: 5\tR12: 5\t\tR16: 5\tR20: 5\tR24: 5\tR28: 5|\n";
			fmt+="|R1: 5\tR5: 5\tR9: 5\tR13: 5\t\tR17: 5\tR21: 5\tR25: 5\tR29: 5|\n";
			fmt+="|R2: 5\tR6: 5\tR10: 5\tR14: 5\t\tR18: 5\tR22: 5\tR26: 5\tR30: 5|\n";
			fmt+="|R3: 5\tR7: 5\tR11: 5\tR15: 5\t\tR19: 5\tR23: 5\tR27: 5\tR31: 5|\n";
			fmt+="-------- -------- -------- ---- --- ---- -------- -------- -------- -------- \n";
			for ( int i=1; i<regs.length; i++ ) {
				regs[ i ]=5;
			}
			//When-Then
			assertEquals( fmt, rb.format( ) );
			
			//TODO test alternative formats, Named/ Index
		}
		
		@Test
		@DisplayName ("Format Index")
		void formatIndex() {
			RegisterBank.regFormat=RegisterBank.RegFormat.Index;
			rb.read( 1 );
			assertEquals( PREFIX + "Reading Value[" + regs[ 1 ] + "]\tFrom Register Index[$1]!\n",
						  log.toString( ) );
		}
		
		@Test
		@Order (2)
		@DisplayName ("Format $R")
		void format$_R() {
			RegisterBank.regFormat=RegisterBank.RegFormat.$R;
			rb.read( 1 );
			assertEquals( PREFIX + "Reading Value[" + regs[ 1 ] + "]\tFrom Register Index[$R1]!\n",
						  log.toString( ) );
		}
		
		@Test
		@Order (3)
		@DisplayName ("Format R")
		void formatR() {
			RegisterBank.regFormat=RegisterBank.RegFormat.R;
			rb.read( 1 );
			assertEquals( PREFIX + "Reading Value[" + regs[ 1 ] + "]\tFrom Register Index[R1]!\n",
						  log.toString( ) );
		}
		
		@Test
		@Order (4)
		@DisplayName ("Format $Named")
		void format$_Named() {
			RegisterBank.regFormat=RegisterBank.RegFormat.$Named;
			rb.read( 1 );
			assertEquals( PREFIX + "Reading Value[" + regs[ 1 ] + "]\tFrom Register Index[$AT]!\n",
						  log.toString( ) );
		}
		
		@Test
		@Order (5)
		@DisplayName ("Format Named")
		void formatNamed() {
			RegisterBank.regFormat=RegisterBank.RegFormat.Named;
			rb.read( 1 );
			assertEquals( PREFIX + "Reading Value[" + regs[ 1 ] + "]\tFrom Register Index[AT]!\n",
						  log.toString( ) );
		}
		
	}
	
	@Nested
	@Tag ("Accessing")
	@DisplayName ("SingleWord")
	class Read {
		private static final String NO_READ = PREFIX + "No Read!\n";
		@Test
		@DisplayName ("Read RegisterBank_Length")
		void read() {
			for ( int i=0; i<regs.length; i++ ) {
				assertEquals( regs[ i ], rb.read( i ) );
				assertReadingValue( i );
				
				log.clear( );
			}
		}
		
		void assertReadingValue(int index) {
			assertEquals( PREFIX + "Reading Value[" + regs[ index ]
						  + "]\tFrom Register Index[R" + index + "]!\n",
						  log.toString( ) );
		}
		
		@Test
		void Read_Null() {
			assertAll(
					() -> assertEquals( 0, rb.read( null ) ),
					() -> assertEquals( NO_READ, log.toString( ) )
			);
		}
		
		@ParameterizedTest (name="Read - OutOfBounds[{index}] - Index: \"{0}\"")
		@ValueSource (ints={ -1, 32, 64, -20 })
		@DisplayName ("Read OutOfBounds")
		void outOfBounds(int index) {
			//<0 >31
			assertThrows( IndexOutOfBoundsException.class, () -> rb.read( index ) );
		}
		
		@Nested
		class DoubleWord {
			@Test
			void DoubleWord_Read() { // Reading 2 operands
				int[] temp=rb.read( 10, 20 );
				assertAll(
						() -> assertEquals( regs[ 10 ], temp[ 0 ] ),
						() -> assertEquals( regs[ 20 ], temp[ 1 ] ),
						//() -> assertEquals( PREFIX + "Reading Values[" + regs[ 10 ] + ", " + regs[ 20 ] + "]\tFrom Register Indexes[R10, R20]!\n", log.toString( ) )
						() -> assertEquals( PREFIX + "Reading Value[" + regs[ 10 ] + "]\tFrom Register Index[R10]!" +
											NAME+"Reading Value[" + regs[ 20 ] + "]\tFrom Register Index[R20]!\n", log.toString( ) )
				);
			}
			
			@Test
			@DisplayName ("DoubleWord Read - SingleNull - First")
			void doubleRead_SingleNull_0() {
				int[] temp=rb.read( null, 20 );
				assertAll(
						() -> assertEquals( 0, temp[ 0 ] ),
						() -> assertEquals( regs[ 20 ], temp[ 1 ] ),
						() -> assertReadingValue( 20 )
				);
			}
			
			@Test
			@DisplayName ("DoubleWord Read - SingleNull - Second")
			void doubleRead_SingleNull_1() {
				int[] temp=rb.read( 30, null );
				assertAll(
						() -> assertEquals( regs[ 30 ], temp[ 0 ] ),
						() -> assertEquals( 0, temp[ 1 ] ),
						() -> assertReadingValue( 30 )
				);
			}
			
			@Test
			@DisplayName ("DoubleWord Read - DoubleNull")
			void doubleRead_DoubleNull() {
				int[] temp=rb.read( null, null );
				assertAll(
						() -> assertEquals( 0, temp[ 0 ] ),
						() -> assertEquals( 0, temp[ 1 ] ),
						() -> assertEquals( NO_READ, log.toString( ) )
				);
			}
			
			@ParameterizedTest (name="DoubleWord Read - OutOfBounds[{index}] - Indexes: \"{arguments}\"")
			@CsvSource ({ "0, -1", "-1, 0", "-1, -1" })
			@DisplayName ("DoubleRead OutOfBounds")
			void doubleRead_OutOfBounds(int index0, int index1) {
				//<0 >31
				assertThrows( IndexOutOfBoundsException.class, () -> rb.read( index0, index1 ) );
			}
			
		}
		
	}
	
	@Nested
	@Tag ("Mutating")
	@DisplayName ("Store")
	class Store {
		private static final String NO_WRITE=PREFIX + "No Write!\n";
		
		@Test
		void store() {
			// Validate initial state
			for ( int i=0; i<regs.length; i++ ) {
				assertEquals( regs[ i ], rb.read( i ) );
			}
			log.clear( );
			
			int newVal;
			
			for ( int i=1; i<regs.length; i++ ) { // skip 0 - Reminder of Registers should mutate.
				log.clear( );
				newVal=random.nextInt( );
				rb.write( i, newVal );
				assertEquals( PREFIX + "Writing Value[" + newVal + "]\tTo Register Index[*R" + i + "]!\n",
							  log.toString( ) );
				assertEquals( newVal, regs[ i ] );
				assertEquals( newVal, rb.read( i ) );
			}
		}
		
		@Test
		@DisplayName ("Store Zero")
		void zero() {
			rb.write( 0, 57 );
			assertAll(
					() -> assertEquals( 0, regs[ 0 ] ),
					() -> assertEquals( NO_WRITE, log.toString( ) ),
					() -> assertEquals( 0, rb.read( 0 ) )
			);
		}
		
		@Test
		@DisplayName ("Store Null - Index")
		void nullIndex() {
			rb.write( null, 57 );
			assertAll(
					() -> assertEquals( 0, regs[ 0 ] ),
					() -> assertEquals( NO_WRITE, log.toString( ) ),
					() -> assertEquals( 0, rb.read( 0 ) )
			);
		}
		
		@Test
		@DisplayName ("Store Null - Data")
		void nullData() {
			rb.write( 20, null );
			assertAll(
					() -> assertEquals( 0, regs[ 0 ] ),
					() -> assertEquals( NO_WRITE, log.toString( ) ),
					() -> assertEquals( 0, rb.read( 0 ) )
			);
		}
		
		@Test
		@DisplayName ("Store Null - Index & Data")
		void null_IndexAndData() {
			rb.write( null, null );
			assertAll(
					() -> assertEquals( 0, regs[ 0 ] ),
					() -> assertEquals( NO_WRITE, log.toString( ) ),
					() -> assertEquals( 0, rb.read( 0 ) )
			);
		}
		
	}
	
	@Test
	void InvalidRange() {
		assertThrows(IndexOutOfBoundsException.class, ()-> rb.inRange( 400 ));
	}
	
}
