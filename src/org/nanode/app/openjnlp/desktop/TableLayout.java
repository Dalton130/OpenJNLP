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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * TableLayout is a layout manager that arranges components in rows and columns
 * like a spreadsheet.  TableLayout allows each row or column to be a different
 * size.  A row or column can be given an absolute size in pixels, a percentage
 * of the available space, or it can grow and shrink to fill the remaining space
 * after other rows and columns have been resized.
 *
 * <p>Using spreadsheet terminology, a cell is the intersection of a row and
 * column.  Cells have finite, non-negative sizes measured in pixels.  The
 * dimensions of a cell depend solely upon the dimensions of its row and column.
 * </p>
 *
 * <p>A component occupies a rectangular group of one or more cells.  If the
 * component occupies more than one cell, the component is resized to fit
 * perfectly in the rectangular region of cells.  If the component occupies a
 * single cell, it can be aligned in four ways within that cell.</p>
 *
 * <p>A single cell component can be stretched horizontally to fit the cell
 * (full justification), or it can be placed in the center of the cell.  The
 * component could also be left justified or right justified.  Similarly, the
 * component can be full, center, top, or bottom justified in the vertical.</p>
 *
 * <pre>
 * public static void main(String args[])
 * {
 *     // Create a frame
 *     Frame frame = new Frame("Example of TableLayout");
 *     frame.setBounds(100, 100, 300, 300);
 * <spc>
 *     // Create a TableLayout for the frame
 *     double border = 10;
 *     double size[][] = {
 *         { border, 0.10, 20, TableLayout.FILL, 20, 0.20, border },  // Columns
 *         { border, 0.20, 20, TableLayout.FILL, 20, 0.20, border } }; // Rows
 * <spc>
 *     frame.setLayout(new TableLayout(size));
 * <spc>
 *     // Create some buttons
 *     String label[] = { "Top", "Bottom", "Left", "Right", "Center", "Overlap" };
 *     Button button[] = new Button[label.length];
 * <spc>
 *     for (int i = 0; i < label.length; i++)
 *         button[i] = new Button(label[i]);
 * <spc>
 *     // Add buttons
 *     frame.add(button[0], "1, 1, 5, 1"); // Top
 *     frame.add(button[1], "1, 5, 5, 5"); // Bottom
 *     frame.add(button[2], "1, 3      "); // Left
 *     frame.add(button[3], "5, 3      "); // Right
 *     frame.add(button[4], "3, 3, c, c"); // Center
 *     frame.add(button[5], "3, 3, 3, 5"); // Overlap
 * <spc>
 *     // Allow user to close the window to terminate the program
 *     frame.addWindowListener(new WindowListener() {
 *         public void windowClosing (WindowEvent e) {
 *             System.exit (0);
 *         }
 * <spc>
 *         public void windowOpened (WindowEvent e) { }
 *         public void windowClosed (WindowEvent e) { }
 *         public void windowIconified (WindowEvent e) { }
 *         public void windowDeiconified (WindowEvent e) { }
 *         public void windowActivated (WindowEvent e) { }
 *         public void windowDeactivated (WindowEvent e) { }
 *     });
 * <spc>
 *     // Show frame
 *     frame.show();
 * }
 * </pre>
 *
 * @author  Daniel E. Barbalace
 * @author  Kevin Herrboldt (<a href="mailto:kevin@nanode.org">kevin@nanode.org</a>)
 */
public class TableLayout implements LayoutManager2, java.io.Serializable {
    /** Default row/column size. */
    protected static final double defaultSize[][] = { { }, { } };

    /** Widths of columns expressed in absolute and relative terms. */
    protected double columnSpec[];

    /** Heights of rows expressed in absolute and relative terms. */
    protected double rowSpec[];

    /** Widths of columns in pixels. */
    protected int columnSize[];

    /** Heights of rows in pixels. */
    protected int rowSize[];

    /**
     * Offsets of columns in pixels.  The left boarder of column n is at
     * columnOffset[n] and the right boarder is at columnOffset[n + 1] for all
     * columns including the last one.  columnOffset.length = columnSize.length + 1.
     */
    protected int columnOffset[];

    /**
     * Offsets of rows in pixels.  The left boarder of row n is at
     * rowOffset[n] and the right boarder is at rowOffset[n + 1] for all
     * rows including the last one.  rowOffset.length = rowSize.length + 1.
     */
    protected int rowOffset[];

    /** List of components and their sizes. */
    protected LinkedList compList;

    /**
     * Indicates whether or not the size of the cells are known for the last known
     * size of the container.  If dirty is true or the container has been resized,
     * the cell sizes must be recalculated using calculateSize.
     */
    protected boolean dirty;

    /** Previous known width of the container. */
    protected int oldWidth;

    /** Previous known height of the container. */
    protected int oldHeight;


    /**
     * Constructs an instance of TableLayout.  This TableLayout will have one row
     * and one column.
     */
    public TableLayout() {
        this(defaultSize);
    }

    /**
     * Constructs an instance of TableLayout.
     *
     * @param   size  widths of columns and heights of rows in the format,
     *                { { col0, col1, col2, ..., colN}, {row0, row1, row2, ..., rowM } }
     *                If this parameter is invalid, the TableLayout will have
     *                exactly one row and one column.
     */
    public TableLayout(double size[][]) {
        // Make sure rows and columns and nothing else is specified
        if ((size != null) && (size.length == 2)) {
            // Get the rows and columns
            double tempCol[] = size[0];
            double tempRow[] = size[1];

            // Create new rows and columns
            columnSpec = new double[tempCol.length];
            rowSpec = new double[tempRow.length];

            // Copy rows and columns
            System.arraycopy(tempCol, 0, columnSpec, 0, columnSpec.length);
            System.arraycopy(tempRow, 0, rowSpec, 0, rowSpec.length);

            // Make sure rows and columns are valid
            for (int i = 0; i < columnSpec.length; i++) {
                if (!isValidSize(columnSpec[i])) {
                    columnSpec[i] = 0.0;
                }
            }

            for (int i = 0; i < rowSpec.length; i++) {
                if (!isValidSize(rowSpec[i])) {
                    rowSpec[i] = 0.0;
                }
            }
        } else {
            double tempCol[] = { TableLayoutConstraints.FILL };
            double tempRow[] = { TableLayoutConstraints.FILL };

            setColumn(tempCol);
            setRow(tempRow);
        }

        compList = new LinkedList();
        dirty = true;
    }

    /**
     * Adds the specified component with the specified name to the layout.
     *
     * @param   name  the component name
     * @param   comp  component to be added
     */
    public void addLayoutComponent(String name, Component comp) {
        addLayoutComponent(comp, name);
    }

