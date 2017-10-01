package net.modevelin.common.config.fixtures;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="messageTypes")
public class MessageTypes {

	@XmlElement(name="messageType")
	private List<MessageType> messageTypes;

	public List<MessageType> getMessageTypes() {
		return Collections.unmodifiableList(messageTypes);
	}
		
}
