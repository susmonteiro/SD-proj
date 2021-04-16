package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static io.grpc.Status.FAILED_PRECONDITION;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.domain.exception.NoBikeAvailableException;
import pt.tecnico.bicloin.hub.domain.exception.NoDocksAvailableException;
import pt.tecnico.bicloin.hub.domain.exception.UserTooFarAwayFromStationException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

public class HubIT extends BaseIT {
	@Test
	public void checkDeliveriesAndPickUpsFunctions() {
		// get initial info station
		InfoStationResponse info = frontend.doInfoStationOperation("gulb");     

		// get initial balance of 2 users
		int balanceD = frontend.doBalanceOperation("diana").getBalance();
		
		int balanceE = frontend.doBalanceOperation("eva").getBalance();

		// top up 2 users with correct value
		int newBalanceD = frontend.doTopUpOperation("diana", 1, "+34010203").getBalance();
		assertEquals(balanceD + 10, newBalanceD);
		balanceD = newBalanceD;
		
		int newBalanceE = frontend.doTopUpOperation("eva", 2, "+155509080706").getBalance();
		assertEquals(balanceE + 20, newBalanceE);
		balanceE = newBalanceE;

		// diana does a bike up operation
		frontend.doBikeUpOperation("diana", 38.6867f, -9.3124f, "gulb");

		// money was taken from diana's account
		newBalanceD = frontend.doBalanceOperation("diana").getBalance();
		assertEquals(balanceD - 10, newBalanceD);
		balanceD = newBalanceD;

		// check changes in station
		InfoStationResponse newInfo = frontend.doInfoStationOperation("gulb");
		assertEquals(info.getName(), newInfo.getName());
		assertEquals(info.getCoordinates().getLatitude(), newInfo.getCoordinates().getLatitude());
		assertEquals(info.getCoordinates().getLongitude(), newInfo.getCoordinates().getLongitude());
		assertEquals(info.getNDocks(), newInfo.getNDocks());
		assertEquals(info.getReward(), newInfo.getReward());
		assertEquals(info.getNBicycles() - 1, newInfo.getNBicycles());
		//assertEquals(info.getNPickUps() + 1, newInfo.getNPickUps());
		assertEquals(info.getNDeliveries(), newInfo.getNDeliveries());

		info = newInfo;

		// eva does another bike up operation
		frontend.doBikeUpOperation("eva", 38.6867f, -9.3124f, "gulb");

		// money was taken from eva's account
		newBalanceE = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceE - 10, newBalanceE);
		balanceE = newBalanceE;

		// check changes in station
		newInfo = frontend.doInfoStationOperation("gulb");
		assertEquals(info.getName(), newInfo.getName());
		assertEquals(info.getCoordinates().getLatitude(), newInfo.getCoordinates().getLatitude());
		assertEquals(info.getCoordinates().getLongitude(), newInfo.getCoordinates().getLongitude());
		assertEquals(info.getNDocks(), newInfo.getNDocks());
		assertEquals(info.getReward(), newInfo.getReward());
		assertEquals(info.getNBicycles() - 1, newInfo.getNBicycles());
		//assertEquals(info.getNPickUps() + 1, newInfo.getNPickUps());
		assertEquals(info.getNDeliveries(), newInfo.getNDeliveries());

		info = newInfo;

		// get another stations info 
		InfoStationResponse info2 = frontend.doInfoStationOperation("istt");    

		// eva tries to deliver bike in that station but its too far away
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeDownOperation("eva", 8.6267f, -109.3924f, "istt"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new UserTooFarAwayFromStationException().getMessage(), e.getStatus().getDescription());

		// nothing changed in the station
		InfoStationResponse newInfo2 = frontend.doInfoStationOperation("istt");  
		assertEquals(info2.getName(), newInfo2.getName());
		assertEquals(info2.getCoordinates().getLatitude(), newInfo2.getCoordinates().getLatitude());
		assertEquals(info2.getCoordinates().getLongitude(), newInfo2.getCoordinates().getLongitude());
		assertEquals(info2.getNDocks(), newInfo2.getNDocks());
		assertEquals(info2.getReward(), newInfo2.getReward());
		assertEquals(info2.getNBicycles(), newInfo2.getNBicycles());
		assertEquals(info2.getNPickUps(), newInfo2.getNPickUps());
		assertEquals(info2.getNDeliveries(), newInfo2.getNDeliveries());

		info2 = newInfo2;

		// eva tries again and succedes
		frontend.doBikeDownOperation("eva", 38.7372f, -9.3023f, "istt");

		// reward was put in eva's account
		newBalanceE = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceE + 4, newBalanceE);
		balanceE = newBalanceE;

		// check changes in station
		newInfo2 = frontend.doInfoStationOperation("istt");
		assertEquals(info2.getName(), newInfo2.getName());
		assertEquals(info2.getCoordinates().getLatitude(), newInfo2.getCoordinates().getLatitude());
		assertEquals(info2.getCoordinates().getLongitude(), newInfo2.getCoordinates().getLongitude());
		assertEquals(info2.getNDocks(), newInfo2.getNDocks());
		assertEquals(info2.getReward(), newInfo2.getReward());
		assertEquals(info2.getNBicycles() + 1, newInfo2.getNBicycles());
		assertEquals(info2.getNPickUps(), newInfo2.getNPickUps());
		//assertEquals(info2.getNDeliveries() + 1, newInfo2.getNDeliveries());

		info2 = newInfo2;

		// diana delivers the bycicle in the same station
		frontend.doBikeDownOperation("diana", 38.6267f, -9.3924f, "gulb");

		// reward was put in user account
		newBalanceD = frontend.doBalanceOperation("diana").getBalance();
		assertEquals(balanceD + 2, newBalanceD);
		balanceD = newBalanceD;

		// check changes in station
		newInfo = frontend.doInfoStationOperation("gulb");
		assertEquals(info.getName(), newInfo.getName());
		assertEquals(info.getCoordinates().getLatitude(), newInfo.getCoordinates().getLatitude());
		assertEquals(info.getCoordinates().getLongitude(), newInfo.getCoordinates().getLongitude());
		assertEquals(info.getNDocks(), newInfo.getNDocks());
		assertEquals(info.getReward(), newInfo.getReward());
		assertEquals(info.getNBicycles() + 1, newInfo.getNBicycles());
		assertEquals(info.getNPickUps(), newInfo.getNPickUps());
		//assertEquals(info.getNDeliveries() + 1, newInfo.getNDeliveries());
    }

	@Test
	public void checkInfoInEmptyAndFullStationsFunctions() {
		// get initial info station
		InfoStationResponse infoGulb = frontend.doInfoStationOperation("gulb");     
	
		InfoStationResponse infoEmpt = frontend.doInfoStationOperation("empt");     

		InfoStationResponse infoFull = frontend.doInfoStationOperation("full");     

		// get initial balance of 2 users
		int balanceD = frontend.doBalanceOperation("diana").getBalance();
		
		int balanceE = frontend.doBalanceOperation("eva").getBalance();

		// top up 2 users with correct value
		int newBalanceD = frontend.doTopUpOperation("diana", 1, "+34010203").getBalance();
		assertEquals(balanceD + 10, newBalanceD);
		balanceD = newBalanceD;
		
		int newBalanceE = frontend.doTopUpOperation("eva", 2, "+155509080706").getBalance();
		assertEquals(balanceE + 20, newBalanceE);
		balanceE = newBalanceE;


		/* === try a bike up in empty station === */

		// diana tries a bike up operation in empty station
		StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeUpOperation("diana", 38.6867f, -9.3124f, "empt"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NoBikeAvailableException().getMessage(), e.getStatus().getDescription());

		// no money was taken from diana's account
		newBalanceD = frontend.doBalanceOperation("diana").getBalance();
		assertEquals(balanceD, newBalanceD);
		balanceD = newBalanceD;

		// no changes in station
		InfoStationResponse newInfoEmpt = frontend.doInfoStationOperation("empt");
		assertEquals(infoEmpt.getName(), newInfoEmpt.getName());
		assertEquals(infoEmpt.getCoordinates().getLatitude(), newInfoEmpt.getCoordinates().getLatitude());
		assertEquals(infoEmpt.getCoordinates().getLongitude(), newInfoEmpt.getCoordinates().getLongitude());
		assertEquals(infoEmpt.getNDocks(), newInfoEmpt.getNDocks());
		assertEquals(infoEmpt.getReward(), newInfoEmpt.getReward());
		assertEquals(infoEmpt.getNBicycles(), newInfoEmpt.getNBicycles());
		assertEquals(infoEmpt.getNPickUps(), newInfoEmpt.getNPickUps());
		assertEquals(infoEmpt.getNDeliveries(), newInfoEmpt.getNDeliveries());

		infoEmpt = newInfoEmpt;


		/* === valid bike up === */

		// eva does another bike up operation
		frontend.doBikeUpOperation("eva", 38.6867f, -9.3124f, "gulb");

		// money was taken from eva's account
		newBalanceE = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceE - 10, newBalanceE);
		balanceE = newBalanceE;

		// check changes in station
		InfoStationResponse newInfoGulb = frontend.doInfoStationOperation("gulb");
		assertEquals(infoGulb.getName(), newInfoGulb.getName());
		assertEquals(infoGulb.getCoordinates().getLatitude(), newInfoGulb.getCoordinates().getLatitude());
		assertEquals(infoGulb.getCoordinates().getLongitude(), newInfoGulb.getCoordinates().getLongitude());
		assertEquals(infoGulb.getNDocks(), newInfoGulb.getNDocks());
		assertEquals(infoGulb.getReward(), newInfoGulb.getReward());
		assertEquals(infoGulb.getNBicycles() - 1, newInfoGulb.getNBicycles());
		// assertEquals(infoGulb.getNPickUps() + 1, newInfoGulb.getNPickUps());
		assertEquals(infoGulb.getNDeliveries(), newInfoGulb.getNDeliveries());

		infoGulb = newInfoGulb;


		/* === valid bike down in empty station === */

		// eva delivers bike in that station
		frontend.doBikeDownOperation("eva", 38.7372f, -9.3023f, "empt");

		// reward was put in eva's account
		newBalanceE = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceE + 5, newBalanceE);
		balanceE = newBalanceE;

		// check changes in station
		newInfoEmpt = frontend.doInfoStationOperation("empt");
		assertEquals(infoEmpt.getName(), newInfoEmpt.getName());
		assertEquals(infoEmpt.getCoordinates().getLatitude(), newInfoEmpt.getCoordinates().getLatitude());
		assertEquals(infoEmpt.getCoordinates().getLongitude(), newInfoEmpt.getCoordinates().getLongitude());
		assertEquals(infoEmpt.getNDocks(), newInfoEmpt.getNDocks());
		assertEquals(infoEmpt.getReward(), newInfoEmpt.getReward());
		assertEquals(infoEmpt.getNBicycles() + 1, newInfoEmpt.getNBicycles());
		assertEquals(infoEmpt.getNPickUps(), newInfoEmpt.getNPickUps());
		// assertEquals(infoEmpt.getNDeliveries() + 1, newInfoEmpt.getNDeliveries());

		infoEmpt = newInfoEmpt;


		/* === diana can now do a bike up from the empt station === */

		// diana does the bike up operation
		frontend.doBikeUpOperation("diana", 38.6867f, -9.3124f, "empt");

		// money was taken from diana's account
		newBalanceD = frontend.doBalanceOperation("diana").getBalance();
		assertEquals(balanceD - 10, newBalanceD);
		balanceD = newBalanceD;

		// check changes in station
		newInfoEmpt = frontend.doInfoStationOperation("empt");
		assertEquals(infoEmpt.getName(), newInfoEmpt.getName());
		assertEquals(infoEmpt.getCoordinates().getLatitude(), newInfoEmpt.getCoordinates().getLatitude());
		assertEquals(infoEmpt.getCoordinates().getLongitude(), newInfoEmpt.getCoordinates().getLongitude());
		assertEquals(infoEmpt.getNDocks(), newInfoEmpt.getNDocks());
		assertEquals(infoEmpt.getReward(), newInfoEmpt.getReward());
		assertEquals(infoEmpt.getNBicycles() - 1, newInfoEmpt.getNBicycles());
		// assertEquals(infoEmpt.getNPickUps() + 1, newInfoEmpt.getNPickUps());
		assertEquals(infoEmpt.getNDeliveries(), newInfoEmpt.getNDeliveries());


		/* === try a bike down in full station === */

		// diana tries a bike down operation in a full dock
		e = assertThrows(StatusRuntimeException.class, () -> frontend.doBikeDownOperation("diana", 38.6867f, -9.3124f, "full"));
        assertEquals(FAILED_PRECONDITION.getCode(), e.getStatus().getCode());
        assertEquals(new NoDocksAvailableException().getMessage(), e.getStatus().getDescription());

		// no money was rewarded to diana
		newBalanceD = frontend.doBalanceOperation("diana").getBalance();
		assertEquals(balanceD, newBalanceD);
		balanceD = newBalanceD;

		// no changes in station
		InfoStationResponse newInfoFull = frontend.doInfoStationOperation("full");
		assertEquals(infoFull.getName(), newInfoFull.getName());
		assertEquals(infoFull.getCoordinates().getLatitude(), newInfoFull.getCoordinates().getLatitude());
		assertEquals(infoFull.getCoordinates().getLongitude(), newInfoFull.getCoordinates().getLongitude());
		assertEquals(infoFull.getNDocks(), newInfoFull.getNDocks());
		assertEquals(infoFull.getReward(), newInfoFull.getReward());
		assertEquals(infoFull.getNBicycles(), newInfoFull.getNBicycles());
		assertEquals(infoFull.getNPickUps(), newInfoFull.getNPickUps());
		assertEquals(infoFull.getNDeliveries(), newInfoFull.getNDeliveries());

		infoFull = newInfoFull;


		/* === valid bike up from full station === */

		// eva tries to deliver bike in that station but its too far away
		frontend.doBikeUpOperation("eva", 38.7372f, -9.3023f, "full");

		// money was taken from eva's account
		newBalanceE = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceE - 10, newBalanceE);
		balanceE = newBalanceE;

		// check changes in station
		newInfoFull = frontend.doInfoStationOperation("full");
		assertEquals(infoFull.getName(), newInfoFull.getName());
		assertEquals(infoFull.getCoordinates().getLatitude(), newInfoFull.getCoordinates().getLatitude());
		assertEquals(infoFull.getCoordinates().getLongitude(), newInfoFull.getCoordinates().getLongitude());
		assertEquals(infoFull.getNDocks(), newInfoFull.getNDocks());
		assertEquals(infoFull.getReward(), newInfoFull.getReward());
		assertEquals(infoFull.getNBicycles() - 1, newInfoFull.getNBicycles());
		// assertEquals(infoFull.getNPickUps() + 1, newInfoFull.getNPickUps());
		assertEquals(infoFull.getNDeliveries(), newInfoFull.getNDeliveries());

		infoFull = newInfoFull;


		/* === diana can now do a bike down in that station === */

		// diana does the bike up operation
		frontend.doBikeDownOperation("diana", 38.6867f, -9.3124f, "full");

		// diana was rewarded
		newBalanceD = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceD + 5, newBalanceD);
		balanceD = newBalanceD;

		// check changes in station
		newInfoFull = frontend.doInfoStationOperation("full");
		assertEquals(infoFull.getName(), newInfoFull.getName());
		assertEquals(infoFull.getCoordinates().getLatitude(), newInfoFull.getCoordinates().getLatitude());
		assertEquals(infoFull.getCoordinates().getLongitude(), newInfoFull.getCoordinates().getLongitude());
		assertEquals(infoFull.getNDocks(), newInfoFull.getNDocks());
		assertEquals(infoFull.getReward(), newInfoFull.getReward());
		assertEquals(infoFull.getNBicycles() + 1, newInfoFull.getNBicycles());
		// assertEquals(infoFull.getNPickUps() + 1, newInfoFull.getNPickUps());
		assertEquals(infoFull.getNDeliveries(), newInfoFull.getNDeliveries());

		infoFull = newInfoFull;


		/* === finally eva does a valid bike down === */

		// eva does a bike down operation
		frontend.doBikeDownOperation("eva", 38.6867f, -9.3124f, "gulb");

		// money was rewarded to eva
		newBalanceE = frontend.doBalanceOperation("eva").getBalance();
		assertEquals(balanceE + 2, newBalanceE);
		balanceE = newBalanceE;

		// check changes in station
		newInfoGulb = frontend.doInfoStationOperation("gulb");
		assertEquals(infoGulb.getName(), newInfoGulb.getName());
		assertEquals(infoGulb.getCoordinates().getLatitude(), newInfoGulb.getCoordinates().getLatitude());
		assertEquals(infoGulb.getCoordinates().getLongitude(), newInfoGulb.getCoordinates().getLongitude());
		assertEquals(infoGulb.getNDocks(), newInfoGulb.getNDocks());
		assertEquals(infoGulb.getReward(), newInfoGulb.getReward());
		assertEquals(infoGulb.getNBicycles() + 1, newInfoGulb.getNBicycles());
		assertEquals(infoGulb.getNPickUps(), newInfoGulb.getNPickUps());
		// assertEquals(infoGulb.getNDeliveries() + 1, newInfoGulb.getNDeliveries());
    }
		
}