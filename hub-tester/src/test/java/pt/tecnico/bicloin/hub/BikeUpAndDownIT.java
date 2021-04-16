package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.FAILED_PRECONDITION;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.NoBikeAvailableException;
import pt.tecnico.bicloin.hub.domain.exception.NoDocksAvailableException;
import pt.tecnico.bicloin.hub.domain.exception.NotEnoughMoneyException;
import pt.tecnico.bicloin.hub.domain.exception.UserAlreadyOnBikeException;
import pt.tecnico.bicloin.hub.domain.exception.UserNotOnBikeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class BikeUpAndDownIT extends BaseIT {
	@Test
	public void bikeUpAndBikeDownNoMoneyTest() {
		// the user must have money in order to perform a bikeUp operation
		frontend.doTopUpOperation("hel", 1, "+405802912");

		int balance = frontend.doBalanceOperation("hel").getBalance();
		assertEquals(10, balance);

		// takes a bicycle
		frontend.doBikeUpOperation("hel", 38.6867f, -9.3124f, "stao");

		// checks balance
		balance = balance - 10;
		AmountResponse response = frontend.doBalanceOperation("hel");
		assertEquals(balance, response.getBalance());

		// puts the bicycle in other station
		frontend.doBikeDownOperation("hel", 38.7372f, -9.3023f, "istt");

		// checks reward was added to user account
		balance = balance + 4;
		response = frontend.doBalanceOperation("hel");
		assertEquals(balance, response.getBalance());

		// tries another bike up, but has no money
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("hel", 38.6867f, -9.3124f, "stao"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NotEnoughMoneyException().getMessage(), e.getStatus().getDescription());
	}

	@Test
	public void bikeUpAndBikeDownBicyclesChecksTest() {
		// the user must have money in order to perform a bikeUp operation
		frontend.doTopUpOperation("gui", 1, "+25840482");

		// tries bike up in empty station
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("gui", 38.6867f, -9.3124f, "empt"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NoBikeAvailableException().getMessage(), e.getStatus().getDescription());

		// takes a bicycle
		frontend.doBikeUpOperation("gui", 38.6867f, -9.3124f, "stao");

		// tries to take another bicycle
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("gui", 38.6867f, -9.3124f, "stao"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserAlreadyOnBikeException().getMessage(), e.getStatus().getDescription());

		// tries to leave bicycle on full station
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeDownOperation("gui", 38.6867f, -9.3124f, "full"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NoDocksAvailableException().getMessage(), e.getStatus().getDescription());

		// leaves bicycle on another station
		frontend.doBikeDownOperation("gui", 38.6867f, -9.3124f, "stao");

		// tries to bike down a bicycle again
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeDownOperation("gui", 38.6867f, -9.3124f, "stao"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserNotOnBikeException().getMessage(), e.getStatus().getDescription());
	}

}
