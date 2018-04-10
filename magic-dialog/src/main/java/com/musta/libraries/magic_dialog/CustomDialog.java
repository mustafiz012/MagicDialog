package com.musta.libraries.magic_dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CustomDialog extends AlertDialog implements OnDismissListener {

    private enum ChoiceMode {
        NONE,
        SINGLE,
        MULTI
    }

    private ChoiceMode mChoiceMode = ChoiceMode.NONE;

    private Drawable mIcon = null;
    private CharSequence mTitle = null;
    private View mCustomTitleView = null;
    protected CharSequence mMessage = null;
    protected View mView = null;
    private ListView mListView = null;
    private View mListEmptyView = null;
    private TextView mMessageView = null;

    private LinearLayout mTitleLayout = null;
    private ImageView mTitleImageView = null;
    private TextView mTitleTextView = null;

    private CharSequence[] mItems = null;
    private ListAdapter mAdapter = null;
    private Cursor mCursor = null;
    private String mIsCheckedColumn = null;
    private String mLabelColumn = null;

    private int mCheckedItem = 0;
    private boolean[] mCheckedItems = null;

    private OnClickListener mListClickListener = null;
    private OnMultiChoiceClickListener mListMultiChoiceClickListener = null;
    private CharSequence mPositiveButtonText = null;
    private OnClickListener mPositiveButtonClickListener = null;
    private CharSequence mNegativeButtonText = null;
    private OnClickListener mNegativeButtonClickListener = null;
    private CharSequence mNeutralButtonText = null;
    private OnClickListener mNeutralButtonClickListener = null;
    private int showCancelAfterSec = 0;

    private Object mTag = null;

    static private int checkShow = 0;

    public boolean isProcessing = false;
    public boolean dividerFlag = true;

    @Override
    public void dismiss() {
        checkShow--;
        isProcessing = false;
        Log.d("CustomDialog", "dismiss()" + checkShow);
        try {
            Thread.sleep(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.dismiss();
    }

    @Override
    public void show() {
        checkShow++;
        isProcessing = true;
        Log.d("CustomDialog", "show()" + checkShow);
        try {
            Thread.sleep(-1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.show();

    }

    private final View.OnClickListener mInternalButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int which = (Integer) v.getTag();

            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (mPositiveButtonClickListener != null) {
                        mPositiveButtonClickListener.onClick(CustomDialog.this, which);
                    } else {
                        dismiss();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    if (mNegativeButtonClickListener != null) {
                        mNegativeButtonClickListener.onClick(CustomDialog.this, which);
                    } else {
                        dismiss();
                    }
                    break;

                case DialogInterface.BUTTON_NEUTRAL:
                    if (mNeutralButtonClickListener != null) {
                        mNeutralButtonClickListener.onClick(CustomDialog.this, which);
                    } else {
                        dismiss();
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unknown which " + which);
            }
        }
    };

    private final OnItemClickListener mInternalItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (mChoiceMode) {
                case NONE:
                case SINGLE:
                    if (mListClickListener != null) {
                        mListClickListener.onClick(CustomDialog.this, position);
                    }
                    break;

                case MULTI:
                    if (mListMultiChoiceClickListener != null) {
                        mListMultiChoiceClickListener.onClick(CustomDialog.this, position, mListView.isItemChecked(position));
                    }
                    break;
            }
        }
    };

    private class InternalAdapter extends ArrayAdapter<CharSequence> {
        public InternalAdapter(Context context) {
            super(context, R.layout.simple_list_item_multi_choice, mItems);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            boolean isChecked = mCheckedItems[position];

            if (isChecked) {
                mListView.setItemChecked(position, true);
            }

            return view;
        }
    }

    private class InternalCursorAdapter extends CursorAdapter {
        public InternalCursorAdapter(Context context, Cursor cursor) {
            super(context, cursor, true);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            CheckedTextView text = null;
            if (view instanceof CheckedTextView) {
                text = (CheckedTextView) view;
                text.setText(cursor.getString(cursor.getColumnIndex(mLabelColumn)));
            }
            boolean isChecked = (cursor.getInt(cursor.getColumnIndex(mIsCheckedColumn)) == 1);
            mListView.setItemChecked(cursor.getPosition(), isChecked);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.simple_list_item_multi_choice, parent, false);
        }
    }

    public CustomDialog(Context context) {
        // Caution !!! Don't change the Style
        super(context, R.style.Theme_Default_CustomDialog);
    }

    public CustomDialog(Context context, int theme) {
        // Caution !!! Don't change the Style
//    	super(context, theme);
        super(context, R.style.Theme_Default_CustomDialog);
    }

    /**
     * badsha.a
     * only applicable for BTinitSearchDialog
     */
    public CustomDialog(Context context, int theme, boolean initSearchDialog) {
        // Caution !!! Don't change the Style
//    	super(context, theme);
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCanceledOnTouchOutside(false);
        setOnDismissListener(this);

        initialize();
    }

    private void initialize() {
        setContentView(R.layout.dialog_custom);

        boolean titleLayoutVisible = initTitleLayout();
        boolean messageLayoutVisible = initMessageLayout();
        boolean buttonLayoutVisible = initButtonLayout();
    }

    private boolean initTitleLayout() {
        boolean isVisible = false;
        mTitleLayout = findViewById(R.id.title_layout);
        mTitleImageView = findViewById(R.id.icon);
        mTitleTextView = findViewById(R.id.title);

        if (mCustomTitleView != null) {
            mTitleLayout.addView(mCustomTitleView);
            isVisible = true;
        } else {
            if (mIcon != null) {
                mTitleImageView.setImageDrawable(mIcon);
                mTitleImageView.setVisibility(View.GONE);
                isVisible = true;
            }

            if (mTitle != null) {
                mTitleTextView.setText(mTitle);
                mTitleTextView.setVisibility(View.VISIBLE);
                isVisible = true;
            }
        }

        if (isVisible) {
            /*
             * if(CMService.ACTIVE_SERVICE == CMConstants.SubAppType.APP_ML){
             * mTitleLayout.setBackgroundResource(R.drawable.manager_dialog_top_medium);
             * mTitleTextView.setBackgroundResource(R.drawable.manager_dialog_title);
             * }
             */
            mTitleLayout.setVisibility(View.VISIBLE);
        }

        return isVisible;
    }

    private boolean initMessageLayout() {
        boolean isVisible = false;
        ViewGroup messageBackgroundLayout = findViewById(R.id.message_background_layout);
        LinearLayout messageLayout = findViewById(R.id.message_layout);

        if (mChoiceMode == ChoiceMode.MULTI) {
            if (mItems != null) {
                mAdapter = new InternalAdapter(getContext());
            } else if (mCursor != null) {
                mAdapter = new InternalCursorAdapter(getContext(), mCursor);
            }
        } else {
            int layout = (mChoiceMode == ChoiceMode.SINGLE) ? R.layout.custom_list_item_single_choice : R.layout.simple_list_item;

            if (mItems != null) {
                mAdapter = new MySimpleArrayAdapter(getContext(), mItems);
            } else if (mCursor != null) {
                mAdapter = new SimpleCursorAdapter(getContext(), layout, mCursor, new String[]{mLabelColumn}, new int[]{android.R.id.text1});
            }
        }

        mListView = findViewById(R.id.list_view);

        if (mAdapter == null) {
            mListView.setVisibility(View.GONE);
        } else {
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(mInternalItemClickListener);

            if (mChoiceMode == ChoiceMode.SINGLE) {
                mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                mListView.setItemChecked(mCheckedItem, true);
                mListView.setSelection(mCheckedItem);
            } else if (mChoiceMode == ChoiceMode.MULTI) {
                mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            }

            if (mListEmptyView != null) {
                mListView.setEmptyView(mListEmptyView);
                messageBackgroundLayout.addView(mListEmptyView);
            }

            isVisible = true;
        }

        if (mMessage != null) {
            mMessageView = findViewById(R.id.message);
            mMessageView.setText(mMessage);
            mMessageView.setTextColor(getContext().getResources().getColor(R.color.color_app_252525));
            mMessageView.setVisibility(View.VISIBLE);
            isVisible = true;
        }

        if (mView != null) {
            messageLayout.addView(mView);
            isVisible = true;
        }

        if (isVisible) {
            messageBackgroundLayout.setVisibility(View.VISIBLE);
        }

        return isVisible;
    }

    public void setPositiveButtonText(CharSequence text) {
        mPositiveButtonText = text;
        TextView positiveButton = findViewById(R.id.positive_button);
        positiveButton.setText(mPositiveButtonText);
        positiveButton.setContentDescription(mPositiveButtonText + positiveButton.getResources().getString(R.string.SS_BUTTON_T_TTS));
    }

    /*
      @author: asad.sabuj
      text color dim
     */
    public void setPositiveButtonEnable(boolean state) {
        TextView positiveButton = findViewById(R.id.positive_button);
        if (state)
            positiveButton.setTextColor(Color.parseColor("#e09721"));
        else
            positiveButton.setTextColor(Color.parseColor("#e09721"));

        positiveButton.setEnabled(state);

    }

    /*@author:badsha.a
     * set Positive Button Clickable and Focusable and change textcolor accordingly
     * @param:boolean
     * */
    public void setPositiveButtonClickableAndFocusable(boolean state) {
        TextView positiveButton = findViewById(R.id.positive_button);
        positiveButton.setClickable(state);
        positiveButton.setFocusable(state);
        if (state)
            positiveButton.setTextColor(Color.parseColor("#e09721"));
        else
            positiveButton.setTextColor(Color.parseColor("#e09721"));
    }

    public CharSequence getPositiveButtonText() {
        return mPositiveButtonText;
    }

    private boolean initButtonLayout() {
        boolean isVisible = false;
        final LinearLayout buttonLayout = findViewById(R.id.button_layout);

        LinearLayout.LayoutParams params = null;
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.custom_dialog_button_margin);
        margin = 0;
        if (mPositiveButtonText != null) {
            TextView positiveButton = findViewById(R.id.positive_button);
            positiveButton.setTag(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setText(mPositiveButtonText);
            positiveButton.setContentDescription(mPositiveButtonText + positiveButton.getResources().getString(R.string.SS_BUTTON_T_TTS));
            if (getContext().getResources().getConfiguration().fontScale > 1.2) {
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);    // 16 * 1.2 = 19.2
            } else {
                positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            }
            positiveButton.setOnClickListener(mInternalButtonClickListener);

            // params = (LinearLayout.LayoutParams)positiveButton.getLayoutParams();
            // params.setMargins(margin, 0, margin, 0);
            // positiveButton.setLayoutParams(params);

            positiveButton.setVisibility(View.VISIBLE);
            isVisible = true;

            /*AddShowButtonShape.getInstance().addToTextView(positiveButton,
                    getContext().getDrawable(R.drawable.ripple),
                    getContext().getDrawable(R.drawable.ripple_effect_rect_button_shape));*/

        }
        if (mNeutralButtonText != null && mPositiveButtonText == null) {
            final TextView neutralButton = findViewById(R.id.neutral_button);
            neutralButton.setTag(DialogInterface.BUTTON_NEUTRAL);
            neutralButton.setText(mNeutralButtonText);
            if (getContext().getResources().getConfiguration().fontScale > 1.2) {
                neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);    // 16 * 1.2 = 19.2
            } else {
                neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            }
            neutralButton.setOnClickListener(mInternalButtonClickListener);

            params = (LinearLayout.LayoutParams) neutralButton.getLayoutParams();
            params.setMargins(0, 32, 24, 64);
            neutralButton.setLayoutParams(params);


            if (showCancelAfterSec >= 2000) {
                if (mNeutralButtonText != null && mNeutralButtonText.equals(getContext().getText(R.string.TS_CANCEL_ACBUTTON3))) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (neutralButton != null) {
                                neutralButton.setVisibility(View.VISIBLE);
                            }
                            if (buttonLayout != null) {
                                buttonLayout.setVisibility(View.VISIBLE);
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
                                params.setMargins(0, -25, 24, 0);
                                buttonLayout.setLayoutParams(params);
                            }
                        }
                    };
                    // Show after some seconds
                    handler.postDelayed(runnable, showCancelAfterSec);
                }
            } else {
                neutralButton.setVisibility(View.VISIBLE);
                isVisible = true;
            }

            /*AddShowButtonShape.getInstance().addToTextView(neutralButton,
                    getContext().getDrawable(R.drawable.ripple),
                    getContext().getDrawable(R.drawable.ripple_effect_rect_button_shape));*/

        }
        if (mNeutralButtonText != null && mPositiveButtonText != null) {
            final TextView neutralButton = findViewById(R.id.neutral_button);
            neutralButton.setTag(DialogInterface.BUTTON_NEUTRAL);
            neutralButton.setText(mNeutralButtonText);
            if (getContext().getResources().getConfiguration().fontScale > 1.2) {
                neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);    // 16 * 1.2 = 19.2
            } else {
                neutralButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            }
            neutralButton.setOnClickListener(mInternalButtonClickListener);


