package net.modevelin.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

public class ExceptionSupport {

	public static Throwable unwrap(final Throwable t) {

		if (isKnownExceptionWrapper(t)) {
			Throwable cause = t.getCause();
			return recursiveExtract(cause, t);
		}

		if (t instanceof RuntimeException) {
			Throwable cause = t.getCause();
			if (cause != null) {
				if (isKnownExceptionWrapper(cause)) {
					Throwable causeCause = cause.getCause();
					return recursiveExtract(causeCause, cause);
				}
				return cause;
			}
		}

		return t;
	}

	private static boolean isKnownExceptionWrapper(final Throwable possibleWrapper) {
		return
			possibleWrapper instanceof InvocationTargetException ||
			possibleWrapper instanceof RuntimeMBeanException ||
			possibleWrapper instanceof RuntimeOperationsException ||
			possibleWrapper instanceof UndeclaredThrowableException;
	}

	private static Throwable recursiveExtract(final Throwable extracted, final Throwable extractedFrom) {
		if (extracted == null) {
			return extractedFrom;
		}
		if (extracted == extractedFrom) {
			return extractedFrom;
		}
		return unwrap(extracted);
	}

	public static <E> E rethrowAsRuntimeException(final Throwable t) {
		Throwable unwrapped = ExceptionSupport.unwrap(t);
		if (unwrapped instanceof RuntimeException) {
			throw (RuntimeException)unwrapped;
		}
		throw new RuntimeException(unwrapped);
	}


}
