package pt.tecnico.rec.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.tecnico.rec.grpc.Rec.*;

public class Register {
    Map<RegisterValue.ValueCase, RegisterValue> register = new ConcurrentHashMap<>();

    public Register() {
    }

}