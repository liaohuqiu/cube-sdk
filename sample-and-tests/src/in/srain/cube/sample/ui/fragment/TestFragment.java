package in.srain.cube.sample.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import in.srain.cube.sample.R;
import in.srain.cube.sample.ui.views.header.ptr.PtrFrameDemo;
import in.srain.cube.views.list.ListViewDataAdapter;
import in.srain.cube.views.list.ViewHolderBase;
import in.srain.cube.views.list.ViewHolderCreator;
import in.srain.cube.views.ptr.PtrFrame;

import java.util.ArrayList;

public final class TestFragment extends Fragment {
    private ArrayList<String> mStringList = new ArrayList<String>();

    public static TestFragment newInstance(String content) {
        TestFragment fragment = new TestFragment(content);
        return fragment;
    }

    private TestFragment(String content) {
        for (int i = 0; i < 20; i++) {
            mStringList.add(content);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pager_tab_indicator, container, false);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ListView listView = (ListView) view.findViewById(R.id.lv_frg_pager_tab);
        ListViewDataAdapter<String> listViewDataAdapter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
            @Override
            public ViewHolderBase<String> createViewHolder() {
                return new ViewHolder();
            }
        });

        listView.setAdapter(listViewDataAdapter);
        listViewDataAdapter.getDataList().addAll(mStringList);
        listViewDataAdapter.notifyDataSetChanged();

        final PtrFrameDemo frame = (PtrFrameDemo) view.findViewById(R.id.frame_frg_pager_tab);
        frame.setHandler(new PtrFrameDemo.Handler() {
            @Override
            public void onRefresh() {

                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.onRefreshComplete();
                    }
                }, 1000);
            }

            @Override
            public boolean canDoRefresh() {
                return true;
            }
        });
        return view;
    }

    private class ViewHolder extends ViewHolderBase<String> {

        private TextView mTextView;

        @Override
        public View createView(LayoutInflater layoutInflater) {
            mTextView = new TextView(getActivity());
            return mTextView;
        }

        @Override
        public void showData(int position, String itemData) {
            mTextView.setText(itemData);
        }
    }
}
