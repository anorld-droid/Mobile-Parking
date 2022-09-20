package com.karanja.views.homefragments;


import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;


import com.karanja.R;

public class SettingsFragment extends PreferenceFragmentCompat {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);
    }

}
