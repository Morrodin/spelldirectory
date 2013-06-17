package com.zzoranor.spelldirectory.data;

import android.util.Pair;

import java.io.Serializable;

/**
 * Serializable container for Pair
 *
 * @author morrodin
 */
public class SpellPair implements Serializable {

       private Pair<Integer, Integer> pair;

    public SpellPair(Pair<Integer, Integer> pair) {
        this.pair = pair;
    }

    public Pair<Integer, Integer> getPair() {
        return pair;
    }

    public void setPair(Pair<Integer, Integer> pair) {
        this.pair = pair;
    }
}
