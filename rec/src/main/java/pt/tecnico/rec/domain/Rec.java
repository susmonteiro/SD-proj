package pt.tecnico.rec.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.rec.domain.exception.InvalidArgumentException;
import pt.tecnico.rec.domain.exception.InvalidIdException;
import pt.tecnico.rec.domain.exception.NoRegisterValueSetException;
import pt.tecnico.rec.grpc.Rec.*;
import static pt.tecnico.rec.RecordMain.debug;


public class Rec {
    private Map<String, Register> registers = new ConcurrentHashMap<>();

    public Rec() {
    }

    private void checkId(String id) throws InvalidIdException {
        if (id == null || id.isBlank()) { 
            debug("!! Invalid id");
            throw new InvalidIdException(); 
        }
    }

    private void checkType(RegisterValue.ValueCase type) throws NoRegisterValueSetException {
        if (type == RegisterValue.ValueCase.VALUE_NOT_SET) { 
            debug("!! Value Case type not set");
            throw new NoRegisterValueSetException(); 
        }
    }


    private void checkInput(String id, RegisterValue.ValueCase type) throws InvalidArgumentException {
        checkId(id);
        checkType(type);
    }

    private Register addNewRegister(String id) {
        debug("#addNewRegister");
        Register register = new Register();
        registers.put(id, register);
        return register;
        
    }

    public RegisterData getRegister(String id, RegisterValue.ValueCase type) 
            throws InvalidArgumentException {
        debug("=== READ OPERATION ===");
        debug("#getRegister");
        checkInput(id, type);
        
        Register register = registers.containsKey(id) ? registers.get(id) : addNewRegister(id);

        debug(register);
        return register.getData(type);
    }

    public void setRegister(String id, RegisterValue.ValueCase type, RegisterData data) 
            throws InvalidArgumentException {
        debug("=== WRITE OPERATION ===");
        debug("#setRegister");
        debug("> input:\t" + data);
        checkInput(id, type);
        
        // if registers doesnt exist, we add it atomically to registers
        registers.putIfAbsent(id, new Register());

        Register register = registers.get(id);        
        register.setData(type, data);
        debug("> register after write operation");
        debug(register);
	}

}