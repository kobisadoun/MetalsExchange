package com.kobi.metalsexchange.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kobi.metalsexchange.app.data.MetalsContract;

import java.text.NumberFormat;

/**
 * {@link ExchangeRatesAdapter} exposes a list of metal exchange rates
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class ExchangeRatesAdapter extends RecyclerView.Adapter<ExchangeRatesAdapter.RatesAdapterViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_PAST_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;
    private String mMetalId;
    private Cursor mCursor;
    final private Context mContext;
    final private ExchangeRatesAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    /**
     * Cache of the children views for a rates list item.
     */
    public class RatesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        public final ImageButton refreshTodayView;
        public final ImageView iconView;
        public final ImageView deltaIconView;
        public final TextView dateView;
        public final TextView deltaView;
        public final TextView rateView;
        public final TextView rateUnitView;


        public RatesAdapterViewHolder(View view) {
            super(view);
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
//            refreshTodayView = (ImageButton) view.findViewById(R.id.list_item_today_refresh);
            deltaIconView = (ImageView) view.findViewById(R.id.list_item_delta_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            deltaView = (TextView) view.findViewById(R.id.list_item_delta_textview);
            rateView = (TextView) view.findViewById(R.id.list_item_rate_textview);
            rateUnitView = (TextView) view.findViewById(R.id.list_item_rate_unit_textview);
//            if(refreshTodayView != null) {
//                refreshTodayView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mClickHandler.onRefreshCurrent();
//                    }
//                });
//            }
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            int dateColumnIndex = mCursor.getColumnIndex(MetalsContract.MetalsRateEntry.COLUMN_DATE);
            mClickHandler.onClick(mCursor.getLong(dateColumnIndex), this);
        }
    }

    public static interface ExchangeRatesAdapterOnClickHandler {
        void onClick(Long date, RatesAdapterViewHolder vh);
        void onRefreshCurrent();
    }

    public ExchangeRatesAdapter(Context context, String metalId, ExchangeRatesAdapterOnClickHandler dh, View emptyView) {
        this.mMetalId = metalId;
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
    }

    @Override
    public RatesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Choose the layout type
        if ( viewGroup instanceof RecyclerView ) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    layoutId = R.layout.list_item_rate_today;
                    break;
                }
                case VIEW_TYPE_PAST_DAY: {
                    layoutId = R.layout.list_item_rate;
                    break;
                }
            }

            View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            return new RatesAdapterViewHolder(view);
        }
        else{
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(RatesAdapterViewHolder adapterViewHolder, int position) {

        mCursor.moveToPosition(position);
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                adapterViewHolder.iconView.setImageResource(Utility.getArtResourceForMetal(mMetalId));
                break;
            }
//            case VIEW_TYPE_PAST_DAY: {
//                viewHolder.iconView.setImageResource(Utility.getIconResourceForCurrency(
//                        Utility.getPreferredCurrency(context)));
//                break;
//            }
        }

        // Read date from cursor
        long dateInMillis = mCursor.getLong(ExchangeRatesFragment.COL_RATE_DATE);
        // Find TextView and set formatted date on it
        adapterViewHolder.dateView.setText(Utility.getFriendlyDayString(mContext, dateInMillis));

//
//        forecastAdapterViewHolder.deltaView.setText(description);

//        // For accessibility, add a content description to the icon field
//        forecastAdapterViewHolder.iconView.setContentDescription(description);

        // Read rate from cursor

        double rateRaw = mCursor.getDouble(Utility.getPreferredCurrencyColumnId(Utility.getPreferredCurrency(mContext)));
        String rate = Utility.getFormattedCurrency( rateRaw, Utility.getPreferredCurrency(mContext), mContext, true);
        adapterViewHolder.rateView.setText(rate);
        adapterViewHolder.rateUnitView.setText("("+Utility.getWeightName(Utility.isGrams(mContext), mContext)+")");

        //in order to calculate the delta we should get the cursor location for one before
        if(!mCursor.isLast() && mCursor.moveToNext()){
            double previousRateRaw = mCursor.getDouble(Utility.getPreferredCurrencyColumnId(Utility.getPreferredCurrency(mContext)));
            double rateDeltaRaw = rateRaw - previousRateRaw;
            double rateDeltaRawPercentage = 0;
            String rateDeltaRawPercentageStr="";
            NumberFormat defaultFormat = NumberFormat.getPercentInstance();
            defaultFormat.setMaximumFractionDigits(2);
            defaultFormat.setMinimumFractionDigits(2);
            int imageIconResourceId = 0;
            int imageArtResourceId = 0;
            if(rateDeltaRaw < 0){
                imageIconResourceId = R.drawable.ic_down;
                imageArtResourceId = R.drawable.art_down;
                rateDeltaRawPercentage = rateDeltaRaw/previousRateRaw;
                rateDeltaRawPercentage *= -1;

            } else if(rateDeltaRaw > 0){
                imageIconResourceId = R.drawable.ic_up;
                imageArtResourceId = R.drawable.art_down;
                rateDeltaRawPercentage = rateDeltaRaw/rateRaw;
            } else{
                imageIconResourceId = R.drawable.ic_same;
                imageArtResourceId = R.drawable.art_down;
            }

            switch (viewType) {
                case VIEW_TYPE_TODAY: {
                    adapterViewHolder.deltaIconView.setImageResource(imageArtResourceId);
                    break;
                }
                case VIEW_TYPE_PAST_DAY: {
                    adapterViewHolder.deltaIconView.setImageResource(imageIconResourceId);
                    break;
                }
            }


            rateDeltaRawPercentageStr = defaultFormat.format(rateDeltaRawPercentage);

          //  if(rateDeltaRaw != 0) {
               // String rateDelta = Utility.getFormattedCurrency(Math.abs(rateDeltaRaw), Utility.getPreferredCurrency(mContext), mContext, true);
                if (adapterViewHolder.deltaView != null) {
                    adapterViewHolder.deltaView.setText(rateDeltaRawPercentageStr);
                }
          //  }
        }

    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_PAST_DAY;
    }

    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }
}