package org.aksw.facete3.cli.main;

public class PaginatorImpl
	extends PaginatorBase<Page>
{
	public PaginatorImpl(long itemsPerPage) {
		super(itemsPerPage);
	}
	
	@Override
	protected Page createPage(long pageNumber, long pageStart, long pageEnd, boolean isActive) {
		Page result = new Page();//Page.factory();
		result.setPageOffset(pageStart);
		result.setActive(isActive);
		result.setPageNumber(pageNumber + 1);
		return result;
	}
}