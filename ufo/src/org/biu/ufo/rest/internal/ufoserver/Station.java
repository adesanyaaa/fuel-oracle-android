package org.biu.ufo.rest.internal.ufoserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Station {
	
	public long id;
	public String company;
	public double latitude;
	public double longitude;
	public String address;
	public int company_id;
	
	@JsonProperty("name")
	public String station;

	@JsonProperty("price")
	public float cost;

	
	public enum CompanyName {
		
		DEFAULT("אחר"), DELEK("דלק"), PAZ("פז"), TEN("Ten"), SADASH("סדש"), DORALON("דור-אלון"), SONOL("סונול");
		
		private CompanyName(final String name) {
			this.companyName = name;
		}
		
		private String companyName;
	};

	
	public static String getEnglishCompanyName(String companyName){
		
		for(CompanyName v : CompanyName.values()){
	        if (v.companyName.equals(companyName)){
	        	return v.name().toLowerCase();
	        }
	    }
		
		return CompanyName.DEFAULT.name().toLowerCase();

	}
}
