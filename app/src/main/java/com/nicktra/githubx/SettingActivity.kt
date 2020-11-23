package com.nicktra.githubx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nicktra.githubx.preference.ReminderPreferenceFragment

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportFragmentManager.beginTransaction().add(R.id.setting_holder, ReminderPreferenceFragment()).commit()
    }
}