package in.srain.cube.views.list;

/**
 * A adapter using View Holder to display the item of a list view;
 *
 * @param <ItemDataType>
 * @author http://www.liaohuqiu.net
 */
public class PagedListViewDataAdapter<ItemDataType> extends ListViewDataAdapterBase<ItemDataType> {

    protected ListPageInfo<ItemDataType> mListPageInfo;

    public PagedListViewDataAdapter() {
        super();
    }

    /**
     * @param viewHolderCreator The view holder creator will create a View Holder that extends {@link ViewHolderBase}
     */
    public PagedListViewDataAdapter(ViewHolderCreator<ItemDataType> viewHolderCreator) {
        super(viewHolderCreator);
    }

    public void setListPageInfo(ListPageInfo<ItemDataType> listPageInfo) {
        mListPageInfo = listPageInfo;
    }

    public ListPageInfo<ItemDataType> getListPageInfo() {
        return mListPageInfo;
    }

    @Override
    public int getCount() {
        if (null == mListPageInfo) {
            return 0;
        }
        return mListPageInfo.getListLength();
    }

    @Override
    public ItemDataType getItem(int position) {
        if (null == mListPageInfo) {
            return null;
        }
        return mListPageInfo.getItem(position);
    }

    public void clearList() {
        if (null != mListPageInfo && mListPageInfo.getDataList() != null) {
            mListPageInfo.getDataList().clear();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