    /**
     * Adds the specified component to the layout, using the specified constraint object.
     *
     * @param   component   comp to add
     * @param   constraint  indicates comp's position and alignment
     */
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints == null) {
            throw new IllegalArgumentException("No constraint for the component");
        }

        // Add component and constraints to the list based on type of constraints
        if (constraints instanceof String) {
            compList.add(new Comp(comp, (String) constraints));
        } else if (constraints instanceof TableLayoutConstraints) {
            compList.add(new Comp(comp, (TableLayoutConstraints) constraints));
        } else {
            throw new IllegalArgumentException("Invalid constraints type " + constraints.getClass());
        }
    }

    /**
     * Returns the alignment along the x axis. For a TableLayout it is always 0.5, centered.
     *
     * @return  always 0.5
     */
    public float getLayoutAlignmentX(Container parent) {
        return 0.5f;
    }

    /**
     * Returns the alignment along the y axis. For a TableLayout it is always 0.5, centered.
     *
     * @return  always 0.5
     */
    public float getLayoutAlignmentY(Container parent) {
        return 0.5f;
    }

    /**
     * Invalidates the layout, indicating that if the layout manager has cached
     * information it should be discarded.
     *
     * @param   target  target container
     */
    public void invalidateLayout(Container target) {
        dirty = true;
    }

    /**
     * Lays out the specified container using this layout. This reshapes
     * the components in the specified target container in order to satisfy the
     * constraints of all components.
     *
     * @param   parent  the component which needs to be laid out
     */
    public void layoutContainer(Container parent) {
        int x, y;   // coordinates of the current component in pixels
        int w, h;   // width and height of the current component in pixels

        Dimension d = parent.getSize();

        if (dirty || d.width != oldWidth || d.height != oldHeight) {
            calculateSize(parent);  // recalculate size if parent container has changed
        }

        // layout all components
        Component component[] = parent.getComponents();
        Comp comp;

        for (int i = 0; i < component.length; i++) {
            try {
                if ((comp = findComp(component[i])) == null) {
                    break;  // skip any component not found in layout
                }

                if (!comp.isSingleCell()) {
                    // component spans multiple cells
                    x = columnOffset[comp.col1];// align left side with left of first column
                    y = rowOffset[comp.row1];   // align top side with top of first row
                    w = columnOffset[comp.col2 + 1] - columnOffset[comp.col1];  // align right side with right of last column
                    h = rowOffset[comp.row2 + 1] - rowOffset[comp.row1];// align bottom side with bottom of last column
                } else {
                    // component occupies single cell
                    Dimension pref = new Dimension(0, 0);

                    // only calculate preferred size if needed, can be expensive
                    if (comp.hAlign != TableLayoutConstraints.FULL || comp.vAlign != TableLayoutConstraints.FULL) {
                        pref = comp.component.getPreferredSize();
                    }

                    // determine width of component
                    if (comp.hAlign == TableLayoutConstraints.FULL || columnSize[comp.col1] < pref.width) {
                        w = columnSize[comp.col1];
                    } else {
                        w = pref.width;
                    }

                    // determine height of component
                    if (comp.vAlign == TableLayoutConstraints.FULL || rowSize[comp.row1] < pref.height) {
                        h = rowSize[comp.row1];
                    } else {
                        h = pref.height;
                    }

                    // Determine left and right borders
                    switch (comp.hAlign) {
                        case TableLayoutConstraints.LEFT:
                            x = columnOffset[comp.col1];
                            break;
                        case TableLayoutConstraints.RIGHT:
                            x = columnOffset[comp.col1 + 1] - w;
                            break;
                        case TableLayoutConstraints.CENTER:
                            x = columnOffset[comp.col1] + ((columnSize[comp.col1] - w) >> 1);
                            break;
                        case TableLayoutConstraints.FULL:
                            x = columnOffset[comp.col1];
                            break;
                        default:
                            x = 0;
                    }

                    // Determine top and bottom borders
                    switch (comp.vAlign) {
                        case TableLayoutConstraints.TOP:
                            y = rowOffset[comp.row1];
                            break;
                        case TableLayoutConstraints.BOTTOM:
                            y = rowOffset[comp.row1 + 1] - h;
                            break;
                        case TableLayoutConstraints.CENTER:
                            y = rowOffset[comp.row1] + ((rowSize[comp.row1] - h) >> 1);
                            break;
                        case TableLayoutConstraints.FULL:
                            y = rowOffset[comp.row1];
                            break;
                        default:
                            y = 0;
                    }
                }

                comp.component.setBounds(x, y, w, h);   // place/resize component
            } catch (Exception error) {
                // If any error occurs, skip this component
            }
        }
    }

    /**
     * Returns the maximum size of this component. The maximum size is always
     * returned as Integer.MAX_VALUE for both horizontal and vertical.
     *
     * @param   target  the component which needs to be laid out
     * @return  maximum size as a dimension
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Determines the minimum size of the container argument using this layout.
     * The minimum size is the smallest size that, if used for the container's
     * size, will ensure that all components are at least as large as their
     * minimum size.  This method cannot guarantee that all components will be
     * their minimum size.  For example, if component A and component B are each
     * allocate half of the container's width and component A wants to be 10 pixels
     * wide while component B wants to be 100 pixels wide, they cannot both be
     * accommodated.  Since in general components rather be larger than their
     * minimum size instead of smaller, component B's request will be fulfilled.
     * The minimum size of the container would be 200 pixels.
     *
     * @param   container  container being served by this layout manager
     * @return  a dimension indicating the container's minimum size
     */
    public Dimension minimumLayoutSize(Container container) {
        int scaledWidth = 0;  // Minimum width of scalled components
        int scaledHeight = 0; // Minimum height of scalled components
        int fillWidth = 0;    // Minimum width of fill components
        int fillHeight = 0;   // Minimum height of fill components
        int temp;             // Temporary variable used to compare sizes

        // Determine percentage of space allocated to fill components.  This is
        // one minus the sum of all scalable components.
        Ratio fillRatio = getFillRatio();

        // Find maximum minimum size of all scaled components
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            Comp comp = (Comp) iter.next();

            // skip if comp is not in valid rows and columns
            if (comp.col1 < 0 || comp.col1 >= columnSpec.length || comp.col2 >= columnSpec.length
             || comp.row1 < 0 || comp.row1 >= rowSpec.length    || comp.row2 >= rowSpec.length) {
                continue;
            }

            Dimension size = comp.component.getMinimumSize();

            // Calculate portion of component that is not absolutely sized
            int scalableWidth = size.width;
            int scalableHeight = size.height;

            for (int i = comp.col1; i <= comp.col2; i++) {
                if (columnSpec[i] >= 1.0) {
                    scalableWidth -= columnSpec[i];
                }
            }

            for (int i = comp.row1; i <= comp.row2; i++) {
                if (rowSpec[i] >= 1.0) {
                    scalableHeight -= rowSpec[i];
                }
            }

            // Determine total percentage of scalable space that the component
            // occupies by adding the relative columns and the fill columns
            double relativeWidth = 0.0;

            for (int i = comp.col1; i <= comp.col2; i++) {
                // Column is scaled
                if (columnSpec[i] > 0.0 && columnSpec[i] < 1.0) {
                    // Add scaled size to relativeWidth
                    relativeWidth += columnSpec[i];
                    // Column is fill
                } else if (columnSpec[i] == TableLayoutConstraints.FILL && fillRatio.width != 0.0) {
                    // Add fill size to relativeWidth
                    relativeWidth += fillRatio.width;
                }
            }

            // Determine the total scaled width as estimated by this component
            if (relativeWidth == 0) {
                temp = 0;
            } else {
                temp = (int) (scalableWidth / relativeWidth + 0.5);
            }

            // If the container needs to be bigger, make it so
            if (scaledWidth < temp) {
                scaledWidth = temp;
            }

            //----------------------------------------------------------------------

            // Determine total percentage of scalable space that the component
            // occupies by adding the relative columns and the fill columns
            double relativeHeight = 0.0;

            for (int i = comp.row1; i <= comp.row2; i++) {
                // Row is scaled
                if (rowSpec[i] > 0.0 && rowSpec[i] < 1.0) {
                    // Add scaled size to relativeHeight
                    relativeHeight += rowSpec[i];
                    // Row is fill
                } else if (rowSpec[i] == TableLayoutConstraints.FILL && fillRatio.height != 0.0) {
                    // Add fill size to relativeHeight
                    relativeHeight += fillRatio.height;
                }
            }

            // Determine the total scaled width as estimated by this component
            if (relativeHeight == 0) {
                temp = 0;
            } else {
                temp = (int) (scalableHeight / relativeHeight + 0.5);
            }

            // If the container needs to be bigger, make it so
            if (scaledHeight < temp) {
                scaledHeight = temp;
            }
        }

        // totalWidth is the scaledWidth plus the sum of all absolute widths and all
        // preferred widths
        int totalWidth = scaledWidth;

        for (int i = 0; i < columnSpec.length; i++) {
            // Is the current column an absolute size
            if (columnSpec[i] >= 1.0) {
                totalWidth += (int) (columnSpec[i] + 0.5);
                // Is the current column a preferred size
            } else if (isPreferredSize(columnSpec[i])) {
                // Assume a maximum width of zero
                int maxWidth = 0;

                // Find maximum preferred width of all components completely
                // contained within this column
                for (Iterator iter = compList.iterator(); iter.hasNext();) {
                    Comp comp = (Comp) iter.next();

                    if (comp.col1 == i && comp.col2 == i) {
                        Dimension p = comp.sizeForSpec(columnSpec[i]);
                        int width = (p == null) ? 0 : p.width;

                        if (maxWidth < width) {
                            maxWidth = width;
                        }
                    }
                }

                // Add preferred width
                totalWidth += maxWidth;
            }
        }

        // totalHeight is the scaledHeight plus the sum of all absolute heights and
        // all preferred widths
        int totalHeight = scaledHeight;

        for (int i = 0; i < rowSpec.length; i++) {
            // Is the current row an absolute size
            if (rowSpec[i] >= 1.0) {
                totalHeight += (int) (rowSpec[i] + 0.5);
                // Is the current row a preferred size
            } else if (isPreferredSize(rowSpec[i])) {
                // Assume a maximum height of zero
                int maxHeight = 0;

                // Find maximum preferred height of all components completely
                // contained within this row
                for (Iterator iter = compList.iterator(); iter.hasNext();) {
                    Comp comp = (Comp) iter.next();

                    if (comp.row1 == i && comp.row1 == i) {
                        Dimension p = comp.sizeForSpec(rowSpec[i]);
                        int height = (p == null) ? 0 : p.height;

                        if (maxHeight < height) {
                            maxHeight = height;
                        }
                    }
                }

                // Add preferred height
                totalHeight += maxHeight;
            }
        }

        // Compensate for container's insets
        Insets inset = container.getInsets();
        totalWidth += inset.left + inset.right;
        totalHeight += inset.top + inset.bottom;

        return new Dimension(totalWidth, totalHeight);
    }

    /**
     * Determines the preferred size of the container argument using this layout.
     * The preferred size is the smallest size that, if used for the container's
     * size, will ensure that all components are at least as large as their
     * preferred size.  This method cannot guarantee that all components will be
     * their preferred size.  For example, if component A and component B are each
     * allocate half of the container's width and component A wants to be 10 pixels
     * wide while component B wants to be 100 pixels wide, they cannot both be
     * accommodated.  Since in general components rather be larger than their
     * preferred size instead of smaller, component B's request will be fulfilled.
     * The preferred size of the container would be 200 pixels.
     *
     * @param   container  container being served by this layout manager
     * @return  a dimension indicating the container's preferred size
     */
    public Dimension preferredLayoutSize(Container container) {
        Dimension size;       // Preferred size of current component
        int scaledWidth = 0;  // Preferred width of scalled components
        int scaledHeight = 0; // Preferred height of scalled components
        int temp;             // Temporary variable used to compare sizes

        // Determine percentage of space allocated to fill components.  This is
        // one minus the sum of all scalable components.
        Ratio fillRatio = getFillRatio();

        // Calculate preferred/minimum column widths
        int columnPrefMin[] = new int[columnSpec.length];

        for (int i = 0; i < columnSpec.length; i++) {
            // Is the current column a preferred/minimum size
            if (isPreferredSize(columnSpec[i])) {
                // Assume a maximum width of zero
                int maxWidth = 0;

                // Find maximum preferred/minimum width of all components completely
                // contained within this column
                for (Iterator iter = compList.iterator(); iter.hasNext();) {
                    Comp comp = (Comp) iter.next();

                    if (comp.col1 == i && comp.col2 == i) {
                        Dimension p = comp.sizeForSpec(columnSpec[i]);
                        int width = (p == null) ? 0 : p.width;

                        if (maxWidth < width) {
                            maxWidth = width;
                        }
                    }
                }

                // Set column's preferred/minimum width
                columnPrefMin[i] = maxWidth;
            }
        }

        // Calculate preferred/minimum row heights
        int rowPrefMin[] = new int[rowSpec.length];

        for (int i = 0; i < rowSpec.length; i++) {
            // Is the current row a preferred/minimum size
            if (isPreferredSize(rowSpec[i])) {
                // Assume a maximum height of zero
                int maxHeight = 0;

                // Find maximum preferred height of all components completely
                // contained within this row
                for (Iterator iter = compList.iterator(); iter.hasNext();) {
                    Comp comp = (Comp) iter.next();

                    if (comp.row1 == i && comp.row1 == i) {
                        Dimension p = comp.sizeForSpec(rowSpec[i]);
                        int height = (p == null) ? 0 : p.height;

                        if (maxHeight < height) {
                            maxHeight = height;
                        }
                    }
                }

                // Add preferred height
                rowPrefMin[i] += maxHeight;
            }
        }

        // Find maximum preferred size of all scaled components
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            // Get next comp
            Comp comp = (Comp) iter.next();

            // Make sure comp is in valid rows and columns
            if (comp.col1 < 0 || comp.col1 >= columnSpec.length || comp.col2 >= columnSpec.length
             || comp.row1 < 0 || comp.row1 >= rowSpec.length    || comp.row2 >= rowSpec.length) {
                // Skip the bad component
                continue;
            }

            // Get preferred size of current component
            size = comp.component.getPreferredSize();

            // Calculate portion of component that is not absolutely sized
            int scalableWidth = size.width;
            int scalableHeight = size.height;

            for (int i = comp.col1; i <= comp.col2; i++) {
                if (columnSpec[i] >= 1.0) {
                    scalableWidth -= columnSpec[i];
                } else if (columnSpec[i] == TableLayoutConstraints.PREFERRED
                        || columnSpec[i] == TableLayoutConstraints.MINIMUM) {
                    scalableWidth -= columnPrefMin[i];
                }
            }

            for (int i = comp.row1; i <= comp.row2; i++) {
                if (rowSpec[i] >= 1.0) {
                    scalableHeight -= rowSpec[i];
                } else if (rowSpec[i] == TableLayoutConstraints.PREFERRED
                        || rowSpec[i] == TableLayoutConstraints.MINIMUM) {
                    scalableHeight -= rowPrefMin[i];
                }
            }

            //----------------------------------------------------------------------

            // Determine total percentage of scalable space that the component
            // occupies by adding the relative columns and the fill columns
            double relativeWidth = 0.0;

            for (int i = comp.col1; i <= comp.col2; i++) {
                // Column is scaled
                if (columnSpec[i] > 0.0 && columnSpec[i] < 1.0) {
                    // Add scaled size to relativeWidth
                    relativeWidth += columnSpec[i];
                    // Column is fill
                } else if (columnSpec[i] == TableLayoutConstraints.FILL && fillRatio.width != 0.0) {
                    // Add fill size to relativeWidth
                    relativeWidth += fillRatio.width;
                }
            }

            // Determine the total scaled width as estimated by this component
            if (relativeWidth == 0) {
                temp = 0;
            } else {
                temp = (int) (scalableWidth / relativeWidth + 0.5);
            }

            // If the container needs to be bigger, make it so
            if (scaledWidth < temp) {
                scaledWidth = temp;
            }

            //----------------------------------------------------------------------

            // Determine total percentage of scalable space that the component
            // occupies by adding the relative columns and the fill columns
            double relativeHeight = 0.0;

            for (int i = comp.row1; i <= comp.row2; i++) {
                // Row is scaled
                if (rowSpec[i] > 0.0 && rowSpec[i] < 1.0) {
                    // Add scaled size to relativeHeight
                    relativeHeight += rowSpec[i];
                    // Row is fill
                } else if (rowSpec[i] == TableLayoutConstraints.FILL && fillRatio.height != 0.0) {
                    // Add fill size to relativeHeight
                    relativeHeight += fillRatio.height;
                }
            }

            // Determine the total scaled width as estimated by this component
            if (relativeHeight == 0) {
                temp = 0;
            } else {
                temp = (int) (scalableHeight / relativeHeight + 0.5);
            }

            // If the container needs to be bigger, make it so
            if (scaledHeight < temp) {
                scaledHeight = temp;
            }
        }

        // totalWidth is the scaledWidth plus the sum of all absolute widths and all
        // preferred widths
        int totalWidth = scaledWidth;

        for (int i = 0; i < columnSpec.length; i++) {
            // Is the current column an absolute size
            if (columnSpec[i] >= 1.0) {
                totalWidth += (int) (columnSpec[i] + 0.5);
                // Is the current column a preferred/minimum size
            } else if (columnSpec[i] == TableLayoutConstraints.PREFERRED
                    || columnSpec[i] == TableLayoutConstraints.MINIMUM) {
                // Add preferred/minimum width
                totalWidth += columnPrefMin[i];
            }
        }

        // totalHeight is the scaledHeight plus the sum of all absolute heights and
        // all preferred widths
        int totalHeight = scaledHeight;

        for (int i = 0; i < rowSpec.length; i++) {
            // Is the current row an absolute size
            if (rowSpec[i] >= 1.0) {
                totalHeight += (int) (rowSpec[i] + 0.5);
                // Is the current row a preferred size
            } else if (isPreferredSize(rowSpec[i])) {
                // Add preferred/minimum width
                totalHeight += rowPrefMin[i];
            }
        }

        // Compensate for container's insets
        Insets inset = container.getInsets();
        totalWidth += inset.left + inset.right;
        totalHeight += inset.top + inset.bottom;

        return new Dimension(totalWidth, totalHeight);
    }

    /**
     * Removes the specified component from the layout.
     *
     * @param   component  component being removed
     */
    public void removeLayoutComponent(Component component) {
        compList.remove(component);
    }

    /**
     * Deletes a column in this layout.  All components to the right of the
     * deletion point are moved left one column.  The container will need to
     * be laid out after this method returns.  See <code>setColumn</code>.
     *
     * @param   i  zero-based index of column to delete
     * @see     #setColumn
     * @see     #deleteColumn
     */
    public void deleteColumn(int i) {
        // Make sure position is valid
        if (i < 0 || i >= columnSpec.length) {
            throw new IllegalArgumentException("Parameter i is invalid.  i = " + i
                                               + ".  Valid range is [0, " + (columnSpec.length - 1) + "].");
        }

        // Copy columns
        double column[] = new double[columnSpec.length - 1];
        System.arraycopy(columnSpec, 0, column, 0, i);
        System.arraycopy(columnSpec, i + 1, column, i, columnSpec.length - i - 1);

        // Delete column
        columnSpec = column;

        // Move all components that are to the right of row deleted
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            // Get next comp
            Comp comp = (Comp) iter.next();

            // Is the first column to the right of the new column
            if (comp.col1 >= i) {
                // Move first column
                --comp.col1;
            }

            // Is the second column to the right of the new column
            if (comp.col2 >= i) {
                // Move second column
                --comp.col2;
            }
        }

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Deletes a row in this layout.  All components below the deletion point are
     * moved up one row.  The container will need to be laid out after this method
     * returns.  See <code>setRow</code>.  There must be at least two rows in order
     * to delete a row.
     *
     * @param   i  zero-based index of column to delete
     * @see     #setRow
     * @see     #deleteRow
     */
    public void deleteRow(int i) {
        // Make sure position is valid
        if (i < 0 || i >= rowSpec.length) {
            throw new IllegalArgumentException("Parameter i is invalid.  i = " + i
                                               + ".  Valid range is [0, " + (rowSpec.length - 1) + "].");
        }

        // Copy rows
        double row[] = new double[rowSpec.length - 1];
        System.arraycopy(rowSpec, 0, row, 0, i);
        System.arraycopy(rowSpec, i + 1, row, i, rowSpec.length - i - 1);

        // Delete row
        rowSpec = row;

        // Move all components that are to below the row deleted
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            // Get next comp
            Comp comp = (Comp) iter.next();

            // Is the first row below the new row
            if (comp.row1 >= i) {
                // Move first row
                --comp.row1;
            }

            // Is the second row below the new row
            if (comp.row2 >= i) {
                // Move second row
                --comp.row2;
            }
        }

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Inserts a column in this layout.  All components to the right of the
     * insertion point are moved right one column.  The container will need to
     * be laid out after this method returns.  See <code>setColumn</code>.
     *
     * @param   i     zero-based index at which to insert the column.
     * @param   size  size of the column to be inserted
     * @see     #setColumn
     * @see     #deleteColumn
     */
    public void insertColumn(int i, double size) {
        // Make sure position is valid
        if (i < 0 || i > columnSpec.length) {
            throw new IllegalArgumentException("Parameter i is invalid.  i = " + i
                                               + ".  Valid range is [0, " + columnSpec.length + "].");
        }

        // Make sure column size is valid
        if (size < 0.0
            && size != TableLayoutConstraints.FILL
            && size != TableLayoutConstraints.PREFERRED
            && size != TableLayoutConstraints.MINIMUM) {
            size = 0.0;
        }

        // Copy columns
        double column[] = new double[columnSpec.length + 1];
        System.arraycopy(columnSpec, 0, column, 0, i);
        System.arraycopy(columnSpec, i, column, i + 1, columnSpec.length - i);

        // Insert column
        column[i] = size;
        columnSpec = column;

        // Move all components that are to the right of new row
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            // Get next comp
            Comp comp = (Comp) iter.next();

            // Is the first column to the right of the new column
            if (comp.col1 >= i) {
                // Move first column
                comp.col1++;
            }

            // Is the second column to the right of the new column
            if (comp.col2 >= i) {
                // Move second column
                comp.col2++;
            }
        }

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Inserts a row in this layout.  All components below the insertion point
     * are moved down one row.  The container will need to be laid out after this
     * method returns.  See <code>setRow</code>.
     *
     * @param   i     zero-based index at which to insert the column.
     * @param   size  size of the row to be inserted
     * @see     #setRow
     * @see     #deleteRow
     */
    public void insertRow(int i, double size) {
        // Make sure position is valid
        if (i < 0 || i > rowSpec.length) {
            throw new IllegalArgumentException("Parameter i is invalid.  i = " + i
                                               + ".  Valid range is [0, " + rowSpec.length + "].");
        }

        // Make sure row size is valid
        if (size < 0.0
            && size != TableLayoutConstraints.FILL
            && size != TableLayoutConstraints.PREFERRED
            && size != TableLayoutConstraints.MINIMUM) {
            size = 0.0;
        }

        // Copy rows
        double row[] = new double[rowSpec.length + 1];
        System.arraycopy(rowSpec, 0, row, 0, i);
        System.arraycopy(rowSpec, i, row, i + 1, rowSpec.length - i);

        // Insert row
        row[i] = size;
        rowSpec = row;

        // Move all components that are below the new row
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            // Get next comp
            Comp comp = (Comp) iter.next();

            // Is the first row to the right of the new row
            if (comp.row1 >= i) {
                // Move first row
                comp.row1++;
            }

            // Is the second row to the right of the new row
            if (comp.row2 >= i) {
                // Move second row
                comp.row2++;
            }
        }

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Gets the constraints of a given component.
     *
     * @param   component  desired component
     * @return  If the given component is found, the constraints associated with
     *          that component.  If the given component is null or is not found,
     *          null is returned.
     */
    public TableLayoutConstraints getConstraints(Component component) {
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            Comp comp = (Comp) iter.next();

            if (comp.equals(component)) {
                return (TableLayoutConstraints) comp.clone();   // return copy of constraints only
            }
        }

        return null;
    }

    /**
     * Gets the sizes of columns in this layout.
     *
     * @return  widths of each of the columns
     * @see     #setColumn
     */
    public double [] getColumn() {
        // Copy columns
        double column[] = new double[columnSpec.length];
        System.arraycopy(columnSpec, 0, column, 0, column.length);

        return column;
    }

    /**
     * Gets the height of a single row in this layout.
     *
     * @return  height of the requested row
     * @see     #setRow
     */
    public double [] getRow() {
        // Copy rows
        double row[] = new double[rowSpec.length];
        System.arraycopy(rowSpec, 0, row, 0, row.length);

        return row;
    }

    /**
     * Gets the width of a single column in this layout.
     *
     * @param   i  zero-based index of row to get.  If this parameter is not valid,
     *             an ArrayOutOfBoundsException will be thrown.
     * @return  width of the requested column
     * @see     #setRow
     */
    public double getColumn(int i) {
        return columnSpec[i];
    }

    /**
     * Gets the sizes of a row in this layout.
     *
     * @param   i  zero-based index of row to get.  If this parameter is not valid,
     *             an ArrayOutOfBoundsException will be thrown.
     * @return  height of each of the requested row
     * @see     #setRow
     */
    public double getRow(int i) {
        return rowSpec[i];
    }

    /**
     * Gets the number of columns in this layout.
     *
     * @return  the number of columns
     */
    public int getNumColumn() {
        return columnSpec.length;
    }

    /**
     * Gets the number of rows in this layout.
     *
     * @return  the number of rows
     */
    public int getNumRow() {
        return rowSpec.length;
    }

    /**
     * Sets the constraints of a given component.
     *
     * @param   component   desired component.  This parameter cannot be null.
     * @param   constraint  new set of constraints.  This parameter cannot be null.
     * @return  If the given component is found, the constraints associated with
     *          that component.  If the given component is null or is not found,
     *          null is returned.
     */
    public void setConstraints(Component component, TableLayoutConstraints constraint) {
        // Check parameters
        if (component == null) {
            throw new IllegalArgumentException("Parameter component cannot be null.");
        } else if (constraint == null) {
            throw new IllegalArgumentException("Parameter constraint cannot be null.");
        }

        // Find and update constraints for the given component
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            Comp comp = (Comp) iter.next();

            if (comp.equals(component)) {
                comp.setConstraints(constraint);
                dirty = true;
                break;
            }
        }
    }

    /**
     * Adjusts the number and sizes of rows in this layout.  After calling this
     * method, the caller should request this layout manager to perform the
     * layout.  This can be done with the following code:
     *
     * <pre>
     *     layout.layoutContainer(container);
     *     container.repaint();
     * </pre>
     *
     * or
     *
     * <pre>
     *     window.pack()
     * </pre>
     *
     * If this is not done, the changes in the layout will not be seen until the
     * container is resized.
     *
     * @param   column  heights of each of the columns
     * @see     #getColumn
     */
    public void setColumn(double column[]) {
        // Copy columns
        columnSpec = new double[column.length];
        System.arraycopy (column, 0, columnSpec, 0, columnSpec.length);

        // Make sure columns are valid
        for (int i = 0; i < columnSpec.length; i++) {
            if (columnSpec[i] < 0.0
             && columnSpec[i] != TableLayoutConstraints.FILL
             && columnSpec[i] != TableLayoutConstraints.PREFERRED
             && columnSpec[i] != TableLayoutConstraints.MINIMUM) {
                columnSpec[i] = 0.0;
            }
        }

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Adjusts the number and sizes of rows in this layout.  After calling this
     * method, the caller should request this layout manager to perform the
     * layout.  This can be done with the following code:
     *
     * <code>
     *     layout.layoutContainer(container);
     *     container.repaint();
     * </code>
     *
     * or
     *
     * <pre>
     *     window.pack()
     * </pre>
     *
     * If this is not done, the changes in the layout will not be seen until the
     * container is resized.
     *
     * @param   row  widths of each of the rows.  This parameter cannot be null.
     * @see     #getRow
     */
    public void setRow(double row[]) {
        // Copy rows
        rowSpec = new double[row.length];
        System.arraycopy (row, 0, rowSpec, 0, rowSpec.length);

        // Make sure rows are valid
        for (int i = 0; i < rowSpec.length; i++) {
            if (rowSpec[i] < 0.0
             && rowSpec[i] != TableLayoutConstraints.FILL
             && rowSpec[i] != TableLayoutConstraints.PREFERRED
                && rowSpec[i] != TableLayoutConstraints.MINIMUM) {
                rowSpec[i] = 0.0;
            }
        }

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Adjusts the width of a single column in this layout.  After calling this
     * method, the caller should request this layout manager to perform the
     * layout.  This can be done with the following code:
     *
     * <code>
     *     layout.layoutContainer(container);
     *     container.repaint();
     * </code>
     *
     * or
     *
     * <pre>
     *     window.pack()
     * </pre>
     *
     * If this is not done, the changes in the layout will not be seen until the
     * container is resized.
     *
     * @param   i     zero-based index of column to set.  If this parameter is not
     *                valid, an ArrayOutOfBoundsException will be thrown.
     * @param   size  width of the column.  This parameter cannot be null.
     * @see     #getColumn
     */
    public void setColumn(int i, double size) {
        // Make sure size is valid
        if (size < 0.0
         && size != TableLayoutConstraints.FILL
         && size != TableLayoutConstraints.PREFERRED
         && size != TableLayoutConstraints.MINIMUM) {
            size = 0.0;
        }

        // Copy new size
        columnSpec[i] = size;

        // Indicate that the cell sizes are not known
        dirty = true;
    }

    /**
     * Adjusts the height of a single row in this layout.  After calling this
     * method, the caller should request this layout manager to perform the
     * layout.  This can be done with the following code:
     *
     * <code>
     *     layout.layoutContainer(container);
     *     container.repaint();
     * </code>
     *
     * or
     *
     * <pre>
     *     window.pack()
     * </pre>
     *
     * If this is not done, the changes in the layout will not be seen until the
     * container is resized.
     *
     * @param   i     zero-based index of row to set.  If this parameter is not
     *                valid, an ArrayOutOfBoundsException will be thrown.
     * @param   size  height of the row.  This parameter cannot be null.
     * @see     #getRow
     */
    public void setRow(int i, double size) {
        // Make sure size is valid
        if (size < 0.0
         && size != TableLayoutConstraints.FILL
         && size != TableLayoutConstraints.PREFERRED
         && size != TableLayoutConstraints.MINIMUM) {
            size = 0.0;
        }

        // Copy new size
        rowSpec[i] = size;

        // Indicate that the cell sizes are not known
        dirty = true;
    }


    /**
     * Converts this TableLayout to a string.
     *
     * @return  a string representing the columns and row sizes in the form
     *          "{{col0, col1, col2, ..., colN}, {row0, row1, row2, ..., rowM}}"
     */
    public String toString() {
        String value = "TableLayout {{";

        if (columnSpec.length > 0) {
            for (int i = 0; i < columnSpec.length - 1; i++) {
                value += columnSpec[i] + ", ";
            }

            value += columnSpec[columnSpec.length - 1] + "}, {";
        } else {
            value += "}, {";
        }

        if (rowSpec.length > 0) {
            for (int i = 0; i < rowSpec.length - 1; i++) {
                value += rowSpec[i] + ", ";
            }

            value += rowSpec[rowSpec.length - 1] + "}}";
        } else {
            value += "}}";
        }

        return value;
    }

    /**
     * Draws a grid on the given container.  This is useful for seeing where the
     * rows and columns go.  In the container's paint or paintComponent method, call this method.
     *
     * @param   container  container using this TableLayout
     * @param   g          graphics content of container (can be offscreen)
     */
    public void paintGrid(Container container, Graphics g) {
        // Calculate the sizes of the rows and columns
        Dimension d = container.getSize();
        Insets insets = container.getInsets();

        if (dirty || d.width != oldWidth || d.height != oldHeight) {
            calculateSize(container);
        }

        g.setColor(Color.blue);

        // draw vertical lines to show column widths
        int x = insets.left;

        for (int col = 0; col < columnSize.length; col++) {
            if (x != insets.left) {
                g.drawLine(x, insets.top, x, d.height - insets.bottom);
            }

            x += columnSize[col];
        }

        // draw horizontal lines to show row heights
        int y = insets.top;

        for (int row = 0; row < rowSize.length; row++) {
            if (y != insets.top) {
                g.drawLine(insets.left, y, d.width - insets.right, y);
            }

            y += rowSize[row];
        }
    }

    /**
     * Determines whether or not there are any hidden components.  A hidden
     * component is one that will not be shown with this layout's current
     * configuration.  Such a component is, at least partly, in an invalid row
     * or column.  For example, on a table with five rows, row -1 and row 5 are both
     * invalid.  Valid rows are 0 through 4, inclusively.
     *
     * @return  True, if there are any hidden components.  False, otherwise.
     * @see     #overlapping
     */
    public boolean hidden() {
        // Assume no components are hidden
        boolean hidden = false;

        // Check all components
        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            // Get next comp
            Comp comp = (Comp) iter.next();

            // Is this component valid
            if (comp.row1 < 0 || comp.col1 < 0 || comp.row2 > rowSpec.length || comp.col2 > columnSpec.length) {
                hidden = true;
                break;
            }
        }

        return hidden;
    }

    /**
     * Determines whether or not there are any overlapping components.  Two
     * components overlap if they cover at least one common cell.
     *
     * @return  True, if there are any overlapping components.  False, otherwise.
     * @see     #hidden
     */
    public boolean overlapping() {
        // Count contraints
        int numComp = compList.size();

        // If there are no components, they can't be overlapping
        if (numComp == 0) {
            return false;
        }

        // Assume no components are overlapping
        boolean overlapping = false;

        // Put entries in an array
        Comp comp[] = (Comp[]) compList.toArray(new Comp[numComp]);

        // Check all components
        for (int knowUnique = 1; !overlapping && knowUnique < numComp; knowUnique++) {
            for (int checking = knowUnique - 1; checking >= 0; --checking) {
                if (comp[checking].col1 >= comp[knowUnique].col1
                 && comp[checking].col1 <= comp[knowUnique].col2
                 && comp[checking].row1 >= comp[knowUnique].row1
                 && comp[checking].row1 <= comp[knowUnique].row2
                 || comp[checking].col2 >= comp[knowUnique].col1
                 && comp[checking].col2 <= comp[knowUnique].col2
                 && comp[checking].row2 >= comp[knowUnique].row1
                 && comp[checking].row2 <= comp[knowUnique].row2) {
                    overlapping = true;
                    break;
                }
            }
        }

        return overlapping;
    }

    /**
     * Calculates the sizes of the rows and columns based on the absolute and
     * relative sizes specified in <code>rowSpec</code> and <code>columnSpec</code>
     * and the size of the container.  The result is stored in <code>rowSize</code>
     * and <code>columnSize</code>.
     *
     * @param   container  container using this TableLayout
     */
    protected void calculateSize(Container container) {
        // Get number of rows and columns
        int numColumn = columnSpec.length;
        int numRow = rowSpec.length;

        // Create array to hold actual sizes in pixels
        columnSize = new int[numColumn];
        rowSize = new int[numRow];

        // Get the container's insets
        Insets inset = container.getInsets();

        // Get the size of the container's available space
        Dimension d = container.getSize();
        int totalWidth = d.width - inset.left - inset.right;
        int totalHeight = d.height - inset.top - inset.bottom;

        // Initially, the available space is the total space
        int availableWidth = totalWidth;
        int availableHeight = totalHeight;

        // Assign absolute widths; this reduces available width
        for (int i = 0; i < numColumn; i++) {
            // Is the current column an absolue size
            if (columnSpec[i] >= 1.0 || columnSpec[i] == 0.0) {
                // Assign absolute width
                columnSize[i] = (int) (columnSpec[i] + 0.5);

                // Reduce available width
                availableWidth -= columnSize[i];
            }
        }

        // Assign absolute heights; this reduces available height
        for (int i = 0; i < numRow; i++) {
            // Is the current column an absolue size
            if (rowSpec[i] >= 1.0 || rowSpec[i] == 0.0) {
                // Assign absolute width
                rowSize[i] = (int) (rowSpec[i] + 0.5);

                // Reduce available width
                availableHeight -= rowSize[i];
            }
        }

        // Assign preferred and minimum widths; this reduces available width.
        // Assignment of preferred/minimum with is like assignment of absolute
        // widths except that each column must determine the maximum
        // preferred/minimum width of the components that are completely contained
        // within the column.
        for (int i = 0; i < numColumn; i++) {
            // Is the current column a preferred size
            if (isPreferredSize(columnSpec[i])) {
                // Assume a maximum width of zero
                int maxWidth = 0;

                // Find maximum preferred width of all components completely
                // contained within this column
                for (Iterator iter = compList.iterator(); iter.hasNext();) {
                    Comp comp = (Comp) iter.next();

                    if (comp.col1 == i && comp.col2 == i) {
                        Dimension p = (columnSpec[i] == TableLayoutConstraints.PREFERRED)
                                    ? comp.component.getPreferredSize()
                                    : comp.component.getMinimumSize();

                        int width = (p == null) ? 0 : p.width;

                        if (maxWidth < width) {
                            maxWidth = width;
                        }
                    }
                }

                // Assign preferred width
                columnSize[i] = maxWidth;

                // Reduce available width
                availableWidth -= maxWidth;
            }
        }

        // Assign preferred and minimum heights; this reduces available height.
        // Assignment of preferred/minimum with is like assignment of absolute
        // heights except that each row must determine the maximum
        // preferred/minimum height of the components that are completely contained
        // within the row.
        for (int i = 0; i < numRow; i++) {
            // Is the current row a preferred size
            if (isPreferredSize(rowSpec[i])) {
                // Assume a maximum height of zero
                int maxHeight = 0;

                // Find maximum preferred height of all components completely
                // contained within this row
                for (Iterator iter = compList.iterator(); iter.hasNext();) {
                    Comp comp = (Comp) iter.next();

                    if (comp.row1 == i && comp.row2 == i) {
                        Dimension p = (rowSpec[i] == TableLayoutConstraints.PREFERRED)
                                    ? comp.component.getPreferredSize()
                                    : comp.component.getMinimumSize();

                        int height = (p == null) ? 0 : p.height;

                        if (maxHeight < height) {
                            maxHeight = height;
                        }
                    }
                }

                // Assign preferred height
                rowSize[i] = maxHeight;

                // Reduce available height
                availableHeight -= maxHeight;
            }
        }

        // Remember how much space is available for relatively sized cells
        int relativeWidth = availableWidth;
        int relativeHeight = availableHeight;

        // Make sure relativeWidth and relativeHeight are non-negative
        if (relativeWidth < 0) {
            relativeWidth = 0;
        }

        if (relativeHeight < 0) {
            relativeHeight = 0;
        }

        // Assign relative widths
        for (int i = 0; i < numColumn; i++) {
            // Is the current column an relative size
            if (columnSpec[i] > 0.0 && columnSpec[i] < 1.0) {
                // Assign relative width
                columnSize[i] = (int) (columnSpec[i] * relativeWidth + 0.5);

                // Reduce available width
                availableWidth -= columnSize[i];
            }
        }

        // Assign relative widths
        for (int i = 0; i < numRow; i++) {
            // Is the current column an relative size
            if (rowSpec[i] > 0.0 && rowSpec[i] < 1.0) {
                // Assign relative width
                rowSize[i] = (int) (rowSpec[i] * relativeHeight + 0.5);

                // Reduce available width
                availableHeight -= rowSize[i];
            }
        }

        // Make sure availableWidth and availableHeight are non-negative
        if (availableWidth < 0) {
            availableWidth = 0;
        }

        if (availableHeight < 0) {
            availableHeight = 0;
        }

        // Count the number of "fill" cells
        int numFillWidth = 0;
        int numFillHeight = 0;

        for (int i = 0; i < numColumn; i++) {
            if (columnSpec[i] == TableLayoutConstraints.FILL) {
                numFillWidth++;
            }
        }

        for (int i = 0; i < numRow; i++) {
            if (rowSpec[i] == TableLayoutConstraints.FILL) {
                numFillHeight++;
            }
        }

        // If numFillWidth (numFillHeight) is zero, the cooresponding if statements
        // will always evaluate to false and the division will not occur.

        // If there are more than one "fill" cell, slack may occur due to rounding
        // errors
        int slackWidth = availableWidth;
        int slackHeight = availableHeight;

        // Assign "fill" cells equal amounts of the remaining space
        for (int i = 0; i < numColumn; i++) {
            if (columnSpec[i] == TableLayoutConstraints.FILL) {
                columnSize[i] = availableWidth / numFillWidth;
                slackWidth -= columnSize[i];
            }
        }

        for (int i = 0; i < numRow; i++) {
            if (rowSpec[i] == TableLayoutConstraints.FILL) {
                rowSize[i] = availableHeight / numFillHeight;
                slackHeight -= rowSize[i];
            }
        }

        // Add slack to the last "fill" cell
        for (int i = numColumn - 1; i >= 0; --i) {
            if (columnSpec[i] == TableLayoutConstraints.FILL) {
                columnSize[i] += slackWidth;
                break;
            }
        }

        for (int i = numRow - 1; i >= 0; --i) {
            if (rowSpec[i] == TableLayoutConstraints.FILL) {
                rowSize[i] += slackHeight;
                break;
            }
        }

        // Calculate offsets of each column (done for effeciency)
        columnOffset = new int[numColumn + 1];
        columnOffset[0] = inset.left;

        for (int i = 0; i < numColumn; i++) {
            columnOffset[i + 1] = columnOffset[i] + columnSize[i];
        }

        // Calculate offsets of each row (done for effeciency)
        rowOffset = new int[numRow + 1];
        rowOffset[0] = inset.top;

        for (int i = 0; i < numRow; i++) {
            rowOffset[i + 1] = rowOffset[i] + rowSize[i];
        }

        // Indicate that the size of the cells are known for the container's
        // current size
        dirty = false;
        oldWidth = totalWidth;
        oldHeight = totalHeight;
    }

    /**
     * Finds the Comp that matches the specified component.
     * <p>
     * This is horribly inefficient, searching through the Comp list linearly.
     *
     * @param   component  the component to find
     * @return  the Comp object that matches the component or <code>null</code> if not found
     */
    protected Comp findComp(Component component) {
        Comp comp;

        for (Iterator iter = compList.iterator(); iter.hasNext();) {
            comp = (Comp) iter.next();

            if (comp.equals(component)) {
                return comp;
            }
        }

        return null;
    }

    protected boolean isPreferredSize(double value) {
        return (value == TableLayoutConstraints.PREFERRED || value == TableLayoutConstraints.MINIMUM);
    }

    protected boolean isValidSize(double value) {
        return (value >= 0.0 || value == TableLayoutConstraints.FILL ||
                value == TableLayoutConstraints.PREFERRED || value == TableLayoutConstraints.MINIMUM);
    }

    protected Ratio getFillRatio() {
        Ratio fillRatio = new Ratio(1.0, 1.0);
        int numWidth = 0;
        int numHeight = 0;

        // calculate for all non-absolute columns
        for (int i = 0; i < columnSpec.length; i++) {
            if (columnSpec[i] > 0.0 && columnSpec[i] < 1.0) {
                fillRatio.width -= columnSpec[i];
            } else if (columnSpec[i] == TableLayoutConstraints.FILL) {
                numWidth++;
            }
        }

        if (numWidth > 1) {
            fillRatio.width /= numWidth;
        }

        if (fillRatio.width < 0.0) {
            fillRatio.width = 0.0;
        }

        // calculate for all non-absolute rows
        for (int i = 0; i < rowSpec.length; i++) {
            if (rowSpec[i] > 0.0 && rowSpec[i] < 1.0) {
                fillRatio.height -= rowSpec[i];
            } else if (rowSpec[i] == TableLayoutConstraints.FILL) {
                numHeight++;
            }
        }

        if (numHeight > 1) {
            fillRatio.height /= numHeight;
        }

        if (fillRatio.height < 0.0) {
            fillRatio.height = 0.0;
        }

        return fillRatio;
    }

    // The following inner class is used to bind components to their constraints
    protected class Comp extends TableLayoutConstraints {
        /** Component bound by the constraints */
        protected Component component;


        /**
         * Constructs a Comp that binds a component to a set of constraints.
         *
         * @param   comp  component being bound
         */
        protected Comp(Component comp) {
            super();

            component = comp;
        }

        /**
         * Constructs a Comp that binds a component to a set of constraints.
         *
         * @param   comp  component being bound
         * @param   tlc   constraints being applied
         */
        protected Comp(Component comp, String tlc) {
            super(tlc);

            component = comp;
        }

        /**
         * Constructs a Comp that binds a component to a set of constraints.
         *
         * @param   comp  component being bound
         * @param   tlc   constraints being applied
         */
        protected Comp(Component comp, TableLayoutConstraints tlc) {
            super(tlc.col1, tlc.row1, tlc.col2, tlc.row2, tlc.hAlign, tlc.vAlign);

            component = comp;
        }

        /**
         * Determines whether or not two entries are equal.
         *
         * @param   object  object being compared to
         * @return  <code>true</code> if the components are equal; <code>false</code>
         *          otherwise
         */
        public boolean equals(Object object) {
            return component.equals(object);
        }

        boolean isSingleCell() {
            return (row1 == row2 && col1 == col2);
        }

        Dimension sizeForSpec(double spec) {
            Dimension dim;

            if (spec == TableLayoutConstraints.PREFERRED) {
                dim = component.getPreferredSize();
            } else {
                dim = component.getMinimumSize();
            }

            return dim;
        }

        void setConstraints(TableLayoutConstraints tlc) {
            col1 = tlc.col1;
            row1 = tlc.row1;
            col2 = tlc.col2;
            row2 = tlc.row2;
            hAlign = tlc.hAlign;
            vAlign = tlc.vAlign;
        }
    }


    class Ratio {
        double width;
        double height;

        Ratio(double w, double h) {
            width = w;
            height = h;
        }
    }
}
