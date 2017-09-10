package net.modevelin.common.bytes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.modevelin.common.ExceptionSupport;
import net.modevelin.common.bytes.support.JarBytesSupport;
import net.modevelin.common.config.Configuration;

public class JarBytesProvider extends JarBytesSupport implements BytesProvider {

	private final String jarFilePath;
	
	public JarBytesProvider(final Configuration configuration) {
		this.jarFilePath = configuration.getProperty("jarFilePath");
	}
	
	@Override
	public Map<String, byte[]> getBytes(final List<String> classNames) {
		Map<String, byte[]> bytesMap = new HashMap<>();
		for (String className : classNames) {
			try {
				byte[] classBytes = getJarBytes(jarFilePath, className);
				bytesMap.put(className.replaceAll("[.]", "/"), classBytes);
			}
			catch (Exception ex) {
				return ExceptionSupport.rethrowAsRuntimeException(ex);
			}
		}
		return bytesMap;
	}

}
