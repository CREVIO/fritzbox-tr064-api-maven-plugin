package de.crevio.fritzbox.tr064.api;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.crevio.fritzbox.tr064.api.beans.ServiceType;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private FritzConnection connection;

    @BeforeEach
    void setUp() {
        // A minimal FritzConnection pointing at WireMock
        connection = new FritzConnection("localhost", wm.getPort());
    }

    private byte[] loadFixture(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("Fixture not found: " + path);
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load fixture: " + path, e);
        }
    }

    private ServiceType buildDeviceConfigServiceType() {
        ServiceType st = new ServiceType();
        st.setServiceType("urn:dslforum-org:service:DeviceConfig:1");
        st.setServiceId("urn:DeviceConfig-com:serviceId:DeviceConfig1");
        st.setControlURL("/upnp/control/deviceconfig");
        st.setEventSubURL("/upnp/control/deviceconfig");
        st.setSCPDURL("/deviceconfigSCPD.xml");
        return st;
    }

    @Test
    void loadServiceFromScpdXml() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/deviceconfigSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/deviceconfigSCPD.xml"))));

        Service service = new Service(buildDeviceConfigServiceType(), connection);

        assertThat(service).isNotNull();
        assertThat(service.getActions()).isNotEmpty();
        assertThat(service.getActions()).containsKey("Reboot");
        assertThat(service.getActions()).containsKey("GetInfo");
    }

    @Test
    void getAction_returnsCorrectAction() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/deviceconfigSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/deviceconfigSCPD.xml"))));

        Service service = new Service(buildDeviceConfigServiceType(), connection);

        Action reboot = service.getAction("Reboot");
        assertThat(reboot).isNotNull();
        assertThat(reboot.getName()).isEqualTo("Reboot");

        Action getInfo = service.getAction("GetInfo");
        assertThat(getInfo).isNotNull();
        assertThat(getInfo.getName()).isEqualTo("GetInfo");
    }

    @Test
    void toString_returnsServiceType() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/deviceconfigSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/deviceconfigSCPD.xml"))));

        Service service = new Service(buildDeviceConfigServiceType(), connection);

        assertThat(service.toString()).isEqualTo("urn:dslforum-org:service:DeviceConfig:1");
    }
}
