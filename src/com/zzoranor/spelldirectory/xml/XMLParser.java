package com.zzoranor.spelldirectory.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;


public class XMLParser {

	DocumentBuilderFactory builderFactory;
	DocumentBuilder builder;
	Document doc;
	Element rootElement;
	
	
	public XMLParser() {
		builderFactory = DocumentBuilderFactory.newInstance();
		builder = null;
		try {
			builder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void parseFile(File file) {

		try {
			doc = builder.parse(file);
			rootElement = doc.getDocumentElement();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<XMLCharacterData> getCharacterData() {
		NodeList nodes = rootElement.getChildNodes();

		ArrayList<XMLCharacterData> data = new ArrayList<XMLCharacterData>();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			
			if (node instanceof Element) {
				
				XMLCharacterData d = new XMLCharacterData();
				
				Element character = (Element) node;

				NodeList list = character.getChildNodes();

				for (int j = 0; j < list.getLength(); j++) {

					Node chNode = list.item(j);

					if (chNode instanceof Element) {
						Element chElem = (Element) chNode;
						//Log.d("XMLParser", "chElem = " + chElem.getNodeName());
						
						NodeList child_list = chElem.getChildNodes();
						
						// Extract Character Data.
						if (chElem.getNodeName().equals("char_data")) {
							
							d.char_id = Integer.parseInt(chElem.getAttribute("char_id"));
							d.class_id = Integer.parseInt(chElem.getAttribute("class_id"));
							d.char_name = chElem.getAttribute("char_name");
							d.chosen_class = chElem.getAttribute("chosen_class");
						}

						// Extract Prepared Spells.
						if (chElem.getNodeName().equals("prepared_spells")) {

							int numSpells = 0; 
							// Count number of Spells. 
							for (int k = 0; k < child_list.getLength(); k++) {
								Node nSpell = child_list.item(k);
								if (nSpell instanceof Element) {
									Element eSpellElem = (Element) nSpell;
									if(eSpellElem.getNodeName().equals("spell"))
									{
										numSpells++;
									}
									
								}
							}
							
							
							// We now have the number of spells prepared. Create array.
							
							d.preparedSpells = new XMLSpellData[numSpells];
							
							int idx = 0;
							
							// Now, create SpellData objects and place in array. 
							for (int k = 0; k < child_list.getLength(); k++) {
								Node nSpell = child_list.item(k);
								if (nSpell instanceof Element) {
									Element eSpellElem = (Element) nSpell;
									if(eSpellElem.getNodeName().equals("spell"))
									{
										XMLSpellData sp = new XMLSpellData();
										sp.char_id = Integer.parseInt(eSpellElem.getAttribute("char_id"));
										sp.spell_id = Integer.parseInt(eSpellElem.getAttribute("spell_id"));
										sp.spell_name = eSpellElem.getAttribute("spell_name");
										sp.class_id = Integer.parseInt(eSpellElem.getAttribute("class_id"));
										sp.spell_known = Integer.parseInt(eSpellElem.getAttribute("spell_known"));
										sp.spell_prepared = Integer.parseInt(eSpellElem.getAttribute("spell_prepared"));
										sp.spell_prepared_lvl = Integer.parseInt(eSpellElem.getAttribute("spell_prepared_lvl"));
										sp.spell_used = Integer.parseInt(eSpellElem.getAttribute("spell_used"));
										d.preparedSpells[idx++] = sp;
									}
									
								}
							}							
						}						
											
						// Extract Known Spells.
						if (chElem.getNodeName().equals("known_spells")) {

							int numSpells = 0; 
							// Count number of Spells. 
							for (int k = 0; k < child_list.getLength(); k++) {
								Node nSpell = child_list.item(k);
								if (nSpell instanceof Element) {
									Element eSpellElem = (Element) nSpell;
									if(eSpellElem.getNodeName().equals("kspell"))
									{
										numSpells++;
									}
									
								}
							}
							
							
							// We now have the number of spells prepared. Create array.
							
							d.knownSpells = new XMLSpellKnownData[numSpells];
							
							int idx = 0;
							
							// Now, create XMLSpellKnownData objects and place in array. 
							for (int k = 0; k < child_list.getLength(); k++) {
								Node nSpell = child_list.item(k);
								if (nSpell instanceof Element) {
									Element eSpellElem = (Element) nSpell;
									if(eSpellElem.getNodeName().equals("kspell"))
									{
										XMLSpellKnownData sp = new XMLSpellKnownData();
										sp.char_id = Integer.parseInt(eSpellElem.getAttribute("char_id"));
										sp.spell_id = Integer.parseInt(eSpellElem.getAttribute("spell_id"));
										sp.spell_lvl = Integer.parseInt(eSpellElem.getAttribute("spell_lvl"));
										sp.known_type = Integer.parseInt(eSpellElem.getAttribute("known_type"));
										d.knownSpells[idx++] = sp;
									}
									
								}
							}
						}
											
					}						

				}
				
				// Add Data to List. 
				data.add(d);
			}
		}

		return data;
	}
}