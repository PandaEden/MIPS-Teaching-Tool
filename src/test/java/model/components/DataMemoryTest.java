package model.components;

import _test.Tags;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import util.Convert;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@Tag( Tags.Pkg.MOD )
@Tag( Tags.Pkg.COM )
@Tag( Tags.DATA )
@Tag( Tags.MEM )
@DisplayName ( Tags.Pkg.MOD + " : " + Tags.Pkg.COM + " : "+Tags.DATA+Tags.MEM+" Test" )
class DataMemoryTest {
	private static final ExecutionLog log=new ExecutionLog( new ArrayList<>( ) );
	private static final double DELTA=0.000001d;
	private static DataMemory dataMemory;
	private static HashMap<Integer, Double> data;
	private static Random random;
	
	@BeforeAll
	static void setUp() {
		data=new HashMap<>( );
		dataMemory=new DataMemory( data, log );
		random=new Random( );
	}
	
	@AfterEach
	void clear() {
		data.clear( );
		log.clear( );
	}
	
	//TODO change Get&Set to float, and test with margin of error
	//TODO make Covert helper method for converting between float/int
	
	//writeData
	@ParameterizedTest (name = "[{index}] Valid Read&Write Data (.word) with value {0}")
	@ValueSource (ints={ Integer.MIN_VALUE, -50050, -11, 0, 25, 2375, Integer.MAX_VALUE })
	void valid_ReadWrite_Data_Word(int inputData) {
		// populate 20 entries in the data with random values, and returns a deep copy of the data
		HashMap<Integer, Double> copy=new HashMap<>( );
		for ( int i=0; i<20; i++ ) {
			double value=random.nextInt( );
			copy.put( i, value );    // Possible issue of inputData == randomSignedInt ?
			data.put( i, value );
		}
		
		int index=random.nextInt( 20 ); // random index 0-19
		//after set Data, all values but the value at this index should equal
		int address=DataMemory.BASE_DATA_ADDRESS + index*DataMemory.DATA_ALIGN;
		
		assertAll(
				() -> assertTrue( dataMemory.writeData( address, inputData ) ),
				() -> assertEquals( "Execution:\n\tDataMemory:\t" + "Writing Value[" + inputData
									+ "]\tTo Memory Address[" + Convert.int2Hex( address ) + "]!\n", log.toString( ) ),
				//check set index has changed.
				() -> assertEquals( inputData, data.get( index ), DELTA ),
				() -> assertNotEquals( inputData, copy.get( index ), DELTA ),
				() -> assertNotEquals( copy.get( index ), data.get( index ), DELTA )
		);
		log.clear( );
		
		assertEquals( inputData, dataMemory.readData( address ) );
		assertEquals( "Execution:\n\tDataMemory:\t" + "Reading Value[" + inputData
					  + "]\tFrom Memory Address[" + Convert.int2Hex( address ) + "]!\n", log.toString( ) );
		
		//check other indexes are not changed.
		for ( int i=0; i<data.size( ); i++ ) {
			if ( i!=index ) {
				assertEquals( copy.get( i ), data.get( i ), DELTA );
				assertEquals( dataMemory.readData( DataMemory.BASE_DATA_ADDRESS + i*DataMemory.DATA_ALIGN ),
							  data.get( i ), DELTA );
			}
		}
	}
	
	@Test
	@Tag( Tags.MULTIPLE )
	void Test_Exceptions_Thrown() {
		
		// Provided data HashMap with too many items	// Should really be checking that the provided hashMap is empty perhaps
		for ( int i=0; i<DataMemory.MAX_DATA_ITEMS+1; i++ ) {
			data.put(DataMemory.BASE_DATA_ADDRESS+i*DataMemory.DATA_ALIGN, 5.0 );
		}
		assertAll(
				//writeData null value	-> NoAction
				() -> assertFalse( dataMemory.writeData( (int) 0x10000000L, null ) ),
				() -> assertEquals( "Execution:\n\tDataMemory:\tNo Action!\n", log.toString( ) ),
				
				// Initalizing DataMemory with too many indexes
				() -> assertThrows( IllegalArgumentException.class, () -> new DataMemory(data,log)),
				
				// Invalid Address Range
				//Below valid data address, writeData
				() -> assertThrows( IndexOutOfBoundsException.class, () -> dataMemory.writeData( (int) 0x10000000L, 20 ) ),
				//Below valid data address, readData
				() -> assertThrows( IndexOutOfBoundsException.class, () -> dataMemory.readData( (int) 0x10000000L ) ),
				//Above Valid data address, writeData
				() -> assertThrows( IndexOutOfBoundsException.class, () -> dataMemory.writeData( (int) 0x10010800L, 20 ) ),
				//Above Valid data address, writeData
				() -> assertThrows( IndexOutOfBoundsException.class, () -> dataMemory.readData( ((int) 0x10010800L) ) ),
				
				// Invalid Address Alignment
				//Not Word Aligned, writeData
				() -> assertThrows( IllegalArgumentException.class, () -> dataMemory.writeData( (int) 0x10010004L, 20 ) ),
				//Not Word Aligned, readData
				() -> assertThrows( IllegalArgumentException.class, () -> dataMemory.readData( (int) 0x10010004L ) )
		);
	}
	
	@Test
	@Tag( Tags.ACC )
	void ReadData_From_Unset_Address() {
		final int align=8;
		//set first 20 values to 20 check get(10) returns 20, as expected.
		for ( int i=0; i<20; i++ ) {
			double value=20;
			data.put( i, value );
			assertEquals( 20, dataMemory.readData( (int) 0x10010000L + (i*align) ) );// check all the values are 20
		}
		assertAll(
				// then get to an address other than the first 20, should give 0.
				() -> assertEquals( 0, dataMemory.readData( (int) 0x10010000L + 20*align ) ), // 21st index
				() -> assertEquals( 0, dataMemory.readData( (int) 0x10010800L - align ) ), // last supported index
				() -> assertEquals( 0, dataMemory.readData( (int) 0x10010800L - 200 ) ),  // 156th index
				() -> assertEquals( 0, dataMemory.readData( null ) )  // 156th index
		);
	}
	
	@Test
	@Tag( Tags.OUT )
	@Tag( Tags.MUT )
	void WriteData_To_An_Arbitrary_Address() {
		dataMemory.writeData( DataMemory.OVER_SUPPORTED_DATA_ADDRESS - DataMemory.DATA_ALIGN, 50 );
		assertEquals( 50, data.get( DataMemory.MAX_DATA_ITEMS - 1 ) );
	}
	
	@Test
	@Tag( Tags.OUT )
	@Tag( Tags.ACC )
	void ReadData_From_An_Arbitrary_Address() {
		data.put( DataMemory.MAX_DATA_ITEMS - 1, 250.0 );
		assertEquals( 250, dataMemory.readData( DataMemory.OVER_SUPPORTED_DATA_ADDRESS - DataMemory.DATA_ALIGN ) );
	}
	
	@Test
	@Tag( Tags.OUT )
	void No_Action () {
		dataMemory.noAction( );	// Also clears the previously, last written/read
		assertEquals( "Execution:\n\tDataMemory:\t" + "No Action!\n", log.toString( ) );
	}


}
