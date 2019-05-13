/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is openjnlp.nanode.org code.
 *
 * The Initial Developer of the Original Code is Nanode LLC. Portions created by Nanode are
 * Copyright (C) 2001-2002 Nanode LLC. All Rights Reserved.
 *
 * Contributor(s):
 *    Kevin Herrboldt <kevin@nanode.org>
 *
 * This is based on Sun Microsystems, Inc. example code written by Daniel E. Barbalace
 */
package org.nanode.app.openjnlp.desktop;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * The following inner class is used to bind components to their
 * constraints.
 *
 * @author  Daniel E. Barbalace
 * @author  Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class TableLayoutConstraints {
    /** Indicates that the component is left justified in its cell */
    public static final int LEFT = 0;

    /** Indicates that the component is top justified in its cell */
    public static final int TOP = 0;

    /** Indicates that the component is centered in its cell */
    public static final int CENTER = 1;

    /** Indicates that the component is full justified in its cell */
    public static final int FULL = 2;

    /** Indicates that the component is bottom justified in its cell */
    public static final int BOTTOM = 3;

    /** Indicates that the component is right justified in its cell */
    public static final int RIGHT = 3;

    /** Indicates that the row/column should fill the available space */
    public static final double FILL = -1.0;

    /**
     * Indicates that the row/column should be allocated just enough space to
     * accomidate the preferred size of all components contained completely within
     * this row/column.
     */
    public static final double PREFERRED = -2.0;

    /**
     * Indicates that the row/column should be allocated just enough space to
     * accomidate the minimum size of all components contained completely within
     * this row/column.
     */
    public static final double MINIMUM = -3.0;

    /** Minimum value for an alignment */
    public static final int MIN_ALIGN = 0;

    /** Maximum value for an alignment */
    public static final int MAX_ALIGN = 3;


    /** Cell in which the upper left corner of the component lays */
    public int col1, row1;

    /** Cell in which the lower right corner of the component lays */
    public int col2, row2;

    /** Horizontal justification if component occupies just one cell */
    public int hAlign;

    /** Verical justification if component occupies just one cell */
    public int vAlign;


    /**
     * Constructs an TableLayoutConstraints with the default settings.  This
     * constructor is equivalent to TableLayoutConstraints(0, 0, 0, 0, FULL, FULL).
     */
    public TableLayoutConstraints() {
        col1 = row1 = col2 = col2 = 0;
        hAlign = vAlign = FULL;
    }

    /**
     * Constructs an TableLayoutConstraints a set of constraints.
     *
     * @param col1      column where upper-left cornor of the component is placed
     * @param row1      row where upper-left cornor of the component is placed
     * @param col2      column where lower-right cornor of the component is placed
     * @param row2      row where lower-right cornor of the component is placed
     * @param hAlign    horizontal justification of a component in a single cell
     * @param vAlign    vertical justification of a component in a single cell
     */
    public TableLayoutConstraints(int col1, int row1, int col2, int row2, int hAlign, int vAlign) {
        this.col1 = col1;
        this.row1 = row1;
        this.col2 = col2;
        this.row2 = row2;

        if ((hAlign < MIN_ALIGN) || (hAlign > MAX_ALIGN)) {
            this.hAlign = FULL;
        } else {
            this.hAlign = hAlign;
        }

        if ((vAlign < MIN_ALIGN) || (vAlign > MAX_ALIGN)) {
            this.vAlign = FULL;
        } else {
            this.vAlign = vAlign;
        }
    }

    /**
     * Constructs an TableLayoutConstraints from a string.
     *
     * @param constraints    indicates TableLayoutConstraints's position and justification
     *                       as a string in the form "row, column" or
     *                       "row, column, horizontal justification, vertical
     *                       justification" or "row 1, column 1, row 2, column 2".
     *                       It is also acceptable to delimit the paramters with
     *                       spaces instead of commas.
     */
    public TableLayoutConstraints(String constraints) {
        // Parse constraints using spaces or commas
        StringTokenizer st = new StringTokenizer(constraints, ", ");

        // Use default values for any parameter not specified or specified
        // incorrectly.  The default parameters place the component in a single
        // cell at column 0, row 0.  The component is fully justified.
        col1 = 0;
        row1 = 0;
        col2 = 0;
        row2 = 0;
        hAlign = FULL;
        vAlign = FULL;

        String token = null;

        try {
            // Get the first column (assume component is in only one column)
            token = st.nextToken();
            col1 = new Integer(token).intValue();
            col2 = col1;

            // Get the first row (assume component is in only one row)
            token = st.nextToken();
            row1 = new Integer(token).intValue();
            row2 = row1;

            // Get the second column
            token = st.nextToken();
            col2 = new Integer(token).intValue();

            // Get the second row
            token = st.nextToken();
            row2 = new Integer(token).intValue();
        } catch (NoSuchElementException error) {
            // do nothing
        } catch (NumberFormatException error) {
            try {
                // Check if token means horizontally justification the component
                if (token.equalsIgnoreCase("l")) {
                    hAlign = LEFT;
                } else if (token.equalsIgnoreCase("c")) {
                    hAlign = CENTER;
                } else if (token.equalsIgnoreCase("f")) {
                    hAlign = FULL;
                } else if (token.equalsIgnoreCase("r")) {
                    hAlign = RIGHT;
                }

                // There can be one more token for the vertical justification even
                // if the horizontal justification is invalid
                token = st.nextToken();

                // Check if token means horizontally justification the component
                if (token.equalsIgnoreCase("t")) {
                    vAlign = TOP;
                } else if (token.equalsIgnoreCase("c")) {
                    vAlign = CENTER;
                } else if (token.equalsIgnoreCase("f")) {
                    vAlign = FULL;
                } else if (token.equalsIgnoreCase("b")) {
                    vAlign = BOTTOM;
                }
            }  catch (NoSuchElementException error2) { }
        }

        // Make sure row2 >= row1
        if (row2 < row1) {
            row2 = row1;
        }

        // Make sure col2 >= col1
        if (col2 < col1) {
            col2 = col1;
        }
    }

    /**
     * Creates a copy of this table layout constraint.
     *
     * @return  a copy of this table layout constraint.
     */
    public Object clone() {
        return new TableLayoutConstraints(col1, row1, col2, row2, hAlign, vAlign);
    }

    /**
     * Gets a string representation of this TableLayoutConstraints.
     *
     * @return a string in the form "row 1, column 1, row 2, column 2" or
     *         "row, column, horizontal justification, vertical justification"
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append(row1);
        buffer.append(", ");
        buffer.append(col1);
        buffer.append(", ");

        if ((row1 == row2) && (col1 == col2)) {
            final char h[] = { 'l', 'c', 'f', 'r' };
            final char v[] = { 't', 'c', 'f', 'b' };

            buffer.append(h[hAlign]);
            buffer.append(", ");
            buffer.append(v[vAlign]);
        } else {
            buffer.append(row2);
            buffer.append(", ");
            buffer.append(col2);
        }

        return buffer.toString();
    }
}
