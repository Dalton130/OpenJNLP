/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/ 
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 */
package org.nanode.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Provides a formal means for encapsulating a version-id.
 * The version-id is parsed according to the following syntax:
 * <blockquote><code>
 * version-id ::= string ( separator string ) *<br />
 * string     ::= char ( char ) *<br />
 * char       ::= any character except a space, a separator or a modifier<br />
 * separator  ::= "." | "-" | "_"<br />
 * modifier   ::= "+" | "*"
 * </code></blockquote>
 * The "+" modifier indicates a greater-than-or-equal match and a "*" modifier indicates a prefix match.
 * <p>
 * A version-id can be described as a tuple of values. It is reduced to a list of parts between
 * separators ('.', '-' or '_'), known as elements. For example, "1.3.0-rc2_001" becomes (1,3,0,rc2,001).
 *
 * @author   Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class Version {
    /** a version-id with no value */
    public static final Version EMPTY_VERSION = new Version("");

    /** no-modifier modifier */
    public static final char MODIFIER_NONE = ' ';

    /** greater-than-or-equal modifier */
    public static final char MODIFIER_PLUS = '+';

    /** prefix match modifier */
    public static final char MODIFIER_SPLAT = '*';


    /** string representation of this version-id, including modifier if present */
    protected String version;

    /** prefix portion of the version-id which is everything except the modifier */
    protected String prefix;

    /** the modifier for this version-id */
    protected char modifier;

    /** a list expressing the version-id as a tuple of the prefix */
    protected List tuple;

    /** the computed hash for this version-id, based on the tuple */
    private int hash;


    /**
     * Constructs a version object based on the suplied version-id string.
     *
     * @param ver   the source string for the version-id
     */
    public Version(String ver) {
        if (ver == null) {
            throw new IllegalArgumentException("null string for version");
        }

        // end of version id is first space or end of string
        int end = ver.indexOf(' ');

        if (end == -1) {
            end = ver.length();
        }

        // determine modifier if present
        if (end > 0 && (ver.charAt(end - 1) == MODIFIER_PLUS || ver.charAt(end - 1) == MODIFIER_SPLAT)) {
            prefix = ver.substring(0, end - 1);
            modifier = ver.charAt(end - 1);
        } else {
            prefix = ver.substring(0, end);
            modifier = MODIFIER_NONE;
        }

        version = ver.substring(0, end);	// the whole version-id including modifier as string

        // now break out each tuple
        StringTokenizer st = new StringTokenizer((modifier != MODIFIER_NONE) ? version.substring(0, version.length() - 1) : version, ".-_");
        tuple = new ArrayList();
        hash = 0;							// initial value of hash

        while (st.hasMoreTokens()) {
            String s = st.nextToken();

            tuple.add(s);
            hash ^= s.hashCode();
        }

        tuple = Collections.unmodifiableList(tuple);
    }

    /**
     * Compares this version-id to another version-id. Any modifiers are taken into consideration
     * when doing the comparison.
     *
     * @param  ver   the version-id to be compared
     * @return the value 0 if the argument version-id is equal to this string; a value less than 0
     *         if this version-id is less than the argument version-id; and a value greater than 0
     *         if this version-id is greater than the argument version-id.
     */
    public int compareTo(Version ver) {
        int result = 0;

        // if the ver is null, this is greater
        if (ver == null) {
            return 1;
        }

        // determine the size of normalized tuples, taking into account "*" modifiers
        int len = Math.max(getTuple().size(), ver.getTuple().size());

        if (getModifier() == MODIFIER_SPLAT && len > getTuple().size()) {
            len = getTuple().size();
        }

        if (ver.getModifier() == MODIFIER_SPLAT && len > ver.getTuple().size()) {
            len = ver.getTuple().size();
        }

        // normalize tuples and compare, taking into account prefix matching ('*' modifier)
        List a = normalizedTuple(getTuple(), len);
        List b = normalizedTuple(ver.getTuple(), len);

        result = compareTuples(a, b);

        // check for greater-than-or-equal ("+" modifier) match conditions
        if (result < 0 && getModifier() == MODIFIER_PLUS) {
            result = 0;
        }

        if (result > 0 && ver.getModifier() == MODIFIER_PLUS) {
            result = 0;
        }

        return result;
    }

    /**
     * Returns a string representation of this version-id.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return version;
    }

    /**
     * Returns the modifier of this version-id. No modifier is indicated by <code>MODIFIER_NONE</code>.
     *
     * @return the modifier character of this version-id
     */
    public char getModifier() {
        return modifier;
    }

    /**
     * Returns the prefix portion of this version-id. The prefix portion is the version-id before the modifier.
     *
     * @return the prefix portion of this version-id
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns an integer suitable for hash table indexing.
     *
     * @return  a hash code for this version-id
     */
    public int hashCode() {
        return hash;
    }

    /**
     * Compares this version-id to another object. The two are equal if and only if the object is not null
     * and is a <code>Version</code> object and they are logically equivalent.
     *
     * @param  obj   the object to compare this version-id to
     * @return <code>true</code> if the object is a version-id that is logically equivalent to this version-id;
     *         <code>false</code> otherwise;
     * @see    #compareTo(Version)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof Version)) {
            return false;
        }

        return (compareTo((Version) obj) == 0);
    }

    /**
     * Returns the tuple of this version-id as a list of element strings. The list is constructed with
     * the left-most element being first in the list and so on. The modifier is not included.
     *
     * @return the tuple as a list
     */
    protected List getTuple() {
        return tuple;
    }


    /**
     * Compares two tuples for equality. It is assumed the tuples have been normalized. The comparison is
     * made by comparing the elements in the first tuple with the same positional element in the second
     * tuple. Each pair of elements are compared numerically if they can both be parsed as java
     * <code>ints</code>, otherwise they are compared lexicographically.
     * <p>
     * Each tuple is treated like a Lisp list, where the tuple can be expressed as <em>(Head Tail)</em>.
     * <em>Head</em> represents the first element of the tuple and <em>Tail</em> represents the rest of
     * the tuple.
     * <p>
     * Given two tuples (HA TA) and (HB TB), then (HA TA) is greater than (HB TB) if and only if:
     * <ul>
     * <li>HA is greater than HB
     * <li>HA and HB are equal, TA and TB are not empty and TA is greater than TB recursively.
     * </ul>
     *
     * @param  a  first tuple
     * @param  b  second tuple
     * @return the value 0 if tuple a and tuple b are equal; a value less than 0 if tuple a is less than
     *         tuple b; and a value greater than 0 if tuple a is greater than tuple b.
     */
     protected static int compareTuples(List a, List b) {
        int result = 0;

        try {
            int ha = Integer.parseInt((String) a.get(0));
            int hb = Integer.parseInt((String) b.get(0));

            result = ha - hb;
        } catch (NumberFormatException e) {
            result = ((String) a.get(0)).compareTo((String) b.get(0));
        } catch (ArrayIndexOutOfBoundsException e) {
            result = a.size() - b.size();	// this is worst-case condition of empty list(s)
        }

        // if heads match and tails are not empty, recursively compare
        if (result == 0 && a.size() > 1 && b.size() > 1) {
            result = compareTuples(a.subList(1, a.size()), b.subList(1, b.size()));
        }

        return result;
    }

    /**
     * Returns a normalized copy of a tuple of the desired length. Normalization is done by appending the
     * element "0" to the new tuple as many times as necessary to get the new tuple to the correct size.
     * If the original tuple has a length greater than desired the new tuple will be a truncated copy of
     * the original tuple.
     *
     * @param  tuple  the tuple to be normalized
     * @param  len    the length of the normalized tuple
     * @return a new tuple of desired length with the element "0" added to the end as many times as necessary
     */
    protected static List normalizedTuple(List tuple, int len) {
        String[] elems = new String[len];
        Arrays.fill(elems, "0");
        Iterator iter = tuple.iterator();

        for (int i = 0; i < elems.length && iter.hasNext(); i++) {
            elems[i] = (String) iter.next();
        }

        return Arrays.asList(elems);
    }


    /**
     * Parses a version string into an array of version-ids. A version string is defined as zero or more
     * version-ids separated by a space. If <code>vers</code> is null an version-id array with
     * <code>EMPT_VERSION</code> will be returned.
     *
     * @param  vers   the version string to be parsed
     * @return an array of <code>Version</code> objects containing the version-ids
     */
    public static Version[] parseVersions(String vers) {
        ArrayList list = new ArrayList();

        if (vers != null) {
            StringTokenizer st = new StringTokenizer(vers, " ");

            while (st.hasMoreTokens()) {
                list.add(new Version(st.nextToken()));
            }
        }

        return ((list.size() > 0) ? (Version[]) list.toArray(new Version[0]) : new Version[] { EMPTY_VERSION });
    }

    /* method to test comparison functionality. supply two version-ids as arguments */
    /*public static void main(String[] args) {
        Version a = new Version(args[0]);
        Version b = new Version(args[1]);
        int result = a.compareTo(b);

        System.out.print(a);

        if (result == 0) {
            System.out.print(" == ");
        } else if (result < 0 ) {
            System.out.print(" < ");
        } else {
            System.out.print(" > ");
        }

        System.out.println(b);
    }*/
}
