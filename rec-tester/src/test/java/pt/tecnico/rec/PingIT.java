package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.Rec.*;

import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper;


public class PingIT extends BaseIT {
	
	@Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().setInput("friend").build();
		List<PingResponse> responses = frontend.pingReplicated(request);
		for (PingResponse r : responses)
			assertEquals("Hello friend!", r.getOutput().substring(0, 13));
	}

	@Test
	public void emptyPingTest() {
		PingRequest request = PingRequest.newBuilder().setInput("").build();
		assertEquals(
			INVALID_ARGUMENT.getCode(),
			assertThrows(StatusRuntimeException.class, () -> frontend.pingReplicated(request))
				.getStatus().getCode()
		);	
	}

}
