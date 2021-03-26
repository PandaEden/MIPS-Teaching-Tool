package model.components;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}
