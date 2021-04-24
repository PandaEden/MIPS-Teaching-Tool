package util;

import org.jetbrains.annotations.Nullable;

public class Util {
	
	/** Min and Max Inclusive */
	public static boolean notNullAndInRange (Integer val, int min, int max) {
		if ( max<min )
			throw new IllegalArgumentException( "Max " + max + " is larger than Min " + min + " What?" );
		
		return (val!=null && val>=min && val<=max);
	}
	
	public static boolean isNullOrBlank (@Nullable String s){ return (s==null || s.isBlank()); }
}
