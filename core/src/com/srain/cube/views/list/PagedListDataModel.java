package com.srain.cube.views.list;

import java.util.List;

public abstract class PagedListDataModel<T> {

	protected ListPageInfo<T> mListPageInfo;

	private PagedListDataHandler mPagedListDataHandler;

	public interface PagedListDataHandler {
		public void onPageDataLoaed(ListPageInfo<?> listPageInfo);
	}

	public void setPageListDataHandler(PagedListDataHandler handler) {
		mPagedListDataHandler = handler;
	}

	public void queryFirstPage() {
		checkPageInfo();
		mListPageInfo.goToHead();
		doQueryDataInner();
	}

	public void queryNextPage() {
		checkPageInfo();
		if (mListPageInfo.nextPage()) {
			doQueryDataInner();
		}
	}

	private void checkPageInfo() {
		if (null == mListPageInfo) {
			throw new IllegalArgumentException(" mListPageInfo has not been initialized.");
		}
	}

	private void doQueryDataInner() {
		if (!mListPageInfo.tryEnterLock()) {
			return;
		}
		doQeuryData();
	}

	protected abstract void doQeuryData();

	protected void setRequestResult(List<T> list, int total) {
		mListPageInfo.updateListInfo(list, total);
		if (null != mPagedListDataHandler) {
			mPagedListDataHandler.onPageDataLoaed(mListPageInfo);
		}
	}

	public ListPageInfo<T> getListPageInfo() {
		return mListPageInfo;
	}
}