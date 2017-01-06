package net.modevelin.core.server;

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ClassRedefinitionFactory {

	private final Map<String, byte[]> initialDefinitions;
	
	public ClassRedefinitionFactory() throws Exception {
		
		this.initialDefinitions = new TreeMap<>();
		
		ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES);
		
		ClassVisitor classVisitor = new ClassVisitor(ASM5, classWriter) {
			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
				if ("execute".equals(name)) {
					// if method we want then
					return new MethodVisitor(ASM5, cv.visitMethod(access, name, desc, signature, exceptions)) {
						@Override
						public void visitCode() {
							mv.visitCode();
							// do additional stuff here
							mv.visitVarInsn(ALOAD, 1);
							mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
							mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
							mv.visitLdcInsn("net.modevelin.test.Main");
							mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
							mv.visitVarInsn(ASTORE, 2);
							mv.visitVarInsn(ALOAD, 2);
							mv.visitLdcInsn("process");
							mv.visitInsn(ICONST_1);
							mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
							mv.visitInsn(DUP);
							mv.visitInsn(ICONST_0);
							mv.visitLdcInsn(Type.getType("Ljava/lang/Runnable;"));
							mv.visitInsn(AASTORE);
							mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
							mv.visitVarInsn(ASTORE, 3);
							mv.visitVarInsn(ALOAD, 3);
							mv.visitInsn(ICONST_1);
							mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V", false);
							mv.visitVarInsn(ALOAD, 3);
							mv.visitInsn(ACONST_NULL);
							mv.visitInsn(ICONST_1);
							mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
							mv.visitInsn(DUP);
							mv.visitInsn(ICONST_0);
							mv.visitVarInsn(ALOAD, 1);
							mv.visitInsn(AASTORE);
							mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
							mv.visitTypeInsn(CHECKCAST, "java/lang/Runnable");
							mv.visitVarInsn(ASTORE, 1);
						}						
					};
				}
				else {
					return cv.visitMethod(access, name, desc, signature, exceptions);
				}
				
			}
		};
		ClassReader classReader = new ClassReader(Type.getInternalName(ThreadPoolExecutor.class));
		classReader.accept(classVisitor, SKIP_DEBUG);
		byte[] bytes = classWriter.toByteArray();
		String internalName = Type.getInternalName(ThreadPoolExecutor.class);
		initialDefinitions.put(internalName, bytes);
		
		String tibrvJarPath = "C:\\Users\\john\\dev\\github\\modevelin\\tibrvj\\target\\modevelin-tibrvj-1.0.0-SNAPSHOT.jar";
		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.TibrvCmTransport");
		initialDefinitions.put("com/tibco/tibrv/TibrvCmTransport", bytes);
		
		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.TibrvListener");
		initialDefinitions.put("com/tibco/tibrv/TibrvListener", bytes);
		
		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.TibrvRvdTransport");
		initialDefinitions.put("com/tibco/tibrv/TibrvRvdTransport", bytes);
		
		bytes = getJarBytes(tibrvJarPath, "com.tibco.tibrv.Tibrv");
		initialDefinitions.put("com/tibco/tibrv/Tibrv", bytes);
		
	}
	
	private static byte[] getJarBytes(String jarFilePath, String classToRedefine) throws Exception {
		
		if (jarFilePath.startsWith("/")) {
			jarFilePath = jarFilePath.substring(1);
		}
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarFilePath);
			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				String jarEntryName = jarEntry.getName(); 
				if (jarEntryName.endsWith(".class")) {
					String className = jarEntryName.substring(0, jarEntryName.length() - 6);
					classToRedefine = classToRedefine.replace('.', '/');
					if (className.equals(classToRedefine) || className.startsWith(classToRedefine + "$")) {
						//LOGGER.info("getting bytes for redefinition of {}", className);
						InputStream is = jarFile.getInputStream(jarEntry);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						int nextByte = is.read();
						while (nextByte != -1) {
							baos.write(nextByte);
							nextByte = is.read();
						}
						is.close();
						byte[] classBytes = baos.toByteArray();
						return classBytes;
					}
				}
			}
		}
		finally {
			if (jarFile != null) {
				jarFile.close();
			}
		}
		return null;
	}
	
	
	public Map<String, byte[]> getRedefinitions(final String agentName, final String command) {
		return initialDefinitions;
	}
	
}
