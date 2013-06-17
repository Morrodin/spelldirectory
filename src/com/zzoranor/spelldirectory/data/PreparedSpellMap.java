package com.zzoranor.spelldirectory.data;

import android.util.Pair;
import com.zzoranor.spelldirectory.SpellLabel;
import com.zzoranor.spelldirectory.util.SerializablePair;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Serializable container class for holding a character's prepared spell map.
 *
 * @author morridin
 */
public class PreparedSpellMap implements Serializable {

    private static final long serialVersionUID = 16782678L;
    private HashMap<String, SerializablePair<Integer, Integer>> preparedSpellMap;

    public PreparedSpellMap(HashMap<String, SerializablePair<Integer, Integer>> preparedSpellMap) {
        this.preparedSpellMap = preparedSpellMap;
    }

    public HashMap<String, SerializablePair<Integer, Integer>> getPreparedSpellMap() {
         return preparedSpellMap;
    }

    public void setPreparedSpellMap(HashMap<String, SerializablePair<Integer, Integer>> preparedSpellMap) {
        this.preparedSpellMap = preparedSpellMap;
    }
}
