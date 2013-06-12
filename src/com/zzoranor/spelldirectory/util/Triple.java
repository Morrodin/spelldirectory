package com.zzoranor.spelldirectory.util;

public class Triple {
	public static final String EQ = "=";
	public static final String LESS_THAN = "<";
	public static final String GREATER_THAN = ">";
	public static final String NOT = "!=";
	
	public String field;
	public String operand;
	public String value;
	
	public Triple(String field, String operand, String value)
	{
		this.field = field;
		this.operand = operand;
		this.value = value;
	}
	
	public Triple(String field, String operand, int value)
	{
		this.field = field;
		this.operand = operand;
		this.value = "" + value;
	}
	
	public Triple(Triple orig)
	{
		field = orig.field;
		operand = orig.operand;
		value = orig.value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((operand == null) ? 0 : operand.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Triple other = (Triple) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (operand == null) {
			if (other.operand != null)
				return false;
		} else if (!operand.equals(other.operand))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return field + " " + operand + " " + value;
	}
	
	
}
