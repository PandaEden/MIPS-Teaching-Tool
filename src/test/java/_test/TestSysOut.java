package _test;

import util.ansi_codes.Color;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestSysOut {
	
	private final PrintStream standardOut=System.out;
	private final ByteArrayOutputStream outputStream=new ByteArrayOutputStream( );
	
	/** Setup - redirecting Standard Output */
	public TestSysOut ( ) {
		Color.colorSupport=false;
		System.setOut( new PrintStream( outputStream ) );
	}
	
	public ByteArrayOutputStream getOutput ( ) {
		return outputStream;
	}
	
	public String toString ( ) {
		return outputStream.toString();
	}
	/** Restore original System.Out */
	public void close(){
		System.setOut( standardOut );
		System.out.println(outputStream);
		outputStream.reset();
	}
}
