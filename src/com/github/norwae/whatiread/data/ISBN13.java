package com.github.norwae.whatiread.data;

import java.util.regex.Pattern;

public final class ISBN13 {
	private static Pattern ISBN13_PATTERN = Pattern.compile("97[89]\\d{10}");
	private static Pattern ISBN10_PATTERN = Pattern.compile("\\d{9}[0-9Xx]");

	public static ISBN13 parse(String string) {
		if (string != null) {
			if (ISBN10_PATTERN.matcher(string).matches()) {
				String code = string.substring(0, 9);
				string = "978" + code;
				string = string + isbn13CheckDigit(string);
			}
			
			if (ISBN13_PATTERN.matcher(string).matches()) {
				if (string.endsWith(isbn13CheckDigit(string.substring(0, 12)))) {
					return new ISBN13(string);
				}
			}
		}

		return null;
	}

	private static String isbn13CheckDigit(String string) {
		char[] chars = string.toCharArray();
		int check = 0;
		for (int i = 0; i < 12; i++) {
			int factor = (i % 2) == 0 ? 1 : 3;
			check += factor * (chars[i] - '0');
		}

		int checkValue = (10 - (check % 10)) % 10;
		return "" + checkValue;
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
