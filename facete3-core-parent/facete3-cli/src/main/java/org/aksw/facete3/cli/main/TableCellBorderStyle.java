package org.aksw.facete3.cli.main;

/**
 * Describing how table cells are separated when drawn
 */
public enum TableCellBorderStyle {
    /**
     * There is no separation between table cells, they are drawn immediately next to each other
     */
    None(0),
    /**
     * There is a single space of separation between the cells, drawn as a single line
     */
    SingleLine(1),
    /**
     * There is a single space of separation between the cells, drawn as a double line
     */
    DoubleLine(1),
    /**
     * There is a single space of separation between the cells, kept empty
     */
    EmptySpace(1),
    ;

    private final int size;

    TableCellBorderStyle(int size) {
        this.size = size;
    }

    /**
     * Returns the number of rows (for vertical borders) or columns (for horizontal borders) this table cell border will
     * take up when used.
     * @return Size of the border, in rows or columns depending on the context
     */
    int getSize() {
        return size;
    }
}