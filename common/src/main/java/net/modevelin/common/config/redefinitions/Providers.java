package net.modevelin.common.config.redefinitions;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="providers")
public class Providers {

	@XmlElement(name="provider")
	private List<Provider> providers;

	public List<Provider> getProviders() {
		return Collections.unmodifiableList(providers);
	}
		
}
