package com.zzoranor.spelldirectory;

public class CharacterLabel {
	private int id;
	private String name;
	
	public CharacterLabel(){
		
	}
	
	public CharacterLabel(int _id, String _name){
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
		
		CharacterLabel ocl = (CharacterLabel) o;
		
		return (id == ocl.id);
	}
	
}
