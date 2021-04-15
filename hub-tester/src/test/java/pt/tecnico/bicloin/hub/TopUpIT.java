package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class TopUpIT extends BaseIT {
	@Test
	public void topUpSuccessTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("bruno")
				.setAmount(10)
				.setPhoneNumber("+35193334444")
				.build();
		AmountResponse response = frontend.topUp(request);
		assertEquals(100, response.getBalance());

		response = frontend.topUp(request);
		assertEquals(200, response.getBalance());
	}

	@Test
	public void topUpInvalidUserTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("u")
				.setAmount(10)
				.setPhoneNumber("+35193334444")
				.build();
		assertEquals(
			INVALID_ARGUMENT.getCode(),
			assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
			.getStatus().getCode()
		);
	}

	@Test
	public void topUpInvalidPhoneNumberTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("bruno")
				.setAmount(10)
				.setPhoneNumber("+3519333444")
				.build();
		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void topUpTooHighValueTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("bruno")
				.setAmount(100)
				.setPhoneNumber("+35193334444")
				.build();
		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void topUpTooLowValueTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("bruno")
				.setAmount(0)
				.setPhoneNumber("+35193334444")
				.build();
		assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
            .getStatus().getCode()
        );
	}

	@Test
	public void topUpEmptyAmountTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("bruno")
				.setPhoneNumber("+3519333444")
				.build();
		assertEquals(
			INVALID_ARGUMENT.getCode(),
			assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
			.getStatus().getCode()
		);
	}

	@Test
	public void topUpEmptyPhoneNumberTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("bruno")
				.setAmount(10)
				.build();
		assertEquals(
			INVALID_ARGUMENT.getCode(),
			assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
			.getStatus().getCode()
		);
	}

}
