package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class BalanceIT extends BaseIT {
    @Test
    public void balanceSuccessTest() {
        BalanceRequest request = BalanceRequest.newBuilder().setUserId("alice").build();
        AmountResponse response = frontend.balance(request);
        assertEquals(0, response.getBalance());
    }

    @Test
    public void balanceNoSuchUserTest() {
        BalanceRequest request = BalanceRequest.newBuilder().setUserId("f").build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.balance(request))
            .getStatus().getCode()
        );
    }

    @Test
    public void balanceEmptyUserTest() {
        BalanceRequest request = BalanceRequest.newBuilder().build();
        assertEquals(
            INVALID_ARGUMENT.getCode(),
            assertThrows(StatusRuntimeException.class, () -> frontend.balance(request))
            .getStatus().getCode()
        );
    }
}