//            params = (LinearLayout.LayoutParams)neutralButton.getLayoutParams();
//            params.setMargins(0, 0, 24, 0);
//            neutralButton.setLayoutParams(params);
//            

            if (showCancelAfterSec >= 2000) {
                if (mNeutralButtonText != null && mNeutralButtonText.equals(getContext().getText(R.string.TS_CANCEL_ACBUTTON3))) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (neutralButton != null) {
                                neutralButton.setVisibility(View.VISIBLE);
                            }
                            if (buttonLayout != null) {
                                buttonLayout.setVisibility(View.VISIBLE);
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) buttonLayout.getLayoutParams();
                                params.setMargins(0, -25, 0, 0);
                                buttonLayout.setLayoutParams(params);
                            }
                        }
                    };
                    // Show after some seconds
                    handler.postDelayed(runnable, showCancelAfterSec);
                }
            } else {
                neutralButton.setVisibility(View.VISIBLE);
                isVisible = true;
            }

            /*AddShowButtonShape.getInstance().addToTextView(neutralButton,
                    getContext().getDrawable(R.drawable.ripple),
                    getContext().getDrawable(R.drawable.ripple_effect_rect_button_shape));*/

        }

        if (mNegativeButtonText != null) {
            TextView negativeButton = findViewById(R.id.negative_button);
            negativeButton.setTag(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setText(mNegativeButtonText);
            negativeButton.setContentDescription(mNegativeButtonText + negativeButton.getResources().getString(R.string.SS_BUTTON_T_TTS));
            if (getContext().getResources().getConfiguration().fontScale > 1.2) {
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);    // 16 * 1.2 = 19.2
            } else {
                negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            }
            negativeButton.setOnClickListener(mInternalButtonClickListener);

            negativeButton.setVisibility(View.VISIBLE);
            isVisible = true;
            /*AddShowButtonShape.getInstance().addToTextView(negativeButton,
                    getContext().getDrawable(R.drawable.ripple),
                    getContext().getDrawable(R.drawable.ripple_effect_rect_button_shape));*/

        }

        if (isVisible) {
            buttonLayout.setVisibility(View.VISIBLE);
        } else {
            buttonLayout.setVisibility(View.INVISIBLE);
        }

        return isVisible;
    }

    public void hideCancel() {
        findViewById(R.id.neutral_button).setVisibility(View.INVISIBLE);
    }

    Handler handler = new Handler();

    private void refreshTitleLayout() {
        if (mCustomTitleView == null && mTitleLayout != null) {
            boolean isVisible = false;

            if (mIcon != null && mTitleImageView != null) {
                mTitleImageView.setImageDrawable(mIcon);
                mTitleImageView.setVisibility(View.GONE);
                isVisible = true;
            }

            if (mTitle != null && mTitleTextView != null) {
                mTitleTextView.setText(mTitle);
                mTitleTextView.setVisibility(View.VISIBLE);
                isVisible = true;
            }

            if (isVisible) {
                mTitleLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStart() {
        if (mAdapter != null && mAdapter.getCount() > 0) {
            if (mListEmptyView != null && mListEmptyView.getVisibility() == View.VISIBLE) {
                mListView.setVisibility(View.VISIBLE);
                mListEmptyView.setVisibility(View.GONE);
            }
        } else {
            if (mListEmptyView != null && mListEmptyView.getVisibility() == View.GONE) {
                mListView.setVisibility(View.GONE);
                mListEmptyView.setVisibility(View.VISIBLE);
            }
        }

        super.onStart();
    }

    @Override
    public void setIcon(int resID) {
        mIcon = getContext().getResources().getDrawable(resID);
    }

    @Override
    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    @Override
    public void setTitle(int titleID) {
        mTitle = getContext().getText(titleID);
        refreshTitleLayout(); // infi_cjlee, 140519, dialog oncreate  setTitle API
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        refreshTitleLayout(); // infi_cjlee, 140519, dialog oncreate  setTitle API
    }

    public void setCustomTitle(int layoutResID) {
        mCustomTitleView = getLayoutInflater().inflate(layoutResID, null);
    }

    @Override
    public void setCustomTitle(View view) {
        mCustomTitleView = view;
    }

    public void setMessage(int messageID) {
        mMessage = getContext().getText(messageID);
    }

    @Override
    public void setMessage(CharSequence message) {
        mMessage = message;
        if (mMessageView != null) {
            mMessageView.setText(message);
        }
    }

    public void setItems(int itemsId, OnClickListener listener) {
        mItems = getContext().getResources().getTextArray(itemsId);
        mListClickListener = listener;
        mChoiceMode = ChoiceMode.NONE;
    }

    public void setItems(CharSequence[] items, OnClickListener listener) {
        mItems = items;
        mListClickListener = listener;
        mChoiceMode = ChoiceMode.NONE;
    }

    public void setAdapter(ListAdapter adapter, OnClickListener listener) {
        mAdapter = adapter;
        mListClickListener = listener;
    }

    public void setCursor(Cursor cursor, String labelColumn, OnClickListener listener) {
        mCursor = cursor;
        mLabelColumn = labelColumn;
        mListClickListener = listener;
    }

    public void setSingleChoiceItems(int itemsId, int checkedItem, OnClickListener listener) {
        mItems = getContext().getResources().getTextArray(itemsId);
        mListClickListener = listener;
        mCheckedItem = checkedItem;
        mChoiceMode = ChoiceMode.SINGLE;
    }

    public void setSingleChoiceItems(CharSequence[] items, int checkedItem, OnClickListener listener) {
        mItems = items;
        mCheckedItem = checkedItem;
        mListClickListener = listener;
        mChoiceMode = ChoiceMode.SINGLE;
    }

    public void setSingleChoiceItems(ListAdapter adapter, int checkedItem, OnClickListener listener) {
        mAdapter = adapter;
        mCheckedItem = checkedItem;
        mListClickListener = listener;
        mChoiceMode = ChoiceMode.SINGLE;
    }

    public void setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, OnClickListener listener) {
        mCursor = cursor;
        mCheckedItem = checkedItem;
        mLabelColumn = labelColumn;
        mListClickListener = listener;
        mChoiceMode = ChoiceMode.SINGLE;
    }

    public int getChoiceItem() {
        return mCheckedItem;
    }

    public void setChoiceItem(int checkedItem) {
        mCheckedItem = checkedItem;
    }

    public void setMultiChoiceItems(int itemsId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
        mItems = getContext().getResources().getTextArray(itemsId);
        mCheckedItems = checkedItems;
        mListMultiChoiceClickListener = listener;
        mChoiceMode = ChoiceMode.MULTI;
    }

    public void setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
        mItems = items;
        mCheckedItems = checkedItems;
        mListMultiChoiceClickListener = listener;
        mChoiceMode = ChoiceMode.MULTI;
    }

    public void setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, OnMultiChoiceClickListener listener) {
        mCursor = cursor;
        mIsCheckedColumn = isCheckedColumn;
        mLabelColumn = labelColumn;
        mListMultiChoiceClickListener = listener;
        mChoiceMode = ChoiceMode.MULTI;
    }

    public void setListEmptyView(int layoutResID) {
        mListEmptyView = getLayoutInflater().inflate(layoutResID, null);
    }

    public void setListEmptyView(View view) {
        mListEmptyView = view;
    }

    @Override
    public ListView getListView() {
        return mListView;
    }

    public void setView(int layoutResID) {
        mView = getLayoutInflater().inflate(layoutResID, null);
    }

    @Override
    public void setView(View view) {
        mView = view;
    }

    /**
     * Get Custom Dialog View
     *
     * @return mView
     */
    public View getView() {
        return mView;
    }

    public void setPositiveButton(int textID, final OnClickListener listener) {
        mPositiveButtonText = getContext().getText(textID);
        mPositiveButtonClickListener = listener;

    }

    public void setPositiveButton(CharSequence text, final OnClickListener listener) {
        mPositiveButtonText = text;
        mPositiveButtonClickListener = listener;
    }

    public void setNeutralButton(int textID, final OnClickListener listener) {
        mNeutralButtonText = getContext().getText(textID);
        mNeutralButtonClickListener = listener;
    }

    public void setNeutralButton(int textID, final OnClickListener listener, int sec) {
        mNeutralButtonText = getContext().getText(textID);
        mNeutralButtonClickListener = listener;
        showCancelAfterSec = sec;
    }

    public TextView getNeutralButton() {
        TextView neutralButton = findViewById(R.id.neutral_button);
        if (neutralButton != null) {
            return neutralButton;
        } else {
            return null;
        }
    }

    public void setNeutralButton(CharSequence text, final OnClickListener listener) {
        mNeutralButtonText = text;
        mNeutralButtonClickListener = listener;
    }

    public void setNegativeButton(int textID, final OnClickListener listener) {
        mNegativeButtonText = getContext().getText(textID);
        mNegativeButtonClickListener = listener;
    }

    public void setNegativeButton(CharSequence text, final OnClickListener listener) {
        mNegativeButtonText = text;
        mNegativeButtonClickListener = listener;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    public static class InteractiveScrollView extends ScrollView {
        private enum ScrollState {
            TOP_SCROLL,
            BOTTOM_SCROLL,
            NORAML_SCROLL,
            NONE
        }

        private OnTopReachedListener mOnTopReachedListener = null;
        private OnBottomReachedListener mOnBottomReachedListener = null;
        private OnNormalScrollListener mOnNormalScrollListener = null;
        private ScrollState mScrollState = ScrollState.NONE;

        public InteractiveScrollView(Context context) {
            super(context);
        }

        public InteractiveScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public InteractiveScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            View mView = getChildAt(getChildCount() - 1);
            int mDistance = (mView.getBottom() - (t + getHeight()));

            if (mDistance <= 10 && mOnBottomReachedListener != null) {
                if (mScrollState != ScrollState.BOTTOM_SCROLL) {
                    mOnBottomReachedListener.onBottomReached();
                    mScrollState = ScrollState.BOTTOM_SCROLL;
                }
            } else if (t <= 10 && mOnTopReachedListener != null) {
                if (mScrollState != ScrollState.TOP_SCROLL) {
                    mOnTopReachedListener.onTopReached();
                    mScrollState = ScrollState.TOP_SCROLL;
                }
            } else {
                if (mScrollState != ScrollState.NORAML_SCROLL) {
                    if (mOnNormalScrollListener != null) {
                        mOnNormalScrollListener.onNormalScroll();
                    }
                }
                mScrollState = ScrollState.NORAML_SCROLL;
            }

            super.onScrollChanged(l, t, oldl, oldt);
        }

        /**
         * Event Listener Interface
         */
        public interface OnTopReachedListener {
            void onTopReached();
        }

        public interface OnBottomReachedListener {
            void onBottomReached();
        }


        public interface OnNormalScrollListener {
            void onNormalScroll();
        }

    }

    public class MySimpleArrayAdapter extends ArrayAdapter<CharSequence> {
        private Context context;
        private CharSequence[] values;

        public MySimpleArrayAdapter(Context context, CharSequence[] values) {
            super(context, R.layout.custom_list_item_single_choice, values);
            this.context = context;
            this.values = values;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.custom_list_item_single_choice, parent, false);
            TextView textView = rowView.findViewById(R.id.list_view_textView);
            ImageView imageView = rowView.findViewById(R.id.list_view_radio_button);
            textView.setText(values[position].toString());
            if (position == mCheckedItem) {
                imageView.setImageResource(R.drawable.rvf_check_on);
            } else {
                imageView.setImageResource(R.drawable.rvf_check_off);
            }
            return rowView;
        }
    }
}