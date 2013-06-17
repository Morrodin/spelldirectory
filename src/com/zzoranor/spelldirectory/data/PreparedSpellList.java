package com.zzoranor.spelldirectory.data;

import com.zzoranor.spelldirectory.SpellLabel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable container class for the prepared spell list of a character
 *
 * @author morrodin
 */
public class PreparedSpellList implements Serializable {

    private static final long serialVersionUID = 5632765123L;

    ArrayList<SpellLabel> preparedSpellList;

    public PreparedSpellList(ArrayList<SpellLabel> preparedSpellList) {
        this.preparedSpellList = preparedSpellList;
    }

    public ArrayList<SpellLabel> getPreparedSpellList() {
        return preparedSpellList;
    }

    public void setPreparedSpellList(ArrayList<SpellLabel> preparedSpellList) {
        this.preparedSpellList = preparedSpellList;
    }
}
