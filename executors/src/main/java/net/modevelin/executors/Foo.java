package net.modevelin.executors;

import java.lang.reflect.Method;

public class Foo {

	public void execute(Runnable runnable) throws Exception {
		ClassLoader loader = getClass().getClassLoader();
		String classToInvoke = System.getProperty("net.modevelin.executors.ExecutorsProvider.classToInvoke");
		String methodToInvoke = System.getProperty("net.modevelin.executors.ExecutorsProvider.methodToInvoke");
		Class<?> clazz = loader.loadClass(classToInvoke);
		Method method = clazz.getMethod(methodToInvoke, Runnable.class);
		method.setAccessible(true);
		method.invoke(null, runnable);
		
		
	}
	
}
