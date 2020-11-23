package com.nicktra.githubx

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.nicktra.githubx.adapter.ListUserAdapter
import com.nicktra.githubx.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.nicktra.githubx.helper.MappingHelper
import com.nicktra.githubx.model.User
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.activity_main.img_placeholder_search
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.android.synthetic.main.activity_main.tv_placeholder_search
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {
    private var title: String = "Favorites"
    private var list = ArrayList<User>()
    private val waitingTime = 500
    private var cdt: CountDownTimer? = null

    private var defaultText = ""
    private lateinit var listUserAdapter: ListUserAdapter

    companion object {
        private val TAG = FavoriteActivity::class.java.simpleName
        private const val STATE_RESULT = "state_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        setActionBarTitle(title)

        rv_favorite.setHasFixedSize(true)

        showRecyclerList()

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        val myObserver = object : ContentObserver(handler) {
            override fun onChange(self: Boolean) {
                getListUsers()
            }
        }

        contentResolver.registerContentObserver(CONTENT_URI, true, myObserver)

        if (savedInstanceState == null) {
            getListUsers()
        } else {
            val listMain = savedInstanceState.getParcelableArrayList<User>(STATE_RESULT)
            if (listMain != null) {
                listUserAdapter.listUserFilter = listMain
                progressBar.visibility = View.INVISIBLE
                img_placeholder_search.visibility = View.INVISIBLE
                tv_placeholder_search.visibility = View.INVISIBLE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(STATE_RESULT, listUserAdapter.listUserFilter)
    }

    override fun onResume() {
        super.onResume()
        getListUsers()
    }

    private fun getListUsers() {
        GlobalScope.launch(Dispatchers.Main) {
            progressBar.visibility = View.VISIBLE
            val deferredNotes = async(Dispatchers.IO) {
                val cursor = contentResolver?.query(CONTENT_URI, null, null, null, null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            progressBar.visibility = View.INVISIBLE

            list = deferredNotes.await()
            if (list.size > 0) {
                showRecyclerList()
                img_placeholder_search.visibility=View.INVISIBLE
                tv_placeholder_search.visibility=View.INVISIBLE
            } else {
                showRecyclerList()
                img_placeholder_search.visibility=View.VISIBLE
                tv_placeholder_search.visibility=View.VISIBLE
                showSnackMessage(getString(R.string.empty_favorite))
            }
        }

    }

    private fun showSnackMessage(message: String) {
        Snackbar.make(rv_favorite, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showRecyclerList() {
        rv_favorite.layoutManager = LinearLayoutManager(this)
        listUserAdapter = ListUserAdapter(list)
        rv_favorite.adapter = listUserAdapter
    }

    private fun showAbout() {
        val moveAboutIntent = Intent(this@FavoriteActivity, AboutActivity::class.java)
        startActivity(moveAboutIntent)
    }

    private fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        val hideSearch = menu.findItem(R.id.search)
        hideSearch.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        setMode(item.itemId)
        return super.onOptionsItemSelected(item)
    }

    private fun setMode(selectedMode: Int) {
        when (selectedMode) {
            R.id.favorite -> {
                val mIntent = Intent(this, FavoriteActivity::class.java)
                startActivity(mIntent)
            }

            R.id.action_settings -> {
                val mIntent = Intent(this, SettingActivity::class.java)
                startActivity(mIntent)
            }

            R.id.action_about -> {
                showAbout()
            }
        }
        setActionBarTitle(title)
    }
}