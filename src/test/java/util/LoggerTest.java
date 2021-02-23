package util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.logs.ErrorLog;
import util.logs.ExecutionLog;
import util.logs.Logger;
import util.logs.WarningsLog;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest{
	Logger logger;
	ArrayList<String> logs;
	
	@BeforeEach
	void setUp(){
		logs = new ArrayList<>();
		logger = new Logger("Test", logs);
	}
	
	@Test
	@DisplayName ("Test Append to Log")
	void testAppendToLog(){
		logger.append("err1");
		logger.append("err2");
		//noinspection ResultOfMethodCallIgnored
		Assertions.assertAll(
				() -> assertTrue(logger.hasEntries()),
				() -> assertEquals("Test:\n\terr1\n\terr2\n", logger.toString()),
				() -> assertEquals("err1", logs.get(0)),
				() -> assertThrows(IndexOutOfBoundsException.class, () -> logs.get(2)),
				() -> assertFalse(logs.isEmpty())
		);
	}
	
	@Test
	@DisplayName ("Test ignore Append Blank")
	void testIgnoreAppendBlank(){
		logger.append(null);// Null
		assertTrue(logs.isEmpty());
		logger.append("something");
		assertEquals(1, logs.size());
		logger.append("");// Empty
		assertEquals(1, logs.size());
		logger.append("     ");// Whitespace
		assertEquals(1, logs.size());
		logger.append("something else    with spaces");
		assertEquals(2, logs.size());
		Assertions.assertAll(
				() -> assertTrue(logger.hasEntries()),
				() -> assertEquals("Test:\n\tsomething\n\tsomething else    with spaces\n", logger.toString()),
				() -> assertEquals("something", logs.get(0)),
				() -> assertFalse(logs.isEmpty())
		);
	}
	
	@Test
	@DisplayName ("Test Clear Log")
	void testClearLog(){
		testAppendToLog();
		logger.clear();
		Assertions.assertAll(
				() -> assertFalse(logger.hasEntries()),
				() -> assertEquals("", logger.toString()),
				() -> assertTrue(logs.isEmpty())
		);
	}
	
	@Test
	@DisplayName ("Test Sub Logs")
	void testSubLogs(){
		testAppendToLog(); //
		logger = new ErrorLog(logs);
		//logs already contains {"err1", "err2"}
		Assertions.assertAll(
				() -> assertTrue(logger.hasEntries()),
				() -> assertEquals("Errors:\n\terr1\n\terr2\n", logger.toString())
		);
		
		logger = new WarningsLog(logs);
		//logs already contains {"err1", "err2"}
		logger.append("warn1");
		Assertions.assertAll(
				() -> assertTrue(logger.hasEntries()),
				() -> assertEquals("Warnings:\n\terr1\n\terr2\n\twarn1\n", logger.toString())
		);
		
		logger = new ExecutionLog(logs);
		//logs already contains {"err1", "err2"}
		logs.clear();
		logger.append("instr1");
		Assertions.assertAll(
				() -> assertTrue(logger.hasEntries()),
				() -> assertEquals("Execution:\n\tinstr1\n", logger.toString())
		);
	}
	
	//Color output will not be tested. it may be removed entirely in future release.
}