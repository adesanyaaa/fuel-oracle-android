package org.biu.ufo.ui.utils;

import java.lang.reflect.Field;

import org.biu.ufo.R;
import org.biu.ufo.rest.Station.CapacityUnit;
import org.biu.ufo.rest.Station.DistanceUnit;
import org.biu.ufo.rest.Station.PriceCurrency;

public class UnitConverter {
	
	public static final int AVERAGE_GAS_TANK_SIZE_US = 16;
	public static final int AVERAGE_GAS_TANK__SIZE_UK = 13;
	public static final int AVERAGE_GAS_TANK_SIZE_LITER = 55;
	
    public static int getResourceForPriceCurrency(PriceCurrency priceCurrency) {
        switch(priceCurrency){
		case CENTS:
			return R.string.currency_cent;
		case DOLLARS:
			return R.string.currency_dollar;
		case NIS:
			return R.string.currency_nis;
        }
        return R.string.currency_dollar;
    }

    public static int getResourceForDistanceUnit(DistanceUnit distanceUnit) {
        switch(distanceUnit){
		case KM:
			return R.string.measurement_km;
		case MILES:
			return R.string.measurement_mile;
        }
        return R.string.measurement_mile;
    }

    public static int getResourceForCapacityUnit(CapacityUnit capacityUnit) {
        switch(capacityUnit){
		case LITTERS:
			return R.string.measurement_liter;
		case UK_GALONS:
			return R.string.measurement_uk_gal;
		case US_GALONS:
			return R.string.measurement_us_gal;
        }
        return R.string.measurement_us_gal;
    }

    public static int getAverageGasTankSize(CapacityUnit capacityUnit){
    	 switch(capacityUnit){
 		case LITTERS:
 			return AVERAGE_GAS_TANK_SIZE_LITER;
 		case UK_GALONS:
 			return AVERAGE_GAS_TANK__SIZE_UK;
 		case US_GALONS:
 			return AVERAGE_GAS_TANK_SIZE_US;
         }
    	return AVERAGE_GAS_TANK_SIZE_US;
    }
    
	public static int getResourceForStationLogo(String companyName) {
	    try {
			companyName = companyName.toLowerCase().trim();
	        Field idField = R.drawable.class.getDeclaredField("logo_"+companyName);
	        return idField.getInt(idField);
	    } catch (Exception e) {
	       return R.drawable.logo_default;
	    }
	}

}
