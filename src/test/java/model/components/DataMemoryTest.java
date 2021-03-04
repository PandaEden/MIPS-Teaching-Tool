package model.components;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.Convert;
import util.logs.ExecutionLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DataMemoryTest{
	private static final ExecutionLog log = new ExecutionLog(new ArrayList<>());
	private static final double DELTA = 0.000001d;
	private static DataMemory dataMemory;
	private static HashMap<Integer, Double> data;
	private static Random random;
	
	@BeforeAll
	static void setUp(){
		data = new HashMap<>();
		dataMemory = new DataMemory(data, log);
		random = new Random();
	}
	
	@AfterEach
	void clear(){
		data.clear();
		log.clear();
	}
	
	private int randomSignedInt(){
		int rnt = random.nextInt(Integer.MAX_VALUE);
		return random.nextBoolean() ? rnt : (-rnt)-1;
	}
	
	//TODO change Get&Set to float, and test with margin of error
	//TODO make Covert helper method for converting between float/int
	
	//writeData
	@ParameterizedTest
	@ValueSource (ints = {Integer.MIN_VALUE, -50050, -11, 0, 25, 2375, Integer.MAX_VALUE})
	@DisplayName ("valid writeData (.word)")
	void validReadWriteData_Word(int inputData){
		// populate 20 entries in the data with random values, and returns a deep copy of the data
		ArrayList<Double> copy = new ArrayList<>();
		for (int i = 20; i>0; i--) {
			double value = randomSignedInt();
			copy.add(value);    // Possible issue of inputData == randomSignedInt ?
			data.put(i, value);
		}
		
		int index = random.nextInt(20); // random index 0-19
		//after set Data, all values but the value at this index should equal
		int address = DataMemory.BASE_DATA_ADDRESS+index*DataMemory.DATA_ALIGN;
		
		assertAll(
				() -> assertTrue(dataMemory.writeData(address, inputData)),
				() -> assertEquals("Execution:\n\tDataMemory:\t"+"Writing Value["+inputData
						+"]\tTo Memory Address["+Convert.uInt2Hex(address)+"]!", log.toString()),
				//check set index has changed.
				() -> assertEquals(inputData, data.get(index), DELTA),
				() -> assertNotEquals(inputData, copy.get(index), DELTA),
				() -> assertNotEquals(copy.get(index), data.get(index), DELTA)
		);
		log.clear();
		
		assertEquals(inputData, dataMemory.readData(address));
		assertEquals("Execution:\n\tDataMemory:\t"+"Reading Value["+inputData
				+"]\tFrom Memory Address["+Convert.uInt2Hex(address)+"]!", log.toString());
		
		//check other indexes are not changed.
		for (int i = 0; i<data.size(); i++)
			if (i!=index) {
				assertEquals(copy.get(index), data.get(index), DELTA);
				assertEquals(dataMemory.readData(DataMemory.BASE_DATA_ADDRESS+i*DataMemory.DATA_ALIGN),
						data.get(index), DELTA);
				
			}
		
	}
	
	@Test
	@DisplayName ("Test Exceptions, Data Memory")
	void testExceptionsDataMemory(){
		assertAll(
				//writeData null value
				() -> assertFalse(dataMemory.writeData((int) 0x10000000L, null)),
				() -> assertEquals("Execution:\n\tDataMemory:\tNo Action!\n", log.toString()),
				//Below valid data address, writeData
				() -> assertThrows(IndexOutOfBoundsException.class, () -> dataMemory.writeData((int) 0x10000000L, 20)),
				//Below valid data address, readData
				() -> assertThrows(IndexOutOfBoundsException.class, () -> dataMemory.readData((int) 0x10000000L)),
				//Above Valid data address, writeData
				() -> assertThrows(IndexOutOfBoundsException.class, () -> dataMemory.writeData((int) 0x10010800L, 20)),
				//Above Valid data address, writeData
				() -> assertThrows(IndexOutOfBoundsException.class, () -> dataMemory.readData(((int) 0x10010800L))),
				//Not Word Aligned, writeData
				() -> assertThrows(IllegalArgumentException.class, () -> dataMemory.writeData((int) 0x10010004L, 20)),
				//Not Word Aligned, readData
				() -> assertThrows(IllegalArgumentException.class, () -> dataMemory.readData((int) 0x10010004L))
		);
	}
	
	//getData
	@Test
	@DisplayName ("Test readData from unset addresses")
	void testGetDataFromUnsetAddresses(){
		final int align = 8;
		//set first 20 values to 20 check get(10) returns 20, as expected.
		for (int i = 0; i<20; i++) {
			double value = 20;
			data.put(i, value);
			assertEquals(20, dataMemory.readData((int) 0x10010000L+(i*align)));// check all the values are 20
		}
		assertAll(
				// then get to an address other than the first 20, should give 0.
				() -> assertEquals(0, dataMemory.readData((int) 0x10010000L+20*align)), // 21st index
				() -> assertEquals(0, dataMemory.readData((int) 0x10010800L-align)), // last supported index
				() -> assertEquals(0, dataMemory.readData((int) 0x10010800L-200)),  // 156th index
				() -> assertEquals(0, dataMemory.readData(null))  // 156th index
		);
	}
	
	@Test
	@DisplayName ("Test Write Data To Arbitrary Address")
	void testWriteDataToArbitraryAddress(){
		dataMemory.writeData(DataMemory.OVER_SUPPORTED_DATA_ADDRESS-DataMemory.DATA_ALIGN, 50);
		assertEquals(50, data.get(DataMemory.MAX_DATA_ITEMS-1));
	}
	
	@Test
	@DisplayName ("Test Read Data From Arbitrary Address")
	void testReadDataFromArbitraryAddress(){
		data.put(DataMemory.MAX_DATA_ITEMS-1, 250.0);
		assertEquals(250, dataMemory.readData(DataMemory.OVER_SUPPORTED_DATA_ADDRESS-DataMemory.DATA_ALIGN));
	}
//
//	@Test
//	@Disabled
//	@DisplayName ("Test DataMemory LoadModel/GetModel")
//	void testDataMemoryLoad_Get(){
//		//test load - modify data to be all 10.
//		for (int i = 0; i<20; i++) {
//			data.put(i ,(double) 10);
//		}
//
//		// load new data where all data is 5.
//		//noinspection MismatchedQueryAndUpdateOfCollection
//		ArrayList<Float> newData = new ArrayList<>();
//		for (int i = 0; i<20; i++) {
//			newData.add((float) 5);
//		}
//		//Data != 5 before load
//		for (int i = 0; i<20; i++) {
//			assertNotEquals(5, data.get(i), DELTA);
//		}
//
//		// assertTrue(dataMemory.loadModel(newData));
//		//Data should now be == 5
//		for (int i = 0; i<20; i++) {
//			assertEquals(5, data.get(i), DELTA);
//		}
//	}
}