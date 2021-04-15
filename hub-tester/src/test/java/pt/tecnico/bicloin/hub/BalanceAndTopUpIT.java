package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidTopUpAmountException;

import pt.tecnico.bicloin.hub.grpc.Hub.*;


import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BalanceAndTopUpIT extends BaseIT {
	@Test
	public void topUpSuccessfulTest() {
        // initially balance is 0
        AmountResponse balance = frontend.doBalanceOperation("bruno");
        assertEquals(0, balance.getBalance());

        // top up with 10 euros
        AmountResponse topUp = frontend.doTopUpOperation("bruno", 10, "+35193334444");
		assertEquals(100, topUp.getBalance());

        // check that the new balance was written in rec (same request)
        balance = frontend.doBalanceOperation("bruno");
        assertEquals(100, balance.getBalance());

        // tries to top up with invalid value
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doTopUpOperation("bruno", 21, "+35193334444"));
		assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
		assertEquals(new InvalidTopUpAmountException().getMessage(), e.getStatus().getDescription());

        // check that the balance was not updated
        balance = frontend.doBalanceOperation("bruno");
        assertEquals(100, balance.getBalance());

        // top up with 20 euros
        topUp = frontend.doTopUpOperation("bruno", 20, "+35193334444");
		assertEquals(300, topUp.getBalance());

        // check that the balance was updated correctly
        balance = frontend.doBalanceOperation("bruno");
        assertEquals(300, balance.getBalance());
	}
}