package org.aksw.facete.v3.component.data_tree;

import com.google.common.collect.Range;

/**
 * This class should serve as the specification for a resource-based
 * 	(unless we find a way to generalize this)
 * data table similar to that of facete and ontowiki.
 * 
 * The idea is to allow pagination in individual table cells - e.g.
 * consider a two column table with department and members, then it should be possible to
 * track the pagination state of each department individually.
 * The pagination / search state would be part of an in-memory rdf model.
 * So a CellSpec would look like:
 * 
 * class CellSpec {
 *   ColumnSpec column;
 *   Map<RootVar, Node> key; // not sure whether to best model keys as maps or as list
 *   Concept filterConcept;
 *   Slice <>
 * }
 * 
 * 
 * 
 * https://www.w3.org/TR/vocab-data-cube/#example
 * The query underyling the example may look like:
 * 
 * SELECT ?row_key_1 ?2004-2006_male ?2004-2006_female ?2005-2007_male ?2008-2010_female {
 *    ?obs_1 period "2004-2006"
 *    ?obs_1 region ?row_key_1
 *    ?obs_1 sex "male"
 *    ?obs_1 value ?2004-2006_male
 *
 *    ?obs_2 period "2004-2006"
 *    ?obs_2 region ?row_key_1
 *    ?obs_2 sex "female"
 *    ?obs_2 value ?2004-2006_female
 *    
 * }
 * 
 * @author raven
 *
 */
public interface DataTable {
	public void addSlice(); // A filter on a predicate - such as epoche (e.g. 2000-2010)
	
	SliceSet getSlices();
	
//	/**
//	 * Add a column spec attached to this data table.
//	 * 
//	 * @return
//	 */
//	ColumnSpec createColumnSpec();

	Column rootColumnSpec();
	
	
	Range<Long> slice();
}

