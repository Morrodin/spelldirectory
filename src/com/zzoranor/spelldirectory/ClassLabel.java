package com.zzoranor.spelldirectory;

public class ClassLabel {
	private int id;
	private String name;
	
	public ClassLabel(){
		
	}
	
	public ClassLabel(int _id, String _name){
		id = _id;
		name = _name;
	}

	public int getId(){
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String _name){
		name = _name;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == this)
			return true;
		
		if(o == null || o.getClass() != this.getClass())
			return false;
		
		ClassLabel ocl = (ClassLabel) o;
		
		return (id == ocl.id);
	}
	
}
