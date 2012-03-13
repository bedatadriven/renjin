package org.renjin.primitives.graphics;

public class ColorDataBaseEntry {

	String name;
	String rgb;
	int code;
	
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getRgb() {
		return rgb;
	}


	public void setRgb(String rgb) {
		this.rgb = rgb;
	}


	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	public ColorDataBaseEntry(String name, String rgb, int code){
		this.name = name;
		this.rgb = rgb;
		this.code = code;
	}
	
}
