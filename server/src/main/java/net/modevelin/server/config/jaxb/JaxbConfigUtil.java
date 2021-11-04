package net.modevelin.server.config.jaxb;

import jakarta.xml.bind.JAXB;
import net.modevelin.common.config.redefinitions.Redefinitions;
import net.modevelin.common.config.registrations.Registrations;

import java.io.IOException;
import java.io.InputStream;

public class JaxbConfigUtil {

    public static Redefinitions unmarshallRedefinitions(final String redefinitionsFile) throws IOException {
        try (InputStream is = JaxbConfigUtil.class.getResourceAsStream(redefinitionsFile)) {
            return JAXB.unmarshal(is, Redefinitions.class);
        }
    }

    public static Registrations unmarshallRegistrations(final String registrationsFile) throws IOException {
        try (InputStream is = JaxbConfigUtil.class.getResourceAsStream(registrationsFile)) {
            return JAXB.unmarshal(is, Registrations.class);
        }
    }

}
