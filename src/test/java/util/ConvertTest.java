package util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConvertTest{
	final int NumR = 50; // Number of random numbers to test
	Random random = new Random();
	
	//	uInt2Hex
	@ParameterizedTest
	@ValueSource (ints = {0, 200, 1048576, Integer.MAX_VALUE, Integer.MIN_VALUE, -50})
	@DisplayName ("Test Integer 2 0xHexadecimal")
	void testInteger20XHexadecimal(Integer address){
		String hex = Convert.uInt2Hex(address);
		//0-1 should be the "0x" sign, 2-9 the value
		
		long abs = Integer.toUnsignedLong(address);
		
		Assertions.assertAll(
				() -> assertEquals("0x", hex.substring(0, 2)), //First 2 bits, sign
				// Remainder should be the value, which can be parsed back to the int
				() -> assertEquals(abs, Long.parseLong(hex.substring(2), 16))
		);
	}
	
	//	hex2uInt:
	@ParameterizedTest
	@ValueSource (strings = {"04", "00400000", "00500000", "10000000",
			"10010000", "10010800", "10040000", "ffff"})
	@DisplayName ("Test Hexadecimal 2 Integer")
	void testHexadecimal2Integer(String hex){
		int integer = Integer.parseInt(hex, 16);
		assertEquals(integer, Convert.hex2uInt("0x"+hex));
	}
	
	@ParameterizedTest
	@ValueSource (strings = {"not a hex", "0x 00001010", "0x", "0x123456789", "0xFFFF", "0xpanda", "07af"})
	@DisplayName ("Test Invalid Hexadecimal 2 Integer")
	void testInvalid_Hexadecimal2Integer(String hex){
		assertThrows(IllegalArgumentException.class, () -> Convert.hex2uInt(hex));
	}
	
	//	address2Imm:
	@Test
	@DisplayName ("Test Address 2 Immediate")
	void testAddress2Immediate(){
		//Test 0
		assertEquals(0, Convert.address2Imm(0));
		
		//Test Integer.MAX
		assertEquals((Integer.MAX_VALUE/4), Convert.address2Imm(Integer.MAX_VALUE));
		
		int address = 0;
		//Test random 200 between
		for (int i = 0; i<NumR; i++) {
			address = random.nextInt(Integer.MAX_VALUE);
			assertEquals(address/4, Convert.address2Imm(address));
		}
	}
	
	@SuppressWarnings ("ResultOfMethodCallIgnored")
	@Test
	@DisplayName ("Test Invalid (Negative) Address 2 Immediate")
	void testInvalid_Address2Immediate(){
		//Testing negative values
		//Test -1
		assertThrows(IllegalArgumentException.class, () -> Convert.address2Imm(-1));
		
		//Test -Integer.MIN_Value
		assertThrows(IllegalArgumentException.class, () -> Convert.address2Imm(Integer.MIN_VALUE));
		
		//Test random 200 between
		for (int i = 0; i<NumR; i++) {
			assertThrows(IllegalArgumentException.class, () ->
					Convert.address2Imm(-1-random.nextInt(Integer.MAX_VALUE))); // -1 to Int.MIN_VALUE
		}
	}
	
	//	imm2Address:
	@Test
	@DisplayName ("Test Immediate 2 Address")
	void testImmediate2Address(){
		final int IMM_MAX = (Integer.MAX_VALUE/4)-1;
		final int IMM_MIN = (-32768); // (-2^15)
		
		//Test 0
		assertEquals(0, Convert.imm2Address(0));
		
		//Test Max
		assertEquals(IMM_MAX*4, Convert.imm2Address(IMM_MAX));
		
		//Test Min
		assertEquals(IMM_MIN*4, Convert.imm2Address(IMM_MIN));
		
		int imm = 0; // -2^15 - to (2^29 -1)
		for (int i = 0; i<NumR; i++) {
			imm = (random.nextInt(-IMM_MIN+IMM_MAX)-IMM_MIN); // Random in range IMM_MIN to IMM_MAX
			assertEquals(imm*4, Convert.imm2Address(imm));
		}
	}
	
	@SuppressWarnings ("ResultOfMethodCallIgnored")
	@Test
	@DisplayName ("Test Invalid Immediate 2 Address")
	void testInvalid_Immediate2Address(){
		final int IMM_UNDER_MIN = (-32768-1); // (-2^15)-1
		// negative < -2^15
		//Test  (-2^15)-1
		assertThrows(IllegalArgumentException.class, () -> Convert.imm2Address(IMM_UNDER_MIN));
		
		//Test  Integer.Min
		assertThrows(IllegalArgumentException.class, () -> Convert.imm2Address(Integer.MIN_VALUE));
		
		for (int i = 0; i<NumR; i++) {
			assertThrows(IllegalArgumentException.class, () -> // Random
					Convert.imm2Address(-random.nextInt(IMM_UNDER_MIN-Integer.MIN_VALUE)-IMM_UNDER_MIN));
		}
		
		final int IMM_MAX = (Integer.MAX_VALUE/4); //2^29
		
		//Test  2^29+1
		assertThrows(IllegalArgumentException.class, () -> Convert.imm2Address(IMM_MAX+1));
		
		//Test  Integer.Max
		assertThrows(IllegalArgumentException.class, () -> Convert.imm2Address(Integer.MAX_VALUE));
		
		for (int i = 0; i<NumR; i++) {
			assertThrows(IllegalArgumentException.class, () -> // Random
					Convert.imm2Address(random.nextInt(Integer.MAX_VALUE-IMM_MAX)+IMM_MAX));
		}
	}
	
	//	address2index:
	//Instr index
	@Test
	@DisplayName ("Test Address 2 Index, Instruction")
	void testAddress2Index_Instruction(){
		//index for address 0x00500000 is 262144, making the last supported index 0x3FFFF (262143)
		for (int instrIdx = 0x3FFFF; instrIdx>=0; instrIdx--) {
			int address = 0x00400000+(instrIdx)*4; // index 0 -> Instruction 1
			assertEquals(Convert.address2Index(address), instrIdx);
		}
	}
	
	//Data index
	@Test
	@DisplayName ("Test Address 2 Index, Data")
	void testAddress2Index_Data(){
		// Data is DoubleWord aligned, index must be a multiple of 2.
		//index for address 0x10010800 is 512, making the last supported index 0x1FE (510)
		
		for (int dataIdx = 0x1FE; dataIdx>=0; dataIdx -= 2) { // Jumps of 2 - data aligned
			int address = 0x10010000+(dataIdx)*4; // index 0 -> data_val 1
			assertEquals(Convert.address2Index(address), dataIdx);
		}
	}
	
	//invalid
	@ParameterizedTest
	@ValueSource (ints = {0x10000000, 0x10040000, 0, -50, Integer.MIN_VALUE, Integer.MAX_VALUE})
	@DisplayName ("Test Invalid Address 2 Index")
	void testInvalid_Address2Index(int address){
		assertThrows(IllegalArgumentException.class, () -> Convert.address2Index(address));
	}
	
	//imm2Address added back to base
	@ParameterizedTest
	@ValueSource (ints = {0, 20, 50, 100, 255, 510, 0x5FFF, 0x3FFFF})
	@DisplayName ("Test Index 2 Address Converted Back")
	void testIndex2Address_ConvertedBack(int immediate){
		final int base_instr = 0x00400000;
		final int base_data = 0x10010000;
		final int data_max_offset = 0x800-8; //255*8  or (256*8)-8
		
		int addr = Convert.imm2Address(immediate);
		Assertions.assertAll(
				// address = integer*4
				() -> assertEquals(addr, immediate*4),
				// integer = add2index(address + base)
				// +base_instr
				() -> assertEquals(immediate, Convert.address2Index(addr+base_instr))
		);
		
		// +base_data  int>0x5FFF  -1 if larger than base max
		if (immediate%2==0 && addr<=base_data+data_max_offset)
			Assertions.assertAll(
					() -> assertEquals(addr, immediate*4),
					() -> assertEquals(immediate, Convert.address2Index(addr+base_data))
			);
	}
	
	@Test
	@DisplayName ("Test Split CSV")
	void testSplitCSV(){
		String s = "    Lots Of    Empty,    Spaces    , csv should     trim  ";
		String[] result = Convert.splitCSV(s);
		Assertions.assertAll(
				() -> assertEquals(3, result.length),
				() -> assertEquals("Lots Of Empty", result[0]),
				() -> assertEquals("Spaces", result[1]),
				() -> assertEquals("csv should trim", result[2])
		);
	}
	
	@Nested
	@DisplayName ("Register Conversion")
	class RegisterConversionTests{
		
		// There is probably a much better way. but using a loop to create them would also obscure it
		final String[] temporaryNamed = split("T0,T1,T2,T3,T4,T5,T6,T7,"+"T8,T9");
		final String[] temporaryR = split("R8,R9,R10,R11,R12,R13,R14,R15,"+"R24,R25");
		final int[] temporary_index = {8, 9, 10, 11, 12, 13, 14, 15, 24, 25};
		final String[] savedNamed = split("S0,S1,S2,S3,S4,S5,S6,S7,"+"S8");
		final String[] savedR = split("R16,R17,R18,R19,R20,R21,R22,R23,"+"R30");
		final int[] saved_index = {16, 17, 18, 19, 20, 21, 22, 23, 30};
		// Implemented should include Temporary & Saved Registers
		final String[] otherNamed = (split("ZERO,AT,V0,V1,A0,A1,A2,A3,"+"K1,K2,GP,SP,"+"RA"));
		final String[] otherR = split("R0,R1,R2,R3,R4,R5,R6,R7,"+"R26,R27,R28,R29,"+"R31");
		final int[] other_index = {0, 1, 2, 3, 4, 5, 6, 7, 26, 27, 28, 29, 31};

		//Convert expects all input to be lowerCase - this should be done by the Parser!
		final String[] split(String csv){
			return Convert.splitCSV(csv.toLowerCase());
		}
		
		@Test
		@DisplayName ("Test R Reference 2 Named")
		void testRReference2Named(){
			for (int i = 0; i<temporaryR.length; i++) {
				assertEquals(temporaryNamed[i], Convert.r2Named(temporaryR[i]));
			}
			for (int i = 0; i<savedR.length; i++) {
				assertEquals(savedNamed[i], Convert.r2Named(savedR[i]));
			}
			for (int i = 0; i<otherR.length; i++) {
				assertEquals(otherNamed[i], Convert.r2Named(otherR[i]));
			}
		}
		
		@Test
		@DisplayName ("Test Named 2 R Reference")
		void testNamed2RReference(){
			for (int i = 0; i<temporaryNamed.length; i++) {
				assertEquals(temporaryR[i], Convert.named2R(temporaryNamed[i]));
			}
			for (int i = 0; i<savedNamed.length; i++) {
				assertEquals(savedR[i], Convert.named2R(savedNamed[i]));
			}
			for (int i = 0; i<otherNamed.length; i++) {
				assertEquals(otherR[i], Convert.named2R(otherNamed[i]));
			}
		}
		
		@Test
		@DisplayName ("Test Named 2 Index")
		void testNamed2Index(){
			for (int i = 0; i<temporaryNamed.length; i++) {
				assertEquals(temporary_index[i], Convert.r2Index(Convert.named2R(temporaryNamed[i])));
			}
			for (int i = 0; i<savedNamed.length; i++) {
				assertEquals(saved_index[i], Convert.r2Index(Convert.named2R(savedNamed[i])));
			}
			for (int i = 0; i<otherNamed.length; i++) {
				assertEquals(other_index[i], Convert.r2Index(Convert.named2R(otherNamed[i])));
			}
		}
		
		@Test
		@DisplayName ("Test R Reference 2 Index")
		void testRReference2Index(){
			for (int i = 0; i<temporaryR.length; i++) {
				assertEquals(temporary_index[i], Convert.r2Index(temporaryR[i]));
			}
			for (int i = 0; i<savedR.length; i++) {
				assertEquals(saved_index[i], Convert.r2Index(savedR[i]));
			}
			for (int i = 0; i<otherR.length; i++) {
				assertEquals(other_index[i], Convert.r2Index(otherR[i]));
			}
		}
		
		@Test
		@DisplayName ("Test Index 2 R Reference")
		void testIndex2RReference(){
			for (int i = 0; i<temporary_index.length; i++) {
				assertEquals(temporaryR[i], Convert.index2R(temporary_index[i]));
			}
			for (int i = 0; i<saved_index.length; i++) {
				assertEquals(savedR[i], Convert.index2R(saved_index[i]));
			}
			for (int i = 0; i<other_index.length; i++) {
				assertEquals(otherR[i], Convert.index2R(other_index[i]));
			}
		}
		
		@Test
		@DisplayName ("Test Index 2 Named")
		void testIndex2Named(){
			for (int i = 0; i<temporary_index.length; i++) {
				assertEquals(temporaryNamed[i], Convert.r2Named(Convert.index2R(temporary_index[i])));
			}
			for (int i = 0; i<saved_index.length; i++) {
				assertEquals(savedNamed[i], Convert.r2Named(Convert.index2R(saved_index[i])));
			}
			for (int i = 0; i<other_index.length; i++) {
				assertEquals(otherNamed[i], Convert.r2Named(Convert.index2R(other_index[i])));
			}
		}
	}
}