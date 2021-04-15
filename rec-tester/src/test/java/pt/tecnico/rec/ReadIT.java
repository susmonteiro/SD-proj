package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static pt.tecnico.rec.frontend.RecordFrontend.*;

import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.INVALID_ARGUMENT;


public class ReadIT extends BaseIT {

    @Test
    public void readExistingRegister_Balance() {
        RegisterValue balanceRequest = getRegisterBalanceAsRegisterValue();
        RegisterRequest request = getRegisterRequest("alice", balanceRequest);
        
        ReadResponse response = frontend.read(request);
        int value = getBalanceValue(response.getData());

        // Although the user exists, the inital balance is 0
        assertEquals(0, value);
    }

    @Test
    public void readNewRegister_OnBike() {
        RegisterValue onBikeRequest = getRegisterOnBikeAsRegisterValue();
        RegisterRequest request = getRegisterRequest("thisIdSupposedlyDoestExist-ReadIT-readNewRegister_OnBike", onBikeRequest);
        
        ReadResponse response = frontend.read(request);
        boolean value = getOnBikeValue(response.getData());

        // Supposedly a new register is created, and returned a default value
        assertEquals(getOnBikeDefaultValue(), value);
    }
    
    @Test
    public void readNewRegister_RequestFilled() {
        RegisterValue nPickUpsRequest = getRegisterNPickUpsAsRegisterValue(1);
        RegisterRequest request = getRegisterRequest("thisIdSupposedlyDoestExist-ReadIT-readNewRegister_RequestFilled", nPickUpsRequest);
       
        ReadResponse response = frontend.read(request);
        int value = getNPickUpsValue(response.getData());

        // It's supposed to read the value and ignore the filled request
        // but we are using a id that, supposedly, doesn't have this registered
        // in order to compare with a value (the default)
        assertEquals(getNPickUpsDefaultValue(), value);
    }

    @Test
    public void readExisting_EmptyRequestValue() {
        RegisterValue emptyVal = RegisterValue.newBuilder().build();
        RegisterRequest request = getRegisterRequest("alice", emptyVal);
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.read(request));

        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        // TODO assertEquals(new XXX().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void readEmptyRegisterRequest() {
        RegisterRequest request = RegisterRequest.newBuilder().build();
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.read(request));

        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        // TODO assertEquals(new XXX().getMessage(), e.getStatus().getDescription());
    }

}
