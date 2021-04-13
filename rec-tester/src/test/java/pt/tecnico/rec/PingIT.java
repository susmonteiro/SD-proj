package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.Rec.*;


public class PingIT extends BaseIT {
	
	@Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().setInput("friend").build();
		PingResponse response = frontend.ping(request);
		assertEquals("Hello friend! Im Rec 1 at localhost:8091", response.getOutput());
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
