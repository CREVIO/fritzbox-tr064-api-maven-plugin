package de.crevio.fritzbox.tr064.api;

import de.crevio.fritzbox.tr064.api.beans.ActionType;
import de.crevio.fritzbox.tr064.api.beans.ArgumentListType;
import de.crevio.fritzbox.tr064.api.beans.ArgumentType;
import de.crevio.fritzbox.tr064.api.beans.ServiceType;
import de.crevio.fritzbox.tr064.api.beans.StateVariableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionTest {

    /**
     * Minimal FritzConnection stub that never makes real HTTP calls.
     * Used so Action can be constructed without WireMock.
     */
    private static class StubFritzConnection extends FritzConnection {
        StubFritzConnection() {
            super("localhost", 49000);
        }
    }

    private Action actionNoArgs;
    private Action actionWithArgs;
    private ServiceType serviceType;

    @BeforeEach
    void setUp() {
        FritzConnection stubConnection = new StubFritzConnection();

        serviceType = new ServiceType();
        serviceType.setServiceType("urn:dslforum-org:service:DeviceConfig:1");
        serviceType.setControlURL("/upnp/control/deviceconfig");
        serviceType.setServiceId("urn:DeviceConfig-com:serviceId:DeviceConfig1");
        serviceType.setEventSubURL("/upnp/control/deviceconfig");
        serviceType.setSCPDURL("/deviceconfigSCPD.xml");

        StateVariableType modelNameVar = new StateVariableType();
        modelNameVar.setName("ModelName");
        modelNameVar.setDataType("string");

        StateVariableType counterVar = new StateVariableType();
        counterVar.setName("Counter");
        counterVar.setDataType("ui2");

        List<StateVariableType> stateVars = new ArrayList<>();
        stateVars.add(modelNameVar);
        stateVars.add(counterVar);

        // Action without arguments
        ActionType rebootActionType = new ActionType();
        rebootActionType.setName("Reboot");
        actionNoArgs = new Action(rebootActionType, stateVars, stubConnection, serviceType);

        // Action with arguments (one out, one in)
        ArgumentType outArg = new ArgumentType();
        outArg.setName("NewModelName");
        outArg.setDirection("out");
        outArg.setRelatedStateVariable("ModelName");

        ArgumentType inArg = new ArgumentType();
        inArg.setName("NewCounter");
        inArg.setDirection("in");
        inArg.setRelatedStateVariable("Counter");

        ArgumentListType argList = new ArgumentListType();
        argList.getArgument().add(outArg);
        argList.getArgument().add(inArg);

        ActionType getInfoActionType = new ActionType();
        getInfoActionType.setName("GetInfo");
        getInfoActionType.setArgumentList(argList);

        actionWithArgs = new Action(getInfoActionType, stateVars, stubConnection, serviceType);
    }

    @Test
    void getName() {
        assertThat(actionNoArgs.getName()).isEqualTo("Reboot");
        assertThat(actionWithArgs.getName()).isEqualTo("GetInfo");
    }

    @Test
    void getArguments() {
        assertThat(actionNoArgs.getArguments()).isEmpty();
        assertThat(actionWithArgs.getArguments()).containsExactlyInAnyOrder("NewModelName", "NewCounter");
    }

    @Test
    void isOut_returnsTrue_forOutArgument() {
        assertThat(actionWithArgs.isOut("NewModelName")).isTrue();
    }

    @Test
    void isIn_returnsTrue_forInArgument() {
        assertThat(actionWithArgs.isIn("NewCounter")).isTrue();
    }

    @Test
    void isOut_throwsException_forUnknownArgument() {
        assertThatThrownBy(() -> actionWithArgs.isOut("UnknownArgument"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getTypeOfArgument_returnsCorrectType() {
        assertThat(actionWithArgs.getTypeOfArgument("NewModelName")).isEqualTo(String.class);
        assertThat(actionWithArgs.getTypeOfArgument("NewCounter")).isEqualTo(Integer.class);
    }

    @Test
    void execute_throwsException_forMissingInArgument() {
        // GetInfo has "NewCounter" as in-argument; calling execute() without providing it should fail
        assertThatThrownBy(() -> actionWithArgs.execute())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Missing In-Arguments");
    }

    @Test
    void execute_throwsException_forUnknownArgument() {
        Map<String, Object> args = new HashMap<>();
        args.put("UnknownArg", "value");

        assertThatThrownBy(() -> actionWithArgs.execute(args))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("is not an Argument for");
    }

    @Test
    void toString_includesNameAndArguments() {
        String result = actionWithArgs.toString();

        assertThat(result).contains("GetInfo");
        assertThat(result).contains("NewModelName");
        assertThat(result).contains("NewCounter");
    }
}
