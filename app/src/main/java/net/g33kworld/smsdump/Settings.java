package net.g33kworld.smsdump;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Settings extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Theme must be set before super.onCreate() is called
        if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("darkTheme", false)) {
            setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).getBoolean("darkTheme", false)) {
                getActivity().setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat);
            }

            //Load preferences from XML resource
            addPreferencesFromResource(R.xml.prefs);
        }
    }
}