package Util;

import Util.Logs.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggerTest{
	Logger logger;
	ArrayList<String> logs;
	
	@BeforeEach
	void setUp(){
		logs = new ArrayList<>();
		logger = new Logger("Test",logs);
	}
	
	@Test
	@DisplayName ("Append to Log Test")
	void append(){
		logger.append("err1");
		logger.append("err2");
		Assertions.assertAll(
				() -> assertTrue(logger.hasEntries()),
				() -> assertEquals(logger.toString(),"Test:\n\terr1\n\terr2\n"),
				() -> assertEquals(logs.get(0),"err1"),
				() -> assertFalse(logs.isEmpty())
		);
	}
	
	@Test
	@DisplayName ("Clear Log")
	void clearLog(){
		append();
		logger.clear();
		Assertions.assertAll(
				() -> assertFalse(logger.hasEntries()),
				() -> assertEquals(logger.toString(), ""),
				() -> assertTrue(logs.isEmpty())
		);
	}
	
	//Color output will not be tested. it may be removed entirely in future release.
}