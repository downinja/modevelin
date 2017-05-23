package com.tibco.tibrv;

/**
 * Redefinition of com.tibco.tibrv.Tibrv. 
 * 
 * Note that if we are attaching the modevelin agent on startup, such that the real 
 * version of this class has not already been loaded, then we don't need to implement 
 * any methods which are not invoked as part of our use case, since the JVM will not 
 * come looking for them. 
 * 
 * The methods below are the ones needed to get the demo working, most likely these 
 * would need to be expanded to provide a more general purpose drop-in replacement for 
 * this class. Ideally, we would use the entire source file and just surgically remove
 * the calls we need to - so that potentially we could attache this redefinition after
 * the original class has been loaded. (In such cases, the JVM enforces that the 
 * redefined class is structurally identical to the one that it's replacing.) However 
 * this would probably bump into licensing issues if the code is not open source.
 *
 */
public class Tibrv {

	 public static void open(int paramInt) throws TibrvException {}
	 
	 public static TibrvQueue defaultQueue() {
		 return null;
	 }
	 
	 protected static boolean loadLibIfNeeded() {
		 return false;
	 }

}
