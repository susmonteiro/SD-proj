package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.InvalidUserException;

import pt.tecnico.bicloin.hub.grpc.Hub.*;

import static pt.tecnico.bicloin.hub.frontend.HubFrontend.*;


public class BalanceIT extends BaseIT {
    @Test
    public void balanceSuccessTest() {
        BalanceRequest request = getBalanceRequest("alice");
        AmountResponse response = frontend.balance(request);
        assertEquals(0, response.getBalance());
    }

    @Test
    public void balanceNoSuchUserTest() {
        BalanceRequest request = getBalanceRequest("u");
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.balance(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
    }

    @Test
    public void balanceEmptyUserTest() {
        BalanceRequest request = BalanceRequest.newBuilder().build();
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.balance(request));
        assertEquals(INVALID_ARGUMENT.getCode(), e.getStatus().getCode());
        assertEquals(new InvalidUserException().getMessage(), e.getStatus().getDescription());
    }
}
