package net.modevelin.common;

import java.util.List;
import java.util.Map;

public interface BytesProvider {
	
	Map<String, byte[]> getBytes(List<String> classNames);

}
