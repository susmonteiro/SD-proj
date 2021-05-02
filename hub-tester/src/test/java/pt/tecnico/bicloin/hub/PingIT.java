package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;

public class PingIT extends BaseIT {
    @Test
        public void pingOKTest() {
                PingRequest request = getPingRequest("friend");
                PingResponse response = frontend.ping(request);
		assertEquals("Hello friend!", response.getOutput().substring(0, 13));
        }

    @Test
        public void emptyPingTest() {
                PingRequest request = getPingRequest("");
                assertEquals(
                        INVALID_ARGUMENT.getCode(),
                        assertThrows(StatusRuntimeException.class, () -> frontend.ping(request))
                                .getStatus().getCode()
                );


        }
}