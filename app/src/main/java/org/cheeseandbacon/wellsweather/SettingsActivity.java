package org.cheeseandbacon.wellsweather;


import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public static final String KEY_PREF_CHECKBOX_LOCATION = "checkbox_location";
    public static final String KEY_PREF_EDITTEXT_MANUAL_LOCATION = "edittext_manual_location";
    public static final String KEY_PREF_LIST_UNITS = "list_units";

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.toolbar_fragment_container,
                new GeneralPreferenceFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Uncomment this to add an options menu
        //getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_test:
                //
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(KEY_PREF_CHECKBOX_LOCATION);
            final EditTextPreference editTextPreference = (EditTextPreference) findPreference(KEY_PREF_EDITTEXT_MANUAL_LOCATION);
            final PreferenceGroup editTextParent = getPreferenceGroup(getPreferenceScreen(), editTextPreference);

            if (editTextPreference != null && editTextParent != null) {
                if (checkBoxPreference.isChecked()) {
                    editTextParent.addPreference(editTextPreference);
                } else {
                    editTextParent.removePreference(editTextPreference);
                }

                checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object object) {
                        if ((boolean) object) {
                            editTextParent.addPreference(editTextPreference);
                        } else {
                            editTextParent.removePreference(editTextPreference);
                        }

                        return true;
                    }
                });

                // Bind the summaries of EditText/List/Dialog/Ringtone preferences
                // to their values. When their values change, their summaries are
                // updated to reflect the new value, per the Android Design
                // guidelines.
                bindPreferenceSummaryToValue(editTextPreference);
                bindPreferenceSummaryToValue(findPreference(KEY_PREF_LIST_UNITS));
            }
        }
    }

    private static PreferenceGroup getPreferenceGroup(PreferenceGroup root, Preference preference){
        for (int i = 0; i < root.getPreferenceCount(); i++) {
            Preference p = root.getPreference(i);

            if (p == preference) {
                return root;
            }

            if (PreferenceGroup.class.isInstance(p)) {
                PreferenceGroup parent = getPreferenceGroup((PreferenceGroup) p, preference);

                if (parent != null) {
                    return parent;
                }
            }
        }

        return  null;
    }
}