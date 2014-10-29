package in.srain.cube.sample.ui.views.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import in.srain.cube.sample.R;

public class TitleAndValue extends LinearLayout {

    private TextView mTitle;
    private TextView mValue;

    public TitleAndValue(Context context) {
        this(context, null);
    }

    public TitleAndValue(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupViews();
    }

    private void setupViews() {
        LayoutInflater.from(getContext()).inflate(R.layout.base_views_title_and_info, this);
        mTitle = (TextView) findViewById(R.id.tv_views_title_and_value_title);
        mValue = (TextView) findViewById(R.id.tv_views_title_and_value_value);
    }

    public TitleAndValue title(String title) {
        mTitle.setText(title);
        return this;
    }

    public TitleAndValue value(String value) {
        mValue.setText(value);
        return this;
    }
}