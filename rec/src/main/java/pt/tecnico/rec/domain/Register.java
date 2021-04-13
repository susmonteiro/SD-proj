package pt.tecnico.rec.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static pt.tecnico.rec.RecordMain.debug;

import pt.tecnico.rec.grpc.Rec.*;

public class Register {
    Map<RegisterValue.ValueCase, RegisterValue> register = new ConcurrentHashMap<>();

    public Register() {
    }

    public void setValue(RegisterValue.ValueCase type, RegisterValue value) {
        register.put(type, value);
    }

    @Override
    public String toString() {
        String output = "=== REGISTER CONTENT ===\n";
        for (Map.Entry<RegisterValue.ValueCase, RegisterValue> entry : register.entrySet()) {
            output += "Type: " + entry.getKey() +
                                 "\nValue: " + entry.getValue();
        }
        return output;
    }

}