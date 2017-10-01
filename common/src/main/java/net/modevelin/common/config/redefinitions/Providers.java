package net.modevelin.common.config.redefinitions;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="providers")
public class Providers {

	@XmlElement(name="provider")
	private List<Provider> providers;

	public List<Provider> getProviders() {
		return Collections.unmodifiableList(providers);
	}
		
}
