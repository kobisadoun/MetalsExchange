package com.android.metalsexchange.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ExchangeRatesAdapter} exposes a list of metal exchange rates
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class ExchangeRatesAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;
    private String mMetalId;

    /**
     * Cache of the children views for a rates list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;
        public final ImageView deltaIconView;
        public final TextView dateView;
        public final TextView deltaView;
        public final TextView rateView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            deltaIconView = (ImageView) view.findViewById(R.id.list_item_delta_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            deltaView = (TextView) view.findViewById(R.id.list_item_delta_textview);
            rateView = (TextView) view.findViewById(R.id.list_item_rate_textview);
        }
    }

    public ExchangeRatesAdapter(Context context, Cursor c, int flags, String metalId) {
        super(context, c, flags);
        this.mMetalId = metalId;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_rate_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_rate;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                viewHolder.iconView.setImageResource(Utility.getArtResourceForMetal(mMetalId));
                break;
            }
//            case VIEW_TYPE_FUTURE_DAY: {
//                viewHolder.iconView.setImageResource(Utility.getIconResourceForCurrency(
//                        Utility.getPreferredCurrency(context)));
//                break;
//            }
        }



        // Read date from cursor
        long dateInMillis = cursor.getLong(ExchangeRatesFragment.COL_RATE_DATE);
        // Find TextView and set formatted date on it
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

//
//        viewHolder.deltaView.setText(description);

//        // For accessibility, add a content description to the icon field
//        viewHolder.iconView.setContentDescription(description);

        // Read rate from cursor

        double rateRaw = cursor.getDouble(Utility.getPreferredCurrencyColumnId(Utility.getPreferredCurrency(context)));
        String rate = Utility.getFormattedCurrency( rateRaw, Utility.getPreferredCurrency(context), context, true);
        viewHolder.rateView.setText(rate);

        //in order to calculate the delta we should get the cursor location for one before
        if(!cursor.isLast() && cursor.moveToNext()){
            double previousRateRaw = cursor.getDouble(Utility.getPreferredCurrencyColumnId(Utility.getPreferredCurrency(context)));
            double rateDeltaRaw = rateRaw - previousRateRaw;
            if(rateDeltaRaw < 0){
                viewHolder.deltaIconView.setImageResource(R.drawable.ic_down);
            } else if(rateDeltaRaw > 0){
                viewHolder.deltaIconView.setImageResource(R.drawable.ic_up);
            } else{
                viewHolder.deltaIconView.setImageResource(R.drawable.ic_same);
            }

            if(rateDeltaRaw != 0) {
                String rateDelta = Utility.getFormattedCurrency(Math.abs(rateDeltaRaw), Utility.getPreferredCurrency(context), context, true);
                if (viewHolder.deltaView != null) {
                    viewHolder.deltaView.setText(rateDelta);
                }
            }
        }

    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}