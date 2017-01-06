package net.modevelin.executors;

import static net.modevelin.common.ExceptionSupport.*;

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
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import net.modevelin.common.bytes.BytesProvider;

/**
 * Implementation of BytesProvider which returns a modified version of ThreadPoolExecutor.
 * Specifically, it modifies the execute method so that before processing the Runnable, the
 * TPE hands it off to a client-specified method - which can then do what it likes with it. 
 * (But which should, one way or another, return a Runnable - be it the same one or a different 
 * one - for subsequent processing by TPE). 
 * 
 * In terms of why use ASM for this, then it would be a PITA to have to decompile/mod/recompile
 * the source code for the various Executor classes involved - and the source will potentially
 * change from Java version to Java version. Whereas this way, we just inject a quick bit
 * of code on entry to the execute(Runnable) method, and don't otherwise touch the rest of 
 * the method. Also, this class drives another way of implementing BytesProvider e.g.
 * bytecode on the fly, rather than being read from a .class file somewhere.
 * 
 * TODO, is there an easier way of doing this than via ASM? Config driven, for example - via
 * a snippet of code?
 * 
 * @author
 *
 */
public class ThreadPoolExecutorAsmProvider implements BytesProvider {
	
	public Map<String, byte[]> getBytes(final List<String> fileNames) {
		
		try {
			
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
								mv.visitVarInsn(ASTORE, 2);
								mv.visitLdcInsn("net.modevelin.executors.ExecutorsProvider.classToInvoke");
								mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
								mv.visitVarInsn(ASTORE, 3);
								mv.visitLdcInsn("net.modevelin.executors.ExecutorsProvider.methodToInvoke");
								mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;", false);
								mv.visitVarInsn(ASTORE, 4);
								mv.visitVarInsn(ALOAD, 2);
								mv.visitVarInsn(ALOAD, 3);
								mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
								mv.visitVarInsn(ASTORE, 5);
								mv.visitVarInsn(ALOAD, 5);
								mv.visitVarInsn(ALOAD, 4);
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
			Map<String, byte[]> map = new HashMap<>();
			map.put(internalName, bytes);
			return map;
		}
		catch (Exception ex) {
			return rethrowAsRuntimeException(ex);
		}
	}

}
