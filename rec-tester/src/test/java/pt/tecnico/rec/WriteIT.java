package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static pt.tecnico.rec.frontend.RecordFrontend.*;

import pt.tecnico.rec.domain.exception.*;
import pt.tecnico.rec.grpc.Rec.*;
import io.grpc.StatusRuntimeException;
import static io.grpc.Status.INVALID_ARGUMENT;

public class WriteIT extends BaseIT {
    /* Not checking stored value, atomic method tester
    *   refer to RecordIT for complex tests */
    
    @Test
    public void writeExistingRegister_NBikes() {
        int newValue = 1;
        RegisterValue nBikesRequest = getRegisterNBikesAsRegisterValue(newValue);
        RegisterRequest request = getRegisterRequest("ista", nBikesRequest);
        
        ReadResponse response = frontend.read(request);
        getNBikesValue(response.getData());
    }

    @Test
    public void writeNewRegister_NDeliveries() {
        int valueToWrite = 1;
        RegisterValue nDeliveriesRequest = getRegisterNDeliveriesAsRegisterValue(valueToWrite);
        RegisterRequest request = getRegisterRequest("thisIdSupposedlyDoestExist-WriteIT-writeNew_NDeliveries", nDeliveriesRequest);
        
        ReadResponse response = frontend.read(request);
        getNDeliveriesValue(response.getData());
    }

    @Test
    public void writeExistingRegister_EmptyRequestValueExistingId() {
        RegisterValue emptyVal = RegisterValue.newBuilder().build();
        RegisterRequest request = getRegisterRequest("alice", emptyVal);
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.write(request));

        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new NoRegisterValueSetException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void writeNewRegister_EmptyRequestValueNotExistingId() {
        RegisterValue emptyVal = RegisterValue.newBuilder().build();
        RegisterRequest request = getRegisterRequest("thisIdSupposedlyDoestExist-WriteIT-writeNewRegister_EmptyRequestValueNotExistingId", emptyVal);
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.write(request));

        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new NoRegisterValueSetException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void writeEmptyRegisterRequest() {
        RegisterRequest request = RegisterRequest.newBuilder().build();
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.write(request));

        // Id should be tested first
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidIdException().getMessage(), e.getStatus().getDescription());
    }

}
