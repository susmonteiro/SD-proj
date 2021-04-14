package pt.tecnico.rec.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static pt.tecnico.rec.RecordMain.debug;

import pt.tecnico.rec.grpc.Rec.*;

public class Register {
    Map<RegisterValue.ValueCase, RegisterValue> register = new ConcurrentHashMap<>();

    public Register() {
    }

    private RegisterValue setDefault(RegisterValue.ValueCase type) {
        RegisterValue defaultValue = RegisterValue.getDefaultInstance();
        this.setValue(type, defaultValue);
        return defaultValue;
    }

    public RegisterValue getValue(RegisterValue.ValueCase type) {
        return register.containsKey(type) ? register.get(type) : setDefault(type);
    }

    public void setValue(RegisterValue.ValueCase type, RegisterValue value) {
        register.put(type, value);
    }

    @Override
    public String toString() {
        String output = "=== REGISTER CONTENT ===\n";
        for (Map.Entry<RegisterValue.ValueCase, RegisterValue> entry : register.entrySet()) {
            output += "Type: " + entry.getKey() +
                        "\n" + entry.getValue();
        }
        return output;
    }

}