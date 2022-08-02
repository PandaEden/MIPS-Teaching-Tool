package util;

import _test.Tags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import util.validation.InstrSpec;

import static org.junit.jupiter.api.Assertions.*;

@Tag( Tags.Pkg.UTIL )
class UtilTest {
	
	@Test
	@Tag ( Tags.MULTIPLE )
	@DisplayName ( "Valid notNullInRange" )
	void notNullInRange ( ) {
		assertTrue( Util.notNullAndInRange( 4, 4, 5 ) );
		
		assertTrue( Util.notNullAndInRange( 0, 0, 31 ) );
		
		assertFalse( Util.notNullAndInRange( 3, 4, 5 ) );
		assertFalse( Util.notNullAndInRange( null, 0, 5 ) );
		
		//noinspection ResultOfMethodCallIgnored
		assertThrows( IllegalArgumentException.class, ( ) -> Util.notNullAndInRange( 3, 5, 4 ) );
		
		//noinspection ResultOfMethodCallIgnored
		assertThrows( IllegalArgumentException.class, ( ) -> Util.notNullAndInRange( null, 5, 4 ) );
	}
	
	@Test
	@DisplayName ( "Coverage Completion" )
	void coverage_Completion ( ) { // TODO refactor splitValidOperands
		assertEquals(3, InstrSpec.findSpec( "add" ).getNUM_OPERANDS());
		assertEquals( InstrSpec.FMT.RD_RS_RT, InstrSpec.findSpec( "add" ).getFORMAT_TYPE());
		
	}
}
