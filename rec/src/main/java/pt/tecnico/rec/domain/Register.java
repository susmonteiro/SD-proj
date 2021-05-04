package pt.tecnico.rec.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static pt.tecnico.rec.RecordMain.debug;

import pt.tecnico.rec.grpc.Rec.*;

public class Register {
    Map<RegisterValue.ValueCase, RegisterData> register = new ConcurrentHashMap<>();

    public Register() {
    }

    private boolean isTagNewer(RegisterTag oldTag, RegisterTag newTag) {
        if (oldTag.getSeqNumber() < newTag.getSeqNumber()) { return true; }
        // useless for now, but will need this in case of multiple hubs
        if (oldTag.getSeqNumber() == newTag.getSeqNumber() && oldTag.getClientID() < newTag.getClientID()) { return true; }
        // else new tag is older than the previous
        return false;
    }

    private RegisterData setDefault(RegisterValue.ValueCase type) {
        RegisterData defaultData = RegisterData.getDefaultInstance();
        this.setData(type, defaultData);
        return defaultData;
    }

    public RegisterData getData(RegisterValue.ValueCase type) {
        return register.containsKey(type) ? register.get(type) : setDefault(type);
    }

    public void setData(RegisterValue.ValueCase type, RegisterData data){
        if (isTagNewer(data.getTag(), register.get(type).getTag()))
            register.put(type, data);
    }

    @Override
    public String toString() {
        String output = "=== REGISTER CONTENT ===\n";
        for (Map.Entry<RegisterValue.ValueCase, RegisterData> entry : register.entrySet()) {
            output += "Type: " + entry.getKey() +
                        "\n" + entry.getValue();
        }
        return output;
    }

}