package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidUserException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidPhoneNumberException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidTopUpAmountException;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class TopUpIT extends BaseIT {
	@Test
	public void topUpInvalidUserTest() {
		TopUpRequest request = getTopUpRequest("u", 10, "+34203040");
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void topUpInvalidPhoneNumberTest() {
		TopUpRequest request = getTopUpRequest("carlos", 10, "+3519333444");
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidPhoneNumberException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void topUpTooHighValueTest() {
		TopUpRequest request = getTopUpRequest("carlos", 21, "+34203040");
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidTopUpAmountException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void topUpTooLowValueTest() {
		TopUpRequest request = getTopUpRequest("carlos", 0, "+34203040");
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidTopUpAmountException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void topUpEmptyAmountTest() {
		TopUpRequest request = TopUpRequest.newBuilder()
				.setUserId("carlos")
				.setPhoneNumber("+34203040")
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
				.setUserId("carlos")
				.setAmount(10)
				.build();
		assertEquals(
			INVALID_ARGUMENT.getCode(),
			assertThrows(StatusRuntimeException.class, () -> frontend.topUp(request))
			.getStatus().getCode()
		);
	}

}
