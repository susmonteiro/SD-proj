package pt.tecnico.rec.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.rec.grpc.Rec.*;
import static pt.tecnico.rec.RecordMain.debug;


public class Rec {
    private Map<String, Register> registers = new ConcurrentHashMap<>();

    public Rec() {
    }

    private Register addNewRegister(String id) {
        Register register = new Register();
        registers.put(id, register);
        return register;
        
    }

    public RegisterValue getRegister(String id, RegisterValue.ValueCase type) {
        
        Register register = registers.containsKey(id) ? registers.get(id) : addNewRegister(id);

        debug(register);
        return register.getValue(type);
    }

    public void setRegister(String id, 
            RegisterValue.ValueCase type, RegisterValue value) {
        
        // if registers doesnt exist, we add it atomically to registers
        registers.putIfAbsent(id, new Register());

        Register register = registers.get(id);        
        register.setValue(type, value);
        debug(register);
	}

}