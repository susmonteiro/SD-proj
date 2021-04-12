package pt.tecnico.rec.domain;

import java.util.Map; 
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.rec.grpc.Rec.*;

public class Rec {
    private Map<String, RegisterValue> registers = new ConcurrentHashMap<String, RegisterValue>();

    public Rec() {
    }

}
