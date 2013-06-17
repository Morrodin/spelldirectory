package com.zzoranor.spelldirectory.util;

import android.util.Pair;

import java.io.Serializable;

/**
 * Serializable version of {@link android.util.Pair}
 */
public class SerializablePair<F, S> implements Serializable {

    private static final long serialVersionUID = 78932786123L;

    public final F first;
    public final S second;

    /**
     * Constructor for a Pair. If either are null then equals() and hashCode() will throw
     * a NullPointerException.
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public SerializablePair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Checks the two objects for equality by delegating to their respective equals() methods.
     * @param o the Pair to which this one is to be checked for equality
     * @return true if the underlying objects of the Pair are both considered equals()
     */
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Pair)) return false;
        final SerializablePair<F, S> other;
        try {
            other = (SerializablePair<F, S>) o;
        } catch (ClassCastException e) {
            return false;
        }
        return first.equals(other.first) && second.equals(other.second);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     * @return a hashcode of the Pair
     */
    public int hashCode() {
        int result = 17;
        result = 31 * result + first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     * @param a the first object in the Pair
     * @param b the second object in the pair
     * @return a Pair that is templatized with the types of a and b
     */
    public static <A, B> SerializablePair <A, B> create(A a, B b) {
        return new SerializablePair<A, B>(a, b);
    }
}

