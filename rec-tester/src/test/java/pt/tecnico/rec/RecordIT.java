package pt.tecnico.rec;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


import static pt.tecnico.rec.frontend.RecordFrontend.*;

public class RecordIT extends BaseIT {
	
	@Test
	public void write_read_sameRegister_balance() {
		int value = 1;
		frontend.setBalance("RecordIT-write_read_sameRegister_balance", value);
		int updatedVal = frontend.getBalance("RecordIT-write_read_sameRegister_balance");
        
		assertEquals(value, updatedVal);
    }

	@Test
	public void read_write_read_sameRegister_onBike() {
		boolean oldVal = frontend.getOnBike("RecordIT-read_write_read_sameRegister_onBike");
		
		boolean value = !oldVal;
		frontend.setOnBike("RecordIT-read_write_read_sameRegister_onBike", value);
		
		boolean updatedVal = frontend.getOnBike("RecordIT-read_write_read_sameRegister_onBike");

		assertNotEquals(oldVal, updatedVal);
    }

	@Test
	public void repeatedRead_sameRegister_nBikes() {
		int oldVal = frontend.getBalance("RecordIT-repeatedRead_sameRegister_nBikes");
		int newVal = frontend.getBalance("RecordIT-repeatedRead_sameRegister_nBikes");
        
		assertEquals(oldVal, newVal);
    }

	@Test
	public void repeatedRead_sameRegisterDifferentValue() {
		int nPickUps = frontend.getNPickUps("RecordIT-repeatedRead_sameRegisterDifferentValue");
		int nDeliveries = frontend.getNDeliveries("RecordIT-repeatedRead_sameRegisterDifferentValue");
        
		// Because a new register is created, both values should be default
		assertEquals(getNPickUpsDefaultValue(), nPickUps);
		assertEquals(getNDeliveriesDefaultValue(), nDeliveries);
    }

}
