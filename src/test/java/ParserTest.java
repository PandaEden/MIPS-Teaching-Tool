import static org.junit.jupiter.api.Assertions.*;

import model.MemoryBuilder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperties;
import org.junit.jupiter.api.condition.DisabledOnOs;
import util.Convert;
import util.logs.ErrorLog;
import util.logs.WarningsLog;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ParserTest {
	private static final String TEST_RESOURCES_DIR = "src"+File.separator+"test"+File.separator+"resources"+File.separator;
	private static ErrorLog errLog = new ErrorLog( new ArrayList<>());;
	private static WarningsLog warnLog = new WarningsLog(new ArrayList<>());
	
	private static Parser parser;
	private static MemoryBuilder mb;
	private String errorFormat;
	private String warningsFormat;
	
	@BeforeAll
	static void beforeAll(){
		mb = new MemoryBuilder( );
		parser = new Parser(mb, errLog,warnLog);
	}
	
	@BeforeEach
	void setUp(){	// Build/ reset models
		errLog.clear();
		warnLog.clear();
		
		errorFormat = "Errors:\n";
		warningsFormat = "Warnings:\n";
	}
	
	private void noErrorOrWarnings( ){
		Assertions.assertAll(
				() -> assertFalse(errLog.hasEntries()),
				() -> assertFalse(warnLog.hasEntries()),
				() -> assertEquals("",errLog.toString()+warnLog.toString())
		);
	}
	
	private void matchErrorsAndWarnings( ){
		Assertions.assertAll(
				() -> assertTrue(errLog.hasEntries()),
				() -> assertTrue(warnLog.hasEntries()),
				() -> assertEquals(errorFormat+warningsFormat, errLog.toString()+warnLog.toString())
		);
	}
	
	private void matchErrorsOnly( ){
		Assertions.assertAll(
				() -> assertTrue(errLog.hasEntries()),
				() -> assertFalse(warnLog.hasEntries()),
				() -> assertEquals(errorFormat, errLog.toString())
		);
	}
	
	private void matchWarningsOnly( ){
		Assertions.assertAll(
				() -> assertFalse(errLog.hasEntries()),
				() -> assertTrue(warnLog.hasEntries()),
				() -> assertEquals(warningsFormat, warnLog.toString())
		);
	}
	
	private void nullObjectErrors(Object object, String errorFormat){
		appendError(errorFormat);
		assertNull(object);
		matchErrorsOnly();
	}
	
	// Appends see documentation text, to the end of ErrorsLog expected format.
	private void appendSeeDocs(){ errorFormat+="#See documentation (README.md)!\n"; }
	// Appends line to end of ErrorsLog expected format, with newline & tab at the start.
	private void appendError(String text){ errorFormat+="\t"+text+"\n"; }
	// Appends line to end of WarningsLog expected format, with newline & tab at the start.
	private void appendWarning(String text){ warningsFormat+="\t"+text+"\n"; }
	
	@Nested
	@DisplayName ("File Tests")
	class FileTests {
		
		@Test
		@DisplayName("File Does Not Exist!")
		void fileDoesNotExist(){
			File temp = new File(TEST_RESOURCES_DIR+"Not_Actual_File");
			assertFalse(temp.exists()); // If the File exists, test is invalid
			
			nullObjectErrors(parser.loadFile(TEST_RESOURCES_DIR+"Not_Actual_File"),
					"File: \"Not_Actual_File\", Does Not Exist!");
		}
		
		@Test
		@DisplayName ("Default Filename For Blank FileName")
		void defaultFilenameForBlankFileName(){
			assertNull(parser.loadFile(" "));
			
			appendWarning("Filename Not Provided, Using Default File: \"FileInput.s\"");
			matchWarningsOnly();
		}
		
		@SuppressWarnings ("SpellCheckingInspection")
		@Test
		@DisplayName("File Over MAX Lines")
		void fileOverMaxLines() {
			nullObjectErrors(parser.loadFile(TEST_RESOURCES_DIR+"FileOver30Klines.s"),
					"File: \"FileOver30Klines.s\", Has Too Many Lines!, Max Lines = [512]!");
		}
		
		@ParameterizedTest
		@ValueSource (strings = {"file|.s", "fi?le.s", "file.a*m"})
		@DisplayName ("Test Invalid FileName")
		void testInvalid_FileName(String text){
			nullObjectErrors(parser.loadFile(text),
					"File: \""+text+"\", Not Valid FileName!");
		}
		
		@Test	// May Need to Manually Test, by changing file permissions on system
		@DisabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
		@DisplayName ("Test File Not Accessible /NotReadable")
		void testFileNotAccessible(){
			// Tried creating a file on the system without Read Permission, This made it think it was a non-valid file?
			// Run test if platform is not Windows
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				
				File tempFile = null;
				try {
					tempFile = File.createTempFile("Busy-File", ".s");
					String filename = tempFile.getPath();
					
					if (tempFile.setReadable(false))	// if successfully made file unReadable
						nullObjectErrors(parser.loadFile(filename), "File: \""+filename+"\", Not Readable!");
					else
						fail("Cannot Test Unreadable File");
					
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					assert tempFile!=null;
					tempFile.deleteOnExit();
				}
			}
		}
	}
	
	@Nested
	@DisplayName ("Split Line Test")
	class SplitLineTest{
		List<String> comments = Arrays.asList(
				"   #  HashOnly_  Text    1233 addi $r0   ",
				"   ;  SemiOnly_ Random Text 2",
				"#   HashFirst_ Random ;Text  ",
				"   ;  SemiFirst_ Random #Text  "
		);
		List<String> labels = Arrays.asList("panda :   ", "_notAPanda:", "  :", " not a valid label :");
		
		// Comments only
		@Test
		@DisplayName ("Test Split Comments Only")
		void testSplit_CommentOnly(){
			for (String comment: comments) {
				String[] split = parser.splitLine(comment);
				assertNull(split[0]);
				assertNull(split[1]);
				assertNull(split[2]);
				assertEquals(Convert.removeExtraWhitespace(comment), split[3]); // extra whitespace is trimmed
			}
		}
		
		//Labels Only
		@Test
		@DisplayName ("Test Split Labels Only")
		void testSplit_LabelsOnly(){
			for (String label: labels) {
				
				String[] split = parser.splitLine(label);
				assertNull(split[1]);
				assertNull(split[2]);
				assertNull(split[3]);
				assertEquals(Convert.removeExtraWhitespace(label).split(":",2)[0].toLowerCase(), split[0]);
			}
		}
		
		//Labels and Comments
		@Test
		@DisplayName ("Test Split Labels And Comments")
		void testSplit_LabelsAndComments(){
			for (String comment: comments) {
				for (String label: labels) {
					String[] split = parser.splitLine(label+comment);
					assertNull(split[1]);
					assertNull(split[2]);
					assertEquals(Convert.removeExtraWhitespace(comment), split[3]); // extra whitespace is trimmed
					assertEquals(Convert.removeExtraWhitespace(label).split(":",2)[0].toLowerCase(), split[0]);
				}
			}
		}
		
		@ParameterizedTest
		@ValueSource (strings = { "  add  ", ".data  ", ".word", "l.d", "  ct.l.d", "PANDA"})
		@DisplayName ("Test Split Arg1 Only")
		void testSplitArg1Only(String arg){
			String[] split = parser.splitLine(arg);
			assertNull(split[0]);
			assertNull(split[2]);
			assertNull(split[3]);
			assertEquals(arg.strip().toLowerCase(), split[1]); // extra whitespace is trimmed
		}
		
		@Test
		@DisplayName ("Test Split Arg 1 And 2 Only")
		void testSplitArg_1And2_Only(){
			String[] split = parser.splitLine(" ADDI    $r0,  .  87  NOT VA:LID   ");
			assertNull(split[0]);
			assertNull(split[3]);
			assertEquals("addi", split[1]);
			assertEquals("$r0, . 87 not va:lid", split[2]);
		}
		
		@Test
		@DisplayName ("Test Split _Full")
		void testSplit_Full(){
			String line = "	_LAB.3L:\tADD.d \tR0 5:6,	0x59 ; s:e  m.:i  # Comments    Section!  ";
			String[] split = parser.splitLine(line);
			assertEquals("_lab.3l", split[0]);
			assertEquals("add.d", split[1]);
			assertEquals("r0 5:6, 0x59", split[2]);
			assertEquals("; s:e m.:i # Comments Section!", split[3]);
		}
		
		@Test
		@DisplayName ("Test Split _Blank")
		void testSplit_Blank(){
			String[] split = parser.splitLine("   ");
			assertNull(split[0]);
			assertNull(split[1]);
			assertNull(split[2]);
			assertNull(split[3]);
		}
		
	}
	
	@Nested
	@DisplayName ("Parse Invalid Lines")
	class ParseInvalidLinesTest {
		
		@Test
		@Disabled //TEST - implement Parse Lines
		@DisplayName ("Over MAX Instructions")
		void overMax4Instructions(){
			String FilePath = "Parse_FileOver16KInstructions.s";
			
			assertNotNull(parser.loadFile(FilePath));	// The File properties are valid
			assertFalse(parser.parseLoadedFile());		// The File Contents . Are Not!
			
			appendError("- File contains too many instructions (16384 limit)!");
			matchErrorsOnly();
		}
		
		@Test
		@Disabled //TEST - implement Parse Lines
		@DisplayName ("Over MAX Data")
		void overMaxData(){
			String FilePath = "FileOver16KInstructions.s";
			
			assertNotNull(parser.loadFile(FilePath));	// The File properties are valid
			assertFalse(parser.parseLoadedFile());		// The File Contents . Are Not!
			
			appendError("- File contains too many instructions (16384 limit)!");
			matchErrorsOnly();
			fail("Not implemented");
		}
		
		@Test
		@DisplayName ("Test Parse InvalidLine _Label_Operands")
		void testParseInvalidLines_Label_Opcode(){
			String line = "  p a n   d a : addi r-8, R70, 32.76 ; # comment";
			
			assertFalse(parser.parseLine(line, -80));
			
			appendError("LineNo: -80\tLabel: \"p a n d a \" Not Supported!\n\t_\n"+
					"\tLineNo: -80\t\tRegister: \"r-8\" Not Valid!\n"+
					"\tLineNo: -80\t\tRegister: \"r70\" Not Valid!\n"+
					"\tLineNo: -80\t\tImmediate Value: \"32.76\" Not Valid Integer!\n"+
					"\tLineNo: -80\tOperands: [r-8, r70, 32.76] for Opcode: \"addi\" Not Valid !");
			matchErrorsOnly();
		}
		
		@ParameterizedTest
		@ValueSource (strings = {"j ", ".word -5:-5", ".word 8589934592", "add R0, R0", "SUB R0 R0 R0",
		"j 0", "j 0x 0040000", "JAL 0x140001", "lw 0x00400000", "lw 20 (R0)", "sw 50 ( 76)", ".word "})
		@DisplayName ("Test Parse InvalidLines")
		void testParseInvalidLines(String line){
			assertFalse(parser.parseLine(line, 5));
			assertTrue(errLog.hasEntries());
		}
	}
	
	
	
	// Parsing
	@Test
	@DisplayName ("- Invalid Directive")
	void invalidDirective(){
		appendError("LineNo: -5\tDirective: \".nonmipsdirective\" Not Supported!");
		//appendSeeDocs();
		
		assertFalse(parser.parseLine("  .nonMipsDirective ", -5));
		matchErrorsOnly();
		assertNull(parser.assemble());
		appendError("No Instructions Found!");
		matchErrorsOnly();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {".data",".text",".code"})
	@DisplayName ("Valid Directive")
	void validDirective(String directive){
		boolean s = parser.parseLine(directive, -5);
		if (!s){
			System.out.println(errLog);
		}
		
		assertTrue(parser.parseLine(directive, -5));
		noErrorOrWarnings();
		assertNull(parser.assemble());
		appendError("No Instructions Found!");
		matchErrorsOnly();
	}
}