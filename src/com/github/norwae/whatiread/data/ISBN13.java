package com.github.norwae.whatiread.data;

import java.util.regex.Pattern;

public final class ISBN13 {
	private static Pattern ISBN_PATTERN = Pattern.compile("97[89]\\d{10}");
	public static ISBN13 parse(String string) {
		if (string != null) {
			string = string.replaceAll("[ \t-_]+", "");
			
			if (ISBN_PATTERN.matcher(string).matches()){
				return new ISBN13(string);
			}
		}
		
		return null;
	}

	private String value;
	
	private ISBN13(String val) {
		value = val;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (o instanceof ISBN13) {
			return ((ISBN13) o).value.equals(value);
		}
		
		return false;
	}
}
