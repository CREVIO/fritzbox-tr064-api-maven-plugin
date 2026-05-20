package de.crevio.fritzbox.tr064.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FritzConnectionTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private String host;
    private int port;

    @BeforeEach
    void setUp() {
        host = "localhost";
        port = wm.getPort();
        stubScpds();
    }

    private void stubScpds() {
        wm.stubFor(get(urlEqualTo("/deviceconfigSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/deviceconfigSCPD.xml"))));

        wm.stubFor(get(urlEqualTo("/userInterfaceSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/userInterfaceSCPD.xml"))));
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

    @Test
    void authenticatedConnectionLoadsServices() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/tr64desc.xml"))));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");
        fc.init();

        assertThat(fc.getServices()).isNotEmpty();
    }

    @Test
    void getServiceByName() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/tr64desc.xml"))));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");
        fc.init();

        assertThat(fc.getService("DeviceConfig:1")).isNotNull();
    }

    @Test
    void unknownServiceReturnsNull() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/tr64desc.xml"))));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");
        fc.init();

        assertThat(fc.getService("NoSuchService:1")).isNull();
    }

    @Test
    void nonOkHttpResponseThrowsIOException() {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");

        assertThatThrownBy(fc::init)
                .isInstanceOf(IOException.class);
    }

    @Test
    void executeRebootAction() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/tr64desc.xml"))));

        wm.stubFor(post(urlEqualTo("/upnp/control/deviceconfig"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/soap-reboot-response.xml"))));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");
        fc.init();

        Service service = fc.getService("DeviceConfig:1");
        assertThat(service).isNotNull();

        Action reboot = service.getAction("Reboot");
        assertThat(reboot).isNotNull();

        Response response = reboot.execute();
        assertThat(response).isNotNull();
    }

    @Test
    void unauthenticatedConnectionLoadsServicesViaIgdDesc() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/igddesc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/igddesc.xml"))));

        wm.stubFor(get(urlEqualTo("/igdicfgSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/igdicfgSCPD.xml"))));

        // No user/pwd → reads igddesc.xml
        FritzConnection fc = new FritzConnection(host, port);
        fc.init();

        assertThat(fc.getServices()).isNotEmpty();
        assertThat(fc.getService("WANCommonInterfaceConfig:1")).isNotNull();
    }

    @Test
    void executeGetInfoAction() throws IOException, JAXBException, NoSuchFieldException {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/tr64desc.xml"))));

        wm.stubFor(post(urlEqualTo("/upnp/control/deviceconfig"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/soap-getinfo-response.xml"))));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");
        fc.init();

        Service service = fc.getService("DeviceConfig:1");
        assertThat(service).isNotNull();

        Action getInfo = service.getAction("GetInfo");
        assertThat(getInfo).isNotNull();

        Response response = getInfo.execute();
        assertThat(response).isNotNull();

        String modelName = response.getValueAsString("NewModelName");
        assertThat(modelName).isEqualTo("FRITZ!Box 7590");
    }

    @Test
    void executeGetCommonLinkProperties_withIntegerResponse() throws IOException, JAXBException, NoSuchFieldException {
        wm.stubFor(get(urlEqualTo("/igddesc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/igddesc.xml"))));

        wm.stubFor(get(urlEqualTo("/igdicfgSCPD.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/igdicfgSCPD.xml"))));

        wm.stubFor(post(urlEqualTo("/igdupnp/control/WANCommonIFC1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/soap-wanconfig-response.xml"))));

        FritzConnection fc = new FritzConnection(host, port);
        fc.init();

        Service service = fc.getService("WANCommonInterfaceConfig:1");
        assertThat(service).isNotNull();

        Action action = service.getAction("GetCommonLinkProperties");
        assertThat(action).isNotNull();

        Response response = action.execute();
        assertThat(response).isNotNull();

        String accessType = response.getValueAsString("NewWANAccessType");
        assertThat(accessType).isEqualTo("DSL");

        int bitRate = response.getValueAsInteger("NewLayer1UpstreamMaxBitRate");
        assertThat(bitRate).isEqualTo(50000);
    }

    @Test
    void getServices_returnsAllServices() throws IOException, JAXBException {
        wm.stubFor(get(urlEqualTo("/tr64desc.xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml")
                        .withBody(loadFixture("fixtures/tr64desc.xml"))));

        FritzConnection fc = new FritzConnection(host, port, "admin", "secret");
        fc.init();

        assertThat(fc.getServices()).containsKey("DeviceConfig:1");
        assertThat(fc.getServices()).containsKey("UserInterface:1");
        assertThat(fc.getServices()).hasSize(2);
    }
}
