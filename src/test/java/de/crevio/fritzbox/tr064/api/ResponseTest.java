package de.crevio.fritzbox.tr064.api;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResponseTest {

    private static final String SOAP_TEMPLATE = """
            <?xml version="1.0"?>
            <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
              <s:Header/>
              <s:Body>
                <u:TestResponse xmlns:u="urn:test">
                  %s
                </u:TestResponse>
              </s:Body>
            </s:Envelope>
            """;

    private SOAPMessage parseSoap(String body) throws IOException, SOAPException {
        String xml = SOAP_TEMPLATE.formatted(body);
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        return MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(bytes));
    }

    private Response buildResponse(String soapBody, Map<String, Type> stateToType, Map<String, String> argumentState)
            throws IOException, SOAPException {
        SOAPMessage msg = parseSoap(soapBody);
        try {
            return new Response(msg, stateToType, argumentState);
        } catch (SOAPException e) {
            throw new IOException(e);
        }
    }

    @Test
    void getValueAsString_returnsCorrectValue() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("ModelName", String.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewModelName", "ModelName");

        Response response = buildResponse("<NewModelName>FRITZ!Box 7590</NewModelName>", stateToType, argumentState);

        assertThat(response).isNotNull();
        assertThat(response.getValueAsString("NewModelName")).isEqualTo("FRITZ!Box 7590");
    }

    @Test
    void getValueAsString_throwsNoSuchFieldException_whenArgumentMissing() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        Map<String, String> argumentState = new HashMap<>();

        Response response = buildResponse("", stateToType, argumentState);

        assertThatThrownBy(() -> response.getValueAsString("NonExistent"))
                .isInstanceOf(NoSuchFieldException.class);
    }

    @Test
    void getValueAsInteger_returnsCorrectValue() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Counter", Integer.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewCounter", "Counter");

        Response response = buildResponse("<NewCounter>42</NewCounter>", stateToType, argumentState);

        assertThat(response.getValueAsInteger("NewCounter")).isEqualTo(42);
    }

    @Test
    void getValueAsInteger_throwsClassCastException_whenWrongType() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Name", String.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewName", "Name");

        Response response = buildResponse("<NewName>hello</NewName>", stateToType, argumentState);

        assertThatThrownBy(() -> response.getValueAsInteger("NewName"))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void getValueAsInteger_throwsNoSuchFieldException_whenMissing() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        Map<String, String> argumentState = new HashMap<>();

        Response response = buildResponse("", stateToType, argumentState);

        assertThatThrownBy(() -> response.getValueAsInteger("NonExistent"))
                .isInstanceOf(NoSuchFieldException.class);
    }

    @Test
    void getValueAsBoolean_returnsTrue_forValueOne() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Flag", Boolean.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewFlag", "Flag");

        Response response = buildResponse("<NewFlag>1</NewFlag>", stateToType, argumentState);

        assertThat(response.getValueAsBoolean("NewFlag")).isTrue();
    }

    @Test
    void getValueAsBoolean_returnsFalse_forValueZero() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Flag", Boolean.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewFlag", "Flag");

        Response response = buildResponse("<NewFlag>0</NewFlag>", stateToType, argumentState);

        assertThat(response.getValueAsBoolean("NewFlag")).isFalse();
    }

    @Test
    void getValueAsBoolean_throwsClassCastException_forInvalidValue() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Flag", Boolean.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewFlag", "Flag");

        Response response = buildResponse("<NewFlag>maybe</NewFlag>", stateToType, argumentState);

        assertThatThrownBy(() -> response.getValueAsBoolean("NewFlag"))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void getValueAsDate_returnsDate() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Timestamp", Date.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewTimestamp", "Timestamp");

        Response response = buildResponse("<NewTimestamp>2024-01-15T10:30:00</NewTimestamp>", stateToType, argumentState);

        assertThat(response.getValueAsDate("NewTimestamp")).isNotNull();
        assertThat(response.getValueAsDate("NewTimestamp")).isInstanceOf(Date.class);
    }

    @Test
    void getValueAsUUID_returnsUUID() throws Exception {
        String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("DeviceId", UUID.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewDeviceId", "DeviceId");

        Response response = buildResponse("<NewDeviceId>" + uuidStr + "</NewDeviceId>", stateToType, argumentState);

        assertThat(response.getValueAsUUID("NewDeviceId")).isEqualTo(UUID.fromString(uuidStr));
    }

    @Test
    void getData_returnsDataMap() throws Exception {
        Map<String, Type> stateToType = new HashMap<>();
        stateToType.put("Name", String.class);

        Map<String, String> argumentState = new HashMap<>();
        argumentState.put("NewName", "Name");

        Response response = buildResponse("<NewName>TestValue</NewName>", stateToType, argumentState);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).containsKey("NewName");
        assertThat(response.getData().get("NewName")).isEqualTo("TestValue");
    }
}
