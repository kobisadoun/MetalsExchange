package com.kobi.metalsexchange.app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ExchangeRatesFragmentPagerAdapter extends FragmentPagerAdapter /*FragmentStatePagerAdapter*/ {
    private Context context;

    public ExchangeRatesFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return Utility.METALS_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return ExchangeRatesFragment.newInstance(Utility.getMetalIdForTabPosition(position));
    }


    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        //Drawable image = context.getResources().getDrawable(Utility.getIconResourceForMetal(Utility.getMetalIdForTabPosition(position)));
        //image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        // Replace blank spaces with image icon
        //SpannableString sb = new SpannableString(Utility.getMetalName(Utility.getMetalIdForTabPosition(position), context));
       // ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        //sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return Utility.getMetalName(Utility.getMetalIdForTabPosition(position), context);
    }

    public int getPageIcon(int position) {
        return Utility.getIconResourceForMetal(Utility.getMetalIdForTabPosition(position));
    }


}