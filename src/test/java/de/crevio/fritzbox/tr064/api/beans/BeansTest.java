package de.crevio.fritzbox.tr064.api.beans;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the JAXB bean classes (mostly getters/setters) to ensure sufficient coverage.
 */
class BeansTest {

    @Test
    void deviceType_gettersAndSetters() {
        DeviceType dt = new DeviceType();
        dt.setDeviceType("urn:test:device:1");
        dt.setFriendlyName("Test Device");
        dt.setManufacturer("ACME");
        dt.setManufacturerURL("http://acme.example.com");
        dt.setModelDescription("Test Model");
        dt.setModelName("Model X");
        dt.setModelNumber("MX-1");
        dt.setModelURL("http://model.example.com");
        dt.setUDN("uuid:12345678-1234-1234-1234-123456789abc");
        dt.setUPC("12345");
        dt.setPresentationURL("http://device.example.com");

        assertThat(dt.getDeviceType()).isEqualTo("urn:test:device:1");
        assertThat(dt.getFriendlyName()).isEqualTo("Test Device");
        assertThat(dt.getManufacturer()).isEqualTo("ACME");
        assertThat(dt.getManufacturerURL()).isEqualTo("http://acme.example.com");
        assertThat(dt.getModelDescription()).isEqualTo("Test Model");
        assertThat(dt.getModelName()).isEqualTo("Model X");
        assertThat(dt.getModelNumber()).isEqualTo("MX-1");
        assertThat(dt.getModelURL()).isEqualTo("http://model.example.com");
        assertThat(dt.getUDN()).isEqualTo("uuid:12345678-1234-1234-1234-123456789abc");
        assertThat(dt.getUPC()).isEqualTo("12345");
        assertThat(dt.getPresentationURL()).isEqualTo("http://device.example.com");
    }

    @Test
    void deviceType_serviceList_and_deviceList() {
        DeviceType dt = new DeviceType();

        ServiceListType serviceList = new ServiceListType();
        ServiceType st = new ServiceType();
        st.setServiceType("urn:test:service:1");
        serviceList.getService().add(st);
        dt.setServiceList(serviceList);

        DeviceListType deviceList = new DeviceListType();
        deviceList.getDevice().add(new DeviceType());
        dt.setDeviceList(deviceList);

        IconListType iconList = new IconListType();
        dt.setIconList(iconList);

        assertThat(dt.getServiceList()).isNotNull();
        assertThat(dt.getServiceList().getService()).hasSize(1);
        assertThat(dt.getDeviceList()).isNotNull();
        assertThat(dt.getDeviceList().getDevice()).hasSize(1);
        assertThat(dt.getIconList()).isNotNull();
    }

    @Test
    void deviceListType_returnsEmptyListWhenNull() {
        DeviceListType dlt = new DeviceListType();
        List<DeviceType> devices = dlt.getDevice();
        assertThat(devices).isNotNull();
        assertThat(devices).isEmpty();

        // Second call returns same list
        assertThat(dlt.getDevice()).isSameAs(devices);
    }

    @Test
    void serviceListType_returnsEmptyListWhenNull() {
        ServiceListType slt = new ServiceListType();
        List<ServiceType> services = slt.getService();
        assertThat(services).isNotNull();
        assertThat(services).isEmpty();
    }

    @Test
    void serviceType_allGettersAndSetters() {
        ServiceType st = new ServiceType();
        st.setServiceType("urn:dslforum-org:service:DeviceConfig:1");
        st.setServiceId("urn:DeviceConfig-com:serviceId:DeviceConfig1");
        st.setControlURL("/upnp/control/deviceconfig");
        st.setEventSubURL("/upnp/control/deviceconfig");
        st.setSCPDURL("/deviceconfigSCPD.xml");

        assertThat(st.getServiceType()).isEqualTo("urn:dslforum-org:service:DeviceConfig:1");
        assertThat(st.getServiceId()).isEqualTo("urn:DeviceConfig-com:serviceId:DeviceConfig1");
        assertThat(st.getControlURL()).isEqualTo("/upnp/control/deviceconfig");
        assertThat(st.getEventSubURL()).isEqualTo("/upnp/control/deviceconfig");
        assertThat(st.getSCPDURL()).isEqualTo("/deviceconfigSCPD.xml");
    }

    @Test
    void actionType_gettersAndSetters() {
        ActionType at = new ActionType();
        at.setName("TestAction");

        ArgumentListType argList = new ArgumentListType();
        ArgumentType arg = new ArgumentType();
        arg.setName("TestArg");
        arg.setDirection("in");
        arg.setRelatedStateVariable("TestVar");
        argList.getArgument().add(arg);
        at.setArgumentList(argList);

        assertThat(at.getName()).isEqualTo("TestAction");
        assertThat(at.getArgumentList()).isNotNull();
        assertThat(at.getArgumentList().getArgument()).hasSize(1);
    }

    @Test
    void argumentType_gettersAndSetters() {
        ArgumentType arg = new ArgumentType();
        arg.setName("NewValue");
        arg.setDirection("out");
        arg.setRelatedStateVariable("SomeVar");

        assertThat(arg.getName()).isEqualTo("NewValue");
        assertThat(arg.getDirection()).isEqualTo("out");
        assertThat(arg.getRelatedStateVariable()).isEqualTo("SomeVar");
    }

    @Test
    void argumentListType_returnsEmptyListWhenNull() {
        ArgumentListType alt = new ArgumentListType();
        assertThat(alt.getArgument()).isNotNull();
        assertThat(alt.getArgument()).isEmpty();
    }

    @Test
    void stateVariableType_gettersAndSetters() {
        StateVariableType sv = new StateVariableType();
        sv.setName("ModelName");
        sv.setDataType("string");
        sv.setSendEvents("no");

        assertThat(sv.getName()).isEqualTo("ModelName");
        assertThat(sv.getDataType()).isEqualTo("string");
        assertThat(sv.getSendEvents()).isEqualTo("no");
    }

    @Test
    void scpdType_gettersAndSetters() {
        ScpdType scpd = new ScpdType();
        ServiceSpecVersionType specVersion = new ServiceSpecVersionType();
        ActionListType actionList = new ActionListType();
        ServiceStateTableType sst = new ServiceStateTableType();

        scpd.setSpecVersion(specVersion);
        scpd.setActionList(actionList);
        scpd.setServiceStateTable(sst);

        assertThat(scpd.getSpecVersion()).isSameAs(specVersion);
        assertThat(scpd.getActionList()).isSameAs(actionList);
        assertThat(scpd.getServiceStateTable()).isSameAs(sst);
    }

    @Test
    void scpdType2_gettersAndSetters() {
        ScpdType2 scpd = new ScpdType2();
        ServiceSpecVersionType specVersion = new ServiceSpecVersionType();
        ActionListType actionList = new ActionListType();
        ServiceStateTableType sst = new ServiceStateTableType();

        scpd.setSpecVersion(specVersion);
        scpd.setActionList(actionList);
        scpd.setServiceStateTable(sst);

        assertThat(scpd.getSpecVersion()).isSameAs(specVersion);
        assertThat(scpd.getActionList()).isSameAs(actionList);
        assertThat(scpd.getServiceStateTable()).isSameAs(sst);
    }

    @Test
    void rootType_gettersAndSetters() {
        RootType root = new RootType();
        DeviceSpecVersionType specVersion = new DeviceSpecVersionType();
        DeviceType device = new DeviceType();

        root.setSpecVersion(specVersion);
        root.setDevice(device);

        assertThat(root.getSpecVersion()).isSameAs(specVersion);
        assertThat(root.getDevice()).isSameAs(device);
    }

    @Test
    void rootType2_gettersAndSetters() {
        RootType2 root = new RootType2();
        DeviceSpecVersionType specVersion = new DeviceSpecVersionType();
        DeviceType device = new DeviceType();

        root.setSpecVersion(specVersion);
        root.setDevice(device);

        assertThat(root.getSpecVersion()).isSameAs(specVersion);
        assertThat(root.getDevice()).isSameAs(device);
    }

    @Test
    void deviceSpecVersionType_gettersAndSetters() {
        DeviceSpecVersionType dsv = new DeviceSpecVersionType();
        dsv.setMajor((byte) 1);
        dsv.setMinor((byte) 0);

        assertThat(dsv.getMajor()).isEqualTo((byte) 1);
        assertThat(dsv.getMinor()).isEqualTo((byte) 0);
    }

    @Test
    void serviceSpecVersionType_gettersAndSetters() {
        ServiceSpecVersionType ssv = new ServiceSpecVersionType();
        ssv.setMajor((byte) 1);
        ssv.setMinor((byte) 0);

        assertThat(ssv.getMajor()).isEqualTo((byte) 1);
        assertThat(ssv.getMinor()).isEqualTo((byte) 0);
    }

    @Test
    void actionListType_returnsEmptyListWhenNull() {
        ActionListType alt = new ActionListType();
        assertThat(alt.getAction()).isNotNull();
        assertThat(alt.getAction()).isEmpty();
    }

    @Test
    void serviceStateTableType_returnsEmptyListWhenNull() {
        ServiceStateTableType sst = new ServiceStateTableType();
        assertThat(sst.getStateVariable()).isNotNull();
        assertThat(sst.getStateVariable()).isEmpty();
    }

    @Test
    void iconListType_getAndSetIcon() {
        IconListType ilt = new IconListType();
        assertThat(ilt.getIcon()).isNull();

        IconType icon = new IconType();
        icon.setMimetype("image/png");
        ilt.setIcon(icon);

        assertThat(ilt.getIcon()).isSameAs(icon);
    }

    @Test
    void iconType_gettersAndSetters() {
        IconType icon = new IconType();
        icon.setMimetype("image/png");
        icon.setWidth((byte) 32);
        icon.setHeight((byte) 32);
        icon.setDepth((byte) 8);
        icon.setUrl("/icon.png");

        assertThat(icon.getMimetype()).isEqualTo("image/png");
        assertThat(icon.getWidth()).isEqualTo((byte) 32);
        assertThat(icon.getHeight()).isEqualTo((byte) 32);
        assertThat(icon.getDepth()).isEqualTo((byte) 8);
        assertThat(icon.getUrl()).isEqualTo("/icon.png");
    }

    @Test
    void objectFactory_createsAllTypes() {
        ObjectFactory factory = new ObjectFactory();

        assertThat(factory.createScpdType()).isNotNull();
        assertThat(factory.createSpecVersionType()).isNotNull();
        assertThat(factory.createArgumentType()).isNotNull();
        assertThat(factory.createArgumentListType()).isNotNull();
        assertThat(factory.createActionType()).isNotNull();
        assertThat(factory.createActionListType()).isNotNull();
        assertThat(factory.createStateVariableType()).isNotNull();
        assertThat(factory.createServiceStateTableType()).isNotNull();
        assertThat(factory.createScpd(new ScpdType())).isNotNull();
    }
}
