package org.aksw.facete3.cli.main;

import java.util.ArrayList;
import java.util.List;

/**
 * Item-based paginator implementation
 * 
 * @author raven
 *
 * @param <T>
 */
public abstract class PaginatorBase<T>
	implements Paginator<T>
{
	public PaginatorBase(long itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	protected long itemsPerPage = 10;
	
	protected long showProximity = 4; // two prior / two next pages
//	protected long showBefore = 3;
//	protected long showAfter = 3;
	
	
	// [<] [<<] [--] [-] [x] [+] [...] [>>] [>]
	protected long visiblePages;
// Potential further attributes (taken from jassa / angular)
//	  boundaryLinks: false,
//	  directionLinks: true,
//	  firstText: 'First',
//	  previousText: 'Previous',
//	  nextText: 'Next',
//	  lastText: 'Last',
//	  rotate: true
	
	/**
	 * 
	 * 
	 * @param numItems
	 * @param itemOffset The page containing this offset is marked as active
	 * @return
	 */
	public List<T> createPages(long numItems, long itemOffset) {
		// Add one extra page if the devision yields a remainder
		long numPages = numItems / itemsPerPage
				+ Math.min(numItems % itemsPerPage, 1);

		long activePage = itemOffset / itemsPerPage;

		long halfProximity = showProximity / 2;
		long extraSpace = showProximity % 2;

		long showBefore = halfProximity + extraSpace;
		long showAfter = halfProximity;

		long availBefore = activePage;
		long availAfter = numPages - activePage - 1;
				

		long beforeToAfter = Math.max(showBefore - availBefore, 0);
		showAfter += beforeToAfter;
		
		long afterToBefore = Math.max(showAfter - availAfter, 0);
		showBefore += afterToBefore;
		
		// Final adjustment of show before (discard any still available space)
		showBefore = Math.min(showBefore, availBefore);
		showAfter = Math.min(showAfter, availAfter);
		
		long from = activePage - showBefore;
		long to = activePage + showAfter;
		
		List<T> result = new ArrayList<>();
		for(long i = from; i <= to; ++i) {
			long pageStart = i * itemsPerPage;
			long pageEnd = Math.min(pageStart + itemsPerPage, numItems);

			boolean isActive = itemOffset >= pageStart && itemOffset < pageEnd;
		
			T page = createPage(i, pageStart, pageEnd, isActive);
			result.add(page);
		}
		
		return result;
	}
	
	abstract protected T createPage(long pageNumber, long pageStart, long pageEnd, boolean isActive);
}