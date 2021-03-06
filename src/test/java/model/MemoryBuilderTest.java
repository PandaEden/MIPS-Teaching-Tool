package model;

import model.components.DataMemory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.logs.ErrorLog;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MemoryBuilderTest{
	private static final String INT_ZERO = "0";
	private static final String TWO_30 = "1073741824"; // value 2^30
	private static final String DEC_ZERO = "0.0";
	private static final String ASCII_DATA = "\"Example of ASCII TEXT\0\"";
	private static final String ASCIIZ_DATA = "\"Example of ASCII TEXT\"";
	private static final String BLANK = "      ";
	private static final String WORD = ".word";
	private static final String DWORD = ".doubleword";
	private static final String DOUBLE = ".double";
	private static final String ASCII = ".ascii";
	private static final String ASCIIZ = ".asciiz";
	
	private static final double DELTA = 0.000001d; //5sf
	private static final double DATA_MAX = DataMemory.MAX_DATA_ITEMS;
	
	private static final ErrorLog errors = new ErrorLog(new ArrayList<>());
	private static final List<String> inValidWords =
			Arrays.asList(""+Double.MAX_VALUE, ""+Double.MIN_VALUE, DEC_ZERO, ASCII_DATA, ASCIIZ_DATA);
	private static final List<String> validRanges = Arrays.asList("20", "200", "50", "5"); // reasonable sizes for range
	private static final ArrayList<String> validPositiveWords = new ArrayList<>(
			Arrays.asList(""+Integer.MAX_VALUE, INT_ZERO, TWO_30));
	private static MemoryBuilder mb;
	private static Random random;
	private static ArrayList<String> validWords;
	private static ArrayList<String> validNegativeWords;
	
	
	@BeforeAll
	static void beforeAll(){
		validWords = new ArrayList<>();
		validNegativeWords = new ArrayList<>();
		
		random = new Random();
		int rand;
		for (int i = 0; i<20; i++) {
			rand = random.nextInt(Integer.MAX_VALUE);
			validPositiveWords.add(""+rand);
		}
		
		int negValue;
		for (String val : validPositiveWords) {
			validWords.add(val);// add positive values to list of valid values
			
			negValue = -Integer.parseInt(val); // inverse value to negative
			negValue--; // -1, since negative range is 1 larger, and -0 is positive
			validNegativeWords.add(""+negValue);    // add to negative values
			
			validWords.add(""+negValue);// add negative values to list of valid values
		}
	}
	
	@BeforeEach
	void setUp(){
		mb = new MemoryBuilder();
		errors.clear(); // should be in TearDown
	}
	
	// --- Add Data
	
	// Add Word
	@Test
	@DisplayName ("Test AddData _Word -Single")
	void testAddDataWordSingle(){
		for (String w : validWords) {
			assertTrue(mb.addData(WORD, w, errors));
			//mb.retrieveData().size()-1  most recently added data is at the index "size()-1"
			// Data in DataManager is stored as double, so Delta is necessary
			assertEquals(Double.parseDouble(w), mb.retrieveData().get(mb.retrieveData().size()-1), DELTA);
		}
	}
	
	@Test
	@DisplayName ("Test Invalid - AddData _Word -Single")
	void testInvalid_AddDataWordSingle_WrongData(){
		ArrayList<String> invalid = new ArrayList<>(inValidWords);
		
		for (String w : validWords) {        // add list of valid word, altered to be invalid
			invalid.add(BLANK+w+BLANK); // added leading/trailing whitespace
		}
		
		for (String inv : invalid) {
			assertFalse(mb.addData(WORD, inv, errors));
			assertEquals("Errors:\n\tData: ["+inv+"], Not Valid For DataType: \""+WORD+"\"!\n",
					errors.toString());
			assertTrue(mb.retrieveData().isEmpty());
			errors.clear();
		}
	}
	
	// Add Range   <int_val>:<int_n>
	@Test
	@DisplayName ("Test AddData _Word -Range")
	void testAddDataWordRange(){
		String range;
		for (String val : validWords) {
			for (String n : validRanges) {
				if (Integer.parseInt(n)<DATA_MAX) {    // Range can't be larger than MAX_DATA
					range = (val+":"+n);
					assertTrue(mb.addData(WORD, range, errors));
					//mb.retrieveData().size()-1  most recently added data is at the index "size()-1"
					// Data in DataManager is stored as double, so Delta is necessary
					assertEquals(Double.parseDouble(val), mb.retrieveData().get(mb.retrieveData().size()-1), DELTA);
					// Reset MB after each run, or cumulative  data will overflow MAX_DATA
					mb = new MemoryBuilder();
				}
			}
		}
	}
	
	@Test
	@DisplayName ("Test Invalid - AddData _Word -Range _Zero")
	void testInvalid_AddDataWordRange_Zero(){
		String range;
		for (String val : validWords) {
			range = (val+":"+INT_ZERO);
			assertTrue(mb.addData(WORD, range, errors));
			
			// Adding a Range of "0" Elements, should return true, but not actually affect the Data.
			assertTrue(mb.retrieveData().isEmpty());
			assertFalse(errors.hasEntries()); // and ErrorsLog should be empty.
			//TODO - A Warning could be issued for this scenario
		}
	}
	
	@Test
	@DisplayName ("Test Invalid - AddData _Word -Range _Negative")
	void testInvalid_AddDataWordRange_Negative(){
		String range;
		for (String val : validWords) {
			for (String n : validNegativeWords) { // Negative values are not valid ranges
				
				range = (val+":"+n);
				assertFalse(mb.addData(WORD, range, errors));
				assertEquals("Errors:\n\t<Int_N>: ["+n+"], Must Be A Positive Integer!"
								+"\tFormat: \"<Int_Val> : <Int_N>\"\n",
						errors.toString());
				assertTrue(mb.retrieveData().isEmpty());
				errors.clear();
			}
		}
	}
	
	// Add Range   <int_val>:<int_n>
	@Test
	@DisplayName ("Test Invalid - AddData _Word -Range Over")
	void testInvalid_AddDataWordRange_Over(){
		// Test is invalid if Int.MAX becomes smaller than MAX_DATA
		//noinspection ConstantConditions
		assertTrue(Integer.MAX_VALUE>DATA_MAX);
		
		// after MAX_DATA Items, no more values should be stored, and should return False.
		String five = "5"; // set value to 5, so it is verifiable it has changed from default 0.
		String range = (five+":"+Integer.MAX_VALUE);
		
		assertFalse(mb.addData(WORD, range, errors));
		HashMap<Integer, Double> data = mb.retrieveData();
		
		assertEquals(DataMemory.MAX_DATA_ITEMS, data.size()); // expect stops  adding items at size limit
		// first 256 items should be set
		for (int i = 0; i<data.size(); i++) {
			assertEquals(5.0, data.get(i), DELTA);
		}
		
	}
	
	@Test
	@DisplayName ("Test InvalidFormat - AddData _Word -Range")
	void testInvalidFormat_AddDataWordRange(){
		String data = "-567 75"; // missing :
		assertFalse(mb.addData(WORD, data, errors));
		assertEquals("Errors:\n\tData: ["+data+"], Not Valid For DataType: \".word\"!\n",
				errors.toString());
	}
	
	// Add CSV Array
	@Test
	@DisplayName ("Test AddData _Word -CSVArray")
	void testAddDataWordCSVArray(){
		// build array of MAX_DATA items
		ArrayList<Double> copy = new ArrayList<>();
		int val = random.nextInt();
		StringBuilder arrCSV = new StringBuilder(""+val);
		copy.add((double) val);    // value 1
		for (int c = 1; c<DATA_MAX; c++) {    // N-1 commas = N values
			val = random.nextInt();
			arrCSV.append(" , ").append(val);
			copy.add((double) val);
		}
		
		assertTrue(mb.addData(WORD, arrCSV.toString(), errors));
		// Data in DataManager is stored as double, so Delta is necessary
		HashMap<Integer, Double> data = mb.retrieveData();
		
		for (int i = 0; i<data.size(); i++) {
			assertEquals(copy.get(i), data.get(i), DELTA);
		}
	}
	
	@Test
	@DisplayName ("Test AddData _Word -CSVArray Over")
	void testAddDataWordCSVArray_Over(){
		// Trying to use MAX_VALUE leads to Java running out of memory
		int TEST_LIMIT = (int) (DATA_MAX*3); // value of DATA_MAX(256) *4 causes StackOverflow
		// build array of MAX_DATA items
		ArrayList<Double> copy = new ArrayList<>();
		int val = random.nextInt();
		StringBuilder arrCSV = new StringBuilder(""+val);
		copy.add((double) val);    // value 1
		for (int c = 0; c<TEST_LIMIT; c++) {    // N-1 commas = N values
			val = random.nextInt();
			arrCSV.append(" , ").append(val);
			
			if (c<DATA_MAX) // only copy up to DATA_MAX
				copy.add((double) val);
		}
		
		assertFalse(mb.addData(WORD, arrCSV.toString(), errors));
		// Data in DataManager is stored as double, so Delta is necessary
		HashMap<Integer, Double> data = mb.retrieveData();
		
		assertEquals(DataMemory.MAX_DATA_ITEMS, data.size()); // expect stops  adding items at size limit
		
		for (int i = 0; i<data.size(); i++) {
			assertEquals(copy.get(i), data.get(i), DELTA);
		}
	}
	
	@Test
	@DisplayName ("Test InvalidFormat - AddData _Word -CSVArray")
	void testInvalidFormat_AddDataWordCSVArray(){
		
		String input = "-567, 800, 5, 75 89, 100, - 9"; // missing ,
		assertFalse(mb.addData(WORD, input, errors));
		assertEquals("Errors:\n"+"\tData Value: [75 89], Index: \"3\", Not Valid Signed Integer!\n"
						+"\tData Value: [- 9], Index: \"5\", Not Valid Signed Integer!\n",
				errors.toString());
		
		// reminder of values should have been placed
		HashMap<Integer, Double> data = mb.retrieveData();
		assertEquals(4, data.size());
		assertEquals(-567, data.get(0), DELTA);
		assertEquals(800, data.get(1), DELTA);
		assertEquals(5, data.get(2), DELTA);
		assertEquals(100, data.get(3), DELTA);
		
		
	}
	
	// Not Valid DataType
	@ParameterizedTest
	@ValueSource (strings = {DWORD, DOUBLE, ASCII, ASCIIZ})
	@DisplayName ("Test Add Data - Invalid DataType")
	void testAddData_InvalidDataType(String text){
		for (String w : validWords) {
			assertThrows(IllegalStateException.class, () -> mb.addData(text, w, errors));
			assertFalse(errors.hasEntries());
			assertTrue(mb.retrieveData().isEmpty());
		}
	}
	// Push Label
	
	//Attach Labels to Address -- After pushing Data or Instr, they should point to their addr
	
	// Add Instr
	
	// Assemble
}