package model.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import util.logs.ExecutionLog;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ComponentTest {
	
	@Nested
	class Mutex {
		private final Integer I0=-900;
		private final Integer I1=50;
		
		private Integer mutex (boolean signal) {
			return Component.Mutex( I0, I1, signal );
		}
		
		@Test
		void Input0 ( ) {
			assertEquals( I0, mutex( true ) );
		}
		
		@Test
		void Input1 ( ) {
			assertEquals( I1, mutex( false ) );
		}
		
		@Test
		void Input1_Null_PassThrough ( ) {
			assertNull( Component.Mutex( I0, null, false ) );
		}
	}
	
	@Nested
	class Adder {
		
		@Test
		void Increment_PC ( ) {
			ExecutionLog log = new ExecutionLog( new ArrayList<>() );
			assertEquals( 121, Component.Adder(50, 71,"Incrementing Program Counter", log) );
			assertEquals( "Execution:\n\tIncrementing Program Counter!\n", log.toString() );
		}
		
		@Test
		void Integer_Overflow ( ) {
			ExecutionLog log = new ExecutionLog( new ArrayList<>() );
			assertEquals( Integer.MIN_VALUE+19 ,Component.Adder( Integer.MAX_VALUE, 20,"Numeric Overflow", log) );
			assertEquals( "Execution:\n\tNumeric Overflow!\n", log.toString() );
		}
		
		@Test
		void Null_Input ( ) {
			ExecutionLog log = new ExecutionLog( new ArrayList<>() );
			assertNull( Component.Adder( Integer.MAX_VALUE, null,"NotPrinted", log) );
			assertFalse( log.hasEntries() );
		}
	}
}
