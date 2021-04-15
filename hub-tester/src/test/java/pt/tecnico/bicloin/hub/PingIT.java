package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class PingIT extends BaseIT {
    @Test
        public void pingOKTest() {
                PingRequest request = PingRequest.newBuilder().setInput("friend").build();
                PingResponse response = frontend.ping(request);
                assertEquals("Hello friend! Im Hub 1 at localhost:8081", response.getOutput());
        }

    @Test
        public void emptyPingTest() {
                PingRequest request = PingRequest.newBuilder().setInput("").build();
                assertEquals(
                        INVALID_ARGUMENT.getCode(),
                        assertThrows(StatusRuntimeException.class, () -> frontend.ping(request))
                                .getStatus().getCode()
                );


        }
}