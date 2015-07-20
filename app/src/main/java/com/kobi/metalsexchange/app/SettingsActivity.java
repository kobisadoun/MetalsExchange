/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kobi.metalsexchange.app;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.kobi.metalsexchange.app.component.NumberPickerPreference;
import com.kobi.metalsexchange.app.data.MetalsContract;

import java.util.Set;

public class SettingsActivity extends PreferenceActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(false);
            getActionBar().setDisplayShowTitleEnabled(true);
            getActionBar().setDisplayUseLogoEnabled(false);
        }

    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_container);

            PreferenceCategory fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_general_header);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_general);

            fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_currencies_header);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_currencies);


            fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_notifications_header);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_notifications);


            fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_data_sync_header);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_data_sync);

            fakeHeader = new PreferenceCategory(getActivity());
            fakeHeader.setTitle(R.string.pref_advanced_header);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_advanced);

            // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
            // updated when the preference changes.

            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_main_currency_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));

            NumberPickerPreference historyCountPreference =  (NumberPickerPreference)findPreference(getString(R.string.pref_history_business_days_key));
            historyCountPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object value) {
                    preference.setSummary(value + "");
                    Utility.setSyncAllAvailableDataOfThisYear(getActivity(), true);
                    getActivity().getApplicationContext().getContentResolver().notifyChange(MetalsContract.MetalsRateEntry.CONTENT_URI, null);
                    preference.setSummary(value + "");
                    return true;
                }
            });
            historyCountPreference.setSummary(Utility.getHistoryCount(getActivity())+"");


//        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sync_frequency_key)));
//
//        Preference syncEnabledPref = (Preference) findPreference(getString(R.string.pref_enable_sync_key));
//        syncEnabledPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object value) {
//                boolean syncEnabled = Boolean.valueOf( value.toString());
//
//                Account account = MetalsExchangeSyncAdapter.getSyncAccount(getActivity());
//                if(!syncEnabled) {
//                    ContentResolver.cancelSync(account, getActivity().getString(R.string.content_authority));
//                    ContentResolver.setSyncAutomatically(account, getActivity().getString(R.string.content_authority), false);
//                }
//                else{
//                    ContentResolver.setSyncAutomatically(account, getActivity().getString(R.string.content_authority), true);
//                    MetalsExchangeSyncAdapter.syncImmediately(getActivity(), false);
//                }
//                return true;
//            }
//        });
//
//
//        Preference syncFrequencyPref = (Preference) findPreference(getString(R.string.pref_sync_frequency_key));
//        syncFrequencyPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            public boolean onPreferenceChange(Preference preference, Object value) {
//                int syncInterval = Integer.valueOf((String) value);
//                MetalsExchangeSyncAdapter.configurePeriodicSync(getActivity(), syncInterval, syncInterval / 3);
//
//                // For list preferences, look up the correct display value in
//                // the preference's 'entries' list (since they have separate labels/values).
//                ListPreference listPreference = (ListPreference) preference;
//                int prefIndex = listPreference.findIndexOfValue((String) value);
//                if (prefIndex >= 0) {
//                    preference.setSummary(listPreference.getEntries()[prefIndex]);
//                }
//                return true;
//            }
//        });


            final MultiSelectListPreference otherCurrenciesListPreference = (MultiSelectListPreference) findPreference(getString(R.string.pref_main_other_currencies_key));
            Dialog d = otherCurrenciesListPreference.getDialog();
            otherCurrenciesListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()

                                                                        {
                                                                            public boolean onPreferenceChange (Preference preference, Object value){
                                                                                Set<String> selectedCurrencies = (Set<String>) value;
                                                                                if (selectedCurrencies.size() > 3) {
                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                                                    builder.setTitle(otherCurrenciesListPreference.getTitle())
                                                                                            .setMessage(otherCurrenciesListPreference.getSummary());

                                                                                    AlertDialog welcomeAlert = builder.create();
                                                                                    welcomeAlert.show();
                                                                                    return false;
                                                                                }
                                                                                getActivity().getApplicationContext().getContentResolver().notifyChange(MetalsContract.MetalsRateEntry.CONTENT_URI, null);
                                                                                return true;
                                                                            }
                                                                        }

            );

        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Utility.resetRatesStatus(getActivity());
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
            getActivity().getApplicationContext().getContentResolver().notifyChange(MetalsContract.MetalsRateEntry.CONTENT_URI, null);

            return true;
        }

    }
}