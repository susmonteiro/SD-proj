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

    public synchronized void setRegister(String id, 
            RegisterValue.ValueCase type, RegisterValue value) {

        Register register = registers.containsKey(id) ? registers.get(id) : addNewRegister(id);

        register.setValue(type, value);    
        debug(register);    
	}


}