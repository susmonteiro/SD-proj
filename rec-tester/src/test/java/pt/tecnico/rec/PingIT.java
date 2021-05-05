package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import pt.tecnico.rec.grpc.Rec.*;
import pt.tecnico.rec.frontend.RecordFrontendReplicationWrapper;

public class PingIT extends BaseIT {
	
	@Test
	public void pingOKTest() throws ZKNamingException{
		String response = frontend.getPing("friend", instance_num);
		assertEquals("Hello friend!", response.substring(0, 13));
	}

	@Test
	public void emptyPingTest() {
		assertEquals(
			INVALID_ARGUMENT.getCode(),
			assertThrows(StatusRuntimeException.class, () -> frontend.getPing("", instance_num))
				.getStatus().getCode()
		);	
	}

}
