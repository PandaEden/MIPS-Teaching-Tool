package Util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConvertTest{
	
	@ParameterizedTest
	@ValueSource (ints = {0, 200, (2^20), Integer.MAX_VALUE, Integer.MIN_VALUE})
	@DisplayName ("Test Integer 2 0xHexadecimal")
	void testInteger20XHexadecimal(int address){
		String hex = Convert.int2hex(address);
		//0-1 should be the "0x" sign, 2-9 the value
		assert hex!=null;
		Assertions.assertAll(
				() -> assertEquals(hex.length(),10),//length = 10,
				() -> assertEquals(hex.substring(0,2),"0x"), //First 2 bits, sign
				// Remainder should be the value, which can be parsed back to the int
				() -> assertEquals(Integer.parseInt(hex.substring(2), 16),address)
		);
	}
	
	@ParameterizedTest
	@ValueSource (strings = {"0x04","0x00400000","0x00500000","0x10000000",
			"0x10010000","0x10010800","0x10040000"})
	@DisplayName ("Test Hexadecimal 2 Integer")
	void testHexadecimal2Integer(String hex){
		int integer = Convert.hex2int(hex);
		assertEquals(Convert.hex2int(hex), integer);
	}
	
	@SuppressWarnings ("ResultOfMethodCallIgnored")
	@ParameterizedTest
	@ValueSource (strings = {"not a hex", "0x 00001010", "0x"})
	@DisplayName ("Test Invalid Hexadecimal 2 Integer")
	void testInvalidHexadecimal2Integer(String hex){
		Assertions.assertThrows(IllegalArgumentException.class, () -> Convert.hex2int(hex));
	}
	
	//	address2index:
	//Instr index
	@Test
	@DisplayName ("Test Address 2 Index, Instruction")
	void testAddress2IndexInstruction(){
		int address = 0x00400000 + 250*4; //Instruction 251
		assertEquals(Convert.address2index(address), 251);
	}
	//Data index
	@Test
	@DisplayName ("Test Address 2 Index, Data")
	void testAddress2IndexData(){
		int address = 0x10010000 + 99*8; //Index 100
		assertEquals(Convert.address2index(address), 100);
	}
	//invalid
	@ParameterizedTest
	@ValueSource (ints = {0x10000000, 0x10040000, 0, -50, Integer.MIN_VALUE, Integer.MAX_VALUE})
	@DisplayName ("Test Invalid Address 2 Index")
	void testInvalidAddress2Index(int address){
		assertEquals(Convert.address2index(address), -1);
	}
	
	@ParameterizedTest
	@ValueSource (ints = {0, 20, 50, 100, 255,0x5FFF, 0x3FFFF})
	@DisplayName ("Test Integer 2 Address")
	void testInteger2Address(int integer){
		int base_instr=0x00400000;
		int base_data=0x10010000;
		int data_max=0x00005FFF;
		
		int addr = Convert.int2address(integer);
		Assertions.assertAll(
				// address = integer*4
				() -> assertEquals(addr,integer*4),
				// integer = add2index(address + base)
					// +base_instr
				() -> assertEquals(Convert.address2index(addr+base_instr),integer),
					// +base_data  int>0x5FFF  -1 if larger than base max
				() -> assertEquals(Convert.address2index(addr+base_data),(integer>data_max)?-1:integer)
		);
	}
	
	@Test
	@DisplayName("Test Split CSV")
	void testSplitCSV () {
		String s = "    Lots Of    Empty,    Spaces    , csv should     trim";
		String[] result = Convert.splitCSV(s);
		Assertions.assertAll(
				() -> assertEquals(result.length, 3),
				() -> assertEquals(result[0],"Lots Of Empty"),
				() -> assertEquals(result[0],"Spaces"),
				() -> assertEquals(result[0],"csv should trim")
		);
	}
	
	@Nested
	@DisplayName("Register Conversion")
	class RegisterConversionTests{
		
		//Convert expects all input to be lowerCase - this should be done by the Parser!
		String[] split(String csv){
			return Convert.splitCSV(csv.toLowerCase());
		}
		
		// There is probably a much better way. but using a loop to create them would also obscure it
		String[] temporaryNamed = split("T0,T1,T2,T3,T4,T5,T6,T7, T8,T9");
		String[] temporaryR = split("R8,R9,R10,R11,R12,R13,R14,R15, R24,R25");
		int[] temporary_index = {8,9,10,11,12,13,14,15, 24,25};
		
		String[] savedNamed = split("S0,S1,S2,S3,S4,S5,S6,S7");
		String[] savedR= split("R16,R17,R18,R19,R20,R21,R22,R23");
		int[] saved_index= {16,17,18,19,20,21,22,23};
		
		// Implemented should include Temporary & Saved Registers
		String[] otherNamed = (split("ZERO, AT, V0,V1, A0,A1,A2,A3, K1,K2, RA, GP,SP,S8"));
		String[] otherR = split("R0, R1, R2,R3, R4,R5,R6,R7, R31");
		int[] other_index = {0, 2,3, 4,5,6,7, 31};
		
		@Test
		@DisplayName ("Test R Reference 2 Named")
		void testRReference2Named(){
			for (int i = 0; i<temporaryR.length; i++) {
				assertEquals(Convert.r2named(temporaryR[i]),temporaryNamed[i]);
			}
			for (int i = 0; i<savedR.length; i++) {
				assertEquals(Convert.r2named(savedR[i]),savedNamed[i]);
			}
			for (int i = 0; i<otherR.length; i++) {
				assertEquals(Convert.r2named(otherR[i]),otherNamed[i]);
			}
		}
		
		@Test
		@DisplayName ("Test Named 2 R Reference")
		void testNamed2RReference(){
			for (int i = 0; i<temporaryNamed.length; i++) {
				assertEquals(Convert.named2r(temporaryNamed[i]),temporaryR[i]);
			}
			for (int i = 0; i<savedNamed.length; i++) {
				assertEquals(Convert.named2r(savedNamed[i]),savedR[i]);
			}
			for (int i = 0; i<otherNamed.length; i++) {
				assertEquals(Convert.named2r(otherNamed[i]),otherR[i]);
			}
		}
		
		@Test
		@DisplayName ("Test Named 2 Index")
		void testNamed2Index(){
			for (int i = 0; i<temporaryNamed.length; i++) {
				assertEquals(Convert.r2index(Convert.named2r(temporaryNamed[i])),temporary_index[i]);
			}
			for (int i = 0; i<savedNamed.length; i++) {
				assertEquals(Convert.r2index(Convert.named2r(savedNamed[i])),saved_index[i]);
			}
			for (int i = 0; i<otherNamed.length; i++) {
				assertEquals(Convert.r2index(Convert.named2r(otherNamed[i])),other_index[i]);
			}
		}
		
		@Test
		@DisplayName ("Test R Reference 2 Index")
		void testRReference2Index(){
			for (int i = 0; i<temporaryR.length; i++) {
				assertEquals(Convert.r2index(temporaryR[i]),temporary_index[i]);
			}
			for (int i = 0; i<savedR.length; i++) {
				assertEquals(Convert.r2index(savedR[i]),saved_index[i]);
			}
			for (int i = 0; i<otherR.length; i++) {
				assertEquals(Convert.r2index(otherR[i]),other_index[i]);
			}
		}
		
		@Test
		@DisplayName ("Test Index 2 R Reference")
		void testIndex2RReference(){
			for (int i = 0; i<temporary_index.length; i++) {
				assertEquals(Convert.index2r(temporary_index[i]),temporaryR[i]);
			}
			for (int i = 0; i<saved_index.length; i++) {
				assertEquals(Convert.index2r(saved_index[i]),savedR[i]);
			}
			for (int i = 0; i<other_index.length; i++) {
				assertEquals(Convert.index2r(other_index[i]),otherR[i]);
			}
		}
		
		@Test
		@DisplayName ("Test Index 2 Named")
		void testIndex2Named(){
			for (int i = 0; i<temporary_index.length; i++) {
				assertEquals(Convert.r2named(Convert.index2r(temporary_index[i])),temporaryNamed[i]);
			}
			for (int i = 0; i<saved_index.length; i++) {
				assertEquals(Convert.r2named(Convert.index2r(saved_index[i])),savedNamed[i]);
			}
			for (int i = 0; i<other_index.length; i++) {
				assertEquals(Convert.r2named(Convert.index2r(other_index[i])),otherNamed[i]);
			}
		}
	}
}