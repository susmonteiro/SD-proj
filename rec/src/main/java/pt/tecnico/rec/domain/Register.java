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
        debug("#setDefault");
        RegisterData defaultData = RegisterData.getDefaultInstance();
        this.setData(type, defaultData);
        return defaultData;
    }

    public RegisterData getData(RegisterValue.ValueCase type) {
        debug("#getData");
        return register.containsKey(type) ? register.get(type) : setDefault(type);
    }

    public void setData(RegisterValue.ValueCase type, RegisterData data){
        debug("#setData");
        // if data didnt exist, immediately add to register
        debug("Register constains this type of data? " + register.containsKey(type));
        // if it exists but tag is newer, then update register
        if (register.containsKey(type)) debug("Is the new tag more recent? " + isTagNewer(register.get(type).getTag(), data.getTag()));
        if (!register.containsKey(type) || isTagNewer(register.get(type).getTag(), data.getTag())) {
            register.put(type, data);
        }
    }

    @Override
    public String toString() {
        String output = "=== REGISTER CONTENT ===\n";
        for (Map.Entry<RegisterValue.ValueCase, RegisterData> entry : register.entrySet()) {
            output += "Type: " + entry.getKey() +
                        "\nTag: " + entry.getValue().getTag() +
                        "\nContent: " + entry.getValue().getValue();
        }
        return output;
    }

}