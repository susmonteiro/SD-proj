package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper.*;

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
        frontend.setNBikes("ista", newValue);
    }

    @Test
    public void writeNewRegister_NDeliveries() {
        int valueToWrite = 1;
        frontend.setNDeliveries("thisIdSupposedlyDoestExist-WriteIT-writeNew_NDeliveries", valueToWrite);
    }
    
    @Test
    public void writeExistingRegister_EmptyRequestValueExistingId() {
        RegisterValue emptyVal = RegisterValue.newBuilder().build();
        RegisterTag emptyTag = RegisterTag.newBuilder().build();
        RegisterRequest request = getRegisterRequest("alice", emptyVal, emptyTag);
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.writeReplicated(request));

        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        // assertEquals(new NoRegisterValueSetException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void writeNewRegister_EmptyRequestValueNotExistingId() {
        RegisterValue emptyVal = RegisterValue.newBuilder().build();
        RegisterTag emptyTag = RegisterTag.newBuilder().build();
        RegisterRequest request = getRegisterRequest("thisIdSupposedlyDoestExist-WriteIT-writeNewRegister_EmptyRequestValueNotExistingId", emptyVal, emptyTag);
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.writeReplicated(request));

        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new NoRegisterValueSetException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void writeEmptyRegisterRequest() {
        RegisterRequest request = RegisterRequest.newBuilder().build();
       
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.writeReplicated(request));

        // Id should be tested first
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidIdException().getMessage(), e.getStatus().getDescription());
    }

}
