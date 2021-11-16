package net.modevelin.server.bytesproviders;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarBytesSupport {

	
	public static byte[] getJarBytes(String jarFilePath, String classToRedefine) throws Exception {
		
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
					if (className.equals(classToRedefine)) {// || className.startsWith(classToRedefine + "$")) {
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
	
}
