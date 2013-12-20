package com.srain.sample.ui.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.srain.cube.app.lifecycle.LifeCycleComponentManager;
import com.srain.cube.image.DefautGlobalImageLoader;
import com.srain.cube.image.DefautImageLoader;
import com.srain.cube.image.RecyclingImageView;
import com.srain.sample.data.Images;
import com.srain.sdk.R;

public class ImageListFragment extends Fragment {

	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private ImageAdapter mAdapter;
	private DefautGlobalImageLoader mImageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);

		mAdapter = new ImageAdapter();
		mImageLoader = DefautGlobalImageLoader.getInstance(getActivity());
		LifeCycleComponentManager.tryAddComponentToContainer(mImageLoader, getActivity());
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.image_list_fragment, container, false);
		final ListView listView = (ListView) v.findViewById(R.id.list_view);
		listView.setAdapter(mAdapter);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					// mImageLoader.pauseWork();
				} else {
					// mImageLoader.resumeWork();
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			mImageLoader.clearCache();
			Toast.makeText(getActivity(), R.string.clear_cache_complete_toast, Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ImageAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return Images.imageUrls.length;
		}

		@Override
		public Object getItem(int position) {
			return Images.imageUrls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			if (convertView == null) {
				convertView = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.item_image_list, null);
			} else {
				convertView = (ViewGroup) convertView;
			}

			TextView textView = (TextView) convertView.findViewById(R.id.tv_list_item);
			RecyclingImageView imageView = (RecyclingImageView) convertView.findViewById(R.id.iv_list_item);
			textView.setText(String.valueOf(position));

			mImageLoader.load(imageView, Images.imageUrls[position], mImageThumbSize, mImageThumbSize, R.drawable.empty_photo);
			return convertView;
		}
	}
}
