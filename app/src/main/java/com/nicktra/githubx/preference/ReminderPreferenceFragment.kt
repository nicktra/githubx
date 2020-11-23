package com.nicktra.githubx.preference

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.nicktra.githubx.R
import com.nicktra.githubx.alarm.AlarmReceiver
import java.util.*

class ReminderPreferenceFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var REMINDER: String
    private lateinit var LANGUAGE: String

    private lateinit var reminderPreference: SwitchPreference
    private lateinit var languagePreference: Preference

    private lateinit var alarmReceiver: AlarmReceiver

    private var langval = Locale.getDefault().getDisplayLanguage()

    companion object {
        private const val DEFAULT_VALUE = "Nothing"
    }

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.preferences)
        init()
        setSummaries()

    }

    private fun init() {
        REMINDER = resources.getString(R.string.key_reminder_setting)
        LANGUAGE = resources.getString(R.string.key_language_setting)

        reminderPreference = findPreference<SwitchPreference> (REMINDER) as SwitchPreference
        languagePreference = findPreference<Preference>(LANGUAGE) as Preference

        alarmReceiver = AlarmReceiver()

        languagePreference.setOnPreferenceClickListener {
            val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(mIntent)
            true
        }

    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == LANGUAGE) {
            languagePreference.summary = sharedPreferences.getString(LANGUAGE, langval)
        }

        if (key == REMINDER) {
            reminderPreference.isChecked = sharedPreferences.getBoolean(REMINDER, false)

            val repeatMessage = getString(R.string.reminder)
            val repeatTime = "09:00"

            var msg = ""
            if(!reminderPreference.isChecked){
                msg = "Reminder at 9:00 AM disabled."
                context?.let { alarmReceiver.cancelAlarm(it, AlarmReceiver.TYPE_REPEATING) }
            }else {
                msg = "Reminder at 9:00 AM enabled."
                context?.let { alarmReceiver.setRepeatingAlarm(it, AlarmReceiver.TYPE_REPEATING, repeatTime, repeatMessage) }
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSummaries() {
        val sh = preferenceManager.sharedPreferences
        languagePreference.summary = sh.getString(LANGUAGE, langval)
        reminderPreference.isChecked = sh.getBoolean(REMINDER, false)
    }

}