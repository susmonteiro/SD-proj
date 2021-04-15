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
        if (id == null || id.isBlank()) { throw new InvalidIdException(); }
    }

    private void checkType(RegisterValue.ValueCase type) throws NoRegisterValueSetException {
        if (type == RegisterValue.ValueCase.VALUE_NOT_SET) { throw new NoRegisterValueSetException(); }
    }

    private void checkInput(String id, RegisterValue.ValueCase type) throws InvalidArgumentException {
            checkId(id);
            checkType(type);
    }

    private Register addNewRegister(String id) {
        Register register = new Register();
        registers.put(id, register);
        return register;
        
    }

    public RegisterValue getRegister(String id, RegisterValue.ValueCase type) 
            throws InvalidArgumentException {
        checkInput(id, type);
        
        Register register = registers.containsKey(id) ? registers.get(id) : addNewRegister(id);

        debug(register);
        return register.getValue(type);
    }

    public void setRegister(String id, RegisterValue.ValueCase type, RegisterValue value) 
            throws InvalidArgumentException {
        checkInput(id, type);
        
        // if registers doesnt exist, we add it atomically to registers
        registers.putIfAbsent(id, new Register());

        Register register = registers.get(id);        
        register.setValue(type, value);
        debug(register);
	}

}