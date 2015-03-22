package in.srain.cube.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import in.srain.cube.R;

/**
 * taken from https://github.com/drakeet/MaterialDialog
 */
public class MaterialDialog {

    private final static int BUTTON_BOTTOM = 8;
    private final static int BUTTON_TOP = 9;

    private boolean mCancel;
    private Context mContext;
    private AlertDialog mAlertDialog;
    private MaterialDialog.Builder mBuilder;
    private View mView;
    private int mTitleResId;
    private CharSequence mTitle;
    private int mMessageResId;
    private CharSequence mMessage;
    private Button mPositiveButton;
    private LinearLayout.LayoutParams mLayoutParams;
    private Button mNegativeButton;
    private boolean mHasShow = false;
    private Drawable mBackgroundDrawable;
    private int mBackgroundResId;
    private View mMessageContentView;
    private DialogInterface.OnDismissListener mOnDismissListener;

    public MaterialDialog(Context context) {
        this.mContext = context;
    }

    private static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public void show() {
        if (mHasShow == false) {
            mBuilder = new Builder();
        } else {
            mAlertDialog.show();
        }
        mHasShow = true;
    }

    public MaterialDialog setView(View view) {
        mView = view;
        if (mBuilder != null) {
            mBuilder.setView(view);
        }
        return this;
    }

    public MaterialDialog setContentView(View view) {
        mMessageContentView = view;
        if (mBuilder != null) {
            mBuilder.setContentView(mMessageContentView);
        }
        return this;
    }

    public MaterialDialog setBackground(Drawable drawable) {
        mBackgroundDrawable = drawable;
        if (mBuilder != null) {
            mBuilder.setBackground(mBackgroundDrawable);
        }
        return this;
    }

    public MaterialDialog setBackgroundResource(int resId) {
        mBackgroundResId = resId;
        if (mBuilder != null) {
            mBuilder.setBackgroundResource(mBackgroundResId);
        }
        return this;
    }

    public void dismiss() {
        mAlertDialog.dismiss();
    }

    private int dip2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public MaterialDialog setTitle(int resId) {
        mTitleResId = resId;
        if (mBuilder != null) {
            mBuilder.setTitle(resId);
        }
        return this;
    }

    public MaterialDialog setTitle(CharSequence title) {
        mTitle = title;
        if (mBuilder != null) {
            mBuilder.setTitle(title);
        }
        return this;
    }

    public MaterialDialog setMessage(int resId) {
        mMessageResId = resId;
        if (mBuilder != null) {
            mBuilder.setMessage(resId);
        }
        return this;
    }

    public MaterialDialog setMessage(CharSequence message) {
        mMessage = message;
        if (mBuilder != null) {
            mBuilder.setMessage(message);
        }
        return this;
    }

    public MaterialDialog setPositiveButton(int resId, final View.OnClickListener listener) {
        mPositiveButton = new Button(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mPositiveButton.setLayoutParams(params);
        mPositiveButton.setBackgroundResource(R.drawable.cube_dialog_button);
        mPositiveButton.setTextColor(Color.argb(255, 35, 159, 242));
        mPositiveButton.setText(resId);
        mPositiveButton.setGravity(Gravity.CENTER);
        mPositiveButton.setTextSize(14);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dip2px(2), 0, dip2px(12), dip2px(BUTTON_BOTTOM));
        mPositiveButton.setLayoutParams(layoutParams);
        mPositiveButton.setOnClickListener(listener);
        if (isLollipop()) {
            mPositiveButton.setBackgroundResource(android.R.color.transparent);
        }
        return this;
    }


    public MaterialDialog setPositiveButton(String text, final View.OnClickListener listener) {
        mPositiveButton = new Button(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mPositiveButton.setLayoutParams(params);
        mPositiveButton.setBackgroundResource(R.drawable.cube_dialog_button);
        mPositiveButton.setTextColor(Color.argb(255, 35, 159, 242));
        mPositiveButton.setText(text);
        mPositiveButton.setGravity(Gravity.CENTER);
        mPositiveButton.setTextSize(14);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dip2px(2), 0, dip2px(12), dip2px(BUTTON_BOTTOM));
        mPositiveButton.setLayoutParams(layoutParams);
        mPositiveButton.setOnClickListener(listener);
        if (isLollipop()) {
            mPositiveButton.setBackgroundResource(android.R.color.transparent);
        }
        return this;
    }

    public MaterialDialog setNegativeButton(int resId, final View.OnClickListener listener) {
        mNegativeButton = new Button(mContext);
        mLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mNegativeButton.setLayoutParams(mLayoutParams);
        mNegativeButton.setBackgroundResource(R.drawable.cube_dialog_button);
        mNegativeButton.setText(resId);
        mNegativeButton.setTextColor(Color.argb(222, 0, 0, 0));
        mNegativeButton.setTextSize(14);
        mNegativeButton.setGravity(Gravity.CENTER);
        mNegativeButton.setOnClickListener(listener);
        if (isLollipop()) {
            mNegativeButton.setBackgroundResource(android.R.color.transparent);
        }

        return this;
    }

    public MaterialDialog setNegativeButton(String text, final View.OnClickListener listener) {
        mNegativeButton = new Button(mContext);
        mLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mNegativeButton.setLayoutParams(mLayoutParams);
        mNegativeButton.setBackgroundResource(R.drawable.cube_dialog_button);
        mNegativeButton.setText(text);
        mNegativeButton.setTextColor(Color.argb(222, 0, 0, 0));
        mNegativeButton.setTextSize(14);
        mNegativeButton.setGravity(Gravity.CENTER);
        mNegativeButton.setOnClickListener(listener);
        if (isLollipop()) {
            mNegativeButton.setBackgroundResource(android.R.color.transparent);
        }

        return this;
    }

    public MaterialDialog setCanceledOnTouchOutside(boolean cancel) {
        this.mCancel = cancel;
        if (mBuilder != null) {
            mBuilder.setCanceledOnTouchOutside(mCancel);
        }
        return this;
    }

    public MaterialDialog setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
        return this;
    }


    private class Builder {

        private TextView mTitleView;
        private TextView mMessageView;
        private Window mAlertDialogWindow;
        private LinearLayout mButtonLayout;

        private Builder() {
            mAlertDialog = new AlertDialog.Builder(mContext).create();
            mAlertDialog.show();
            mAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            mAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            mAlertDialogWindow = mAlertDialog.getWindow();
            View contentView = LayoutInflater.from(mContext).inflate(R.layout.cube_material_dialog, null);
            contentView.setFocusable(true);
            contentView.setFocusableInTouchMode(true);

            mAlertDialogWindow.setBackgroundDrawableResource(R.drawable.cube_material_dialog_window);

            mAlertDialogWindow.setContentView(contentView);

            mTitleView = (TextView) mAlertDialogWindow.findViewById(R.id.cube_dialog_title);
            mMessageView = (TextView) mAlertDialogWindow.findViewById(R.id.cube_dialog_message);
            mButtonLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.cube_dialog_bottom);
            if (mView != null) {
                LinearLayout linearLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.cube_dialog_content_container);
                linearLayout.removeAllViews();
                linearLayout.addView(mView);
            }
            if (mTitleResId != 0) {
                setTitle(mTitleResId);
            }
            if (mTitle != null) {
                setTitle(mTitle);
            }
            if (mTitle == null && mTitleResId == 0) {
                mTitleView.setVisibility(View.GONE);
            }
            if (mMessageResId != 0) {
                setMessage(mMessageResId);
            }
            if (mMessage != null) {
                setMessage(mMessage);
            }
            if (mPositiveButton != null) {
                mButtonLayout.addView(mPositiveButton);
            }
            if (mLayoutParams != null && mNegativeButton != null) {
                if (mButtonLayout.getChildCount() > 0) {
                    mLayoutParams.setMargins(dip2px(12), 0, 0, dip2px(BUTTON_BOTTOM));
                    mNegativeButton.setLayoutParams(mLayoutParams);
                    mButtonLayout.addView(mNegativeButton, 1);
                } else {
                    mNegativeButton.setLayoutParams(mLayoutParams);
                    mButtonLayout.addView(mNegativeButton);
                }
            }
            if (mBackgroundResId != 0) {
                LinearLayout linearLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.material_background);
                linearLayout.setBackgroundResource(mBackgroundResId);
            }
            if (mBackgroundDrawable != null) {
                LinearLayout linearLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.material_background);
                linearLayout.setBackgroundDrawable(mBackgroundDrawable);
            }

            if (mMessageContentView != null) {
                this.setContentView(mMessageContentView);
            }
            mAlertDialog.setCanceledOnTouchOutside(mCancel);
            if (mOnDismissListener != null) {
                mAlertDialog.setOnDismissListener(mOnDismissListener);
            }
        }

        public void setTitle(int resId) {
            mTitleView.setText(resId);
        }

        public void setTitle(CharSequence title) {
            mTitleView.setText(title);
        }

        public void setMessage(int resId) {
            mMessageView.setText(resId);
        }

        public void setMessage(CharSequence message) {
            mMessageView.setText(message);
        }

        /**
         * set positive button
         *
         * @param text     the name of button
         * @param listener
         */
        public void setPositiveButton(String text, final View.OnClickListener listener) {
            Button button = new Button(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            button.setBackgroundResource(R.drawable.cube_material_card);
            button.setTextColor(Color.argb(255, 35, 159, 242));
            button.setText(text);
            button.setGravity(Gravity.CENTER);
            button.setTextSize(14);
            button.setPadding(dip2px(12), 0, dip2px(32), dip2px(BUTTON_BOTTOM));
            button.setOnClickListener(listener);
            mButtonLayout.addView(button);
        }

        /**
         * set negative button
         *
         * @param text     the name of button
         * @param listener
         */
        public void setNegativeButton(String text, final View.OnClickListener listener) {
            Button button = new Button(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            button.setLayoutParams(params);
            button.setBackgroundResource(R.drawable.cube_material_card);
            button.setText(text);
            button.setTextColor(Color.argb(222, 0, 0, 0));
            button.setTextSize(14);
            button.setGravity(Gravity.CENTER);
            button.setPadding(0, 0, 0, dip2px(8));
            button.setOnClickListener(listener);
            if (mButtonLayout.getChildCount() > 0) {
                params.setMargins(20, 0, 10, dip2px(BUTTON_BOTTOM));
                button.setLayoutParams(params);
                mButtonLayout.addView(button, 1);
            } else {
                button.setLayoutParams(params);
                mButtonLayout.addView(button);
            }
        }

        public void setView(View view) {
            LinearLayout l = (LinearLayout) mAlertDialogWindow.findViewById(R.id.cube_dialog_content_container);
            l.removeAllViews();
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(layoutParams);

            view.setOnFocusChangeListener(
                    new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            mAlertDialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            // show imm
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(
                                    InputMethodManager.SHOW_FORCED,
                                    InputMethodManager.HIDE_IMPLICIT_ONLY
                            );

                        }
                    }
            );

            l.addView(view);

            if (view instanceof ViewGroup) {

                ViewGroup viewGroup = (ViewGroup) view;

                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    if (viewGroup.getChildAt(i) instanceof EditText) {
                        EditText editText = (EditText) viewGroup.getChildAt(i);
                        editText.setFocusable(true);
                        editText.requestFocus();
                        editText.setFocusableInTouchMode(true);
                    }
                }
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    if (viewGroup.getChildAt(i) instanceof AutoCompleteTextView) {
                        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) viewGroup.getChildAt(i);
                        autoCompleteTextView.setFocusable(true);
                        autoCompleteTextView.requestFocus();
                        autoCompleteTextView.setFocusableInTouchMode(true);
                    }
                }
            }
        }

        public void setContentView(View contentView) {
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            contentView.setLayoutParams(layoutParams);
            LinearLayout linearLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.message_content_view);
            if (linearLayout != null) {
                linearLayout.removeAllViews();
                linearLayout.addView(contentView);
            }
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                if (linearLayout.getChildAt(i) instanceof AutoCompleteTextView) {
                    AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) linearLayout.getChildAt(i);
                    autoCompleteTextView.setFocusable(true);
                    autoCompleteTextView.requestFocus();
                    autoCompleteTextView.setFocusableInTouchMode(true);
                }
            }
        }

        public void setBackground(Drawable drawable) {
            LinearLayout linearLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.material_background);
            linearLayout.setBackgroundDrawable(drawable);
        }

        public void setBackgroundResource(int resId) {
            LinearLayout linearLayout = (LinearLayout) mAlertDialogWindow.findViewById(R.id.material_background);
            linearLayout.setBackgroundResource(resId);
        }


        public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
            mAlertDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        }
    }
}