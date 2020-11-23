package com.nicktra.githubx

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.nicktra.githubx.adapter.ListUserAdapter
import com.nicktra.githubx.model.User
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var title: String = "GitHubX"
    private val list = ArrayList<User>()
    private val waitingTime = 500
    private var cdt: CountDownTimer? = null

    private lateinit var listUserAdapter: ListUserAdapter

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val STATE_RESULT = "state_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setActionBarTitle(title)

        rv_users.setHasFixedSize(true)

        showRecyclerList()

        if (savedInstanceState == null) {

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

    private fun getListUsers(query: String) {
        progressBar.visibility = View.VISIBLE

        val client = AsyncHttpClient()
        val url = "https://api.github.com/search/users?q=$query"
        client.addHeader("Authorization","token ${BuildConfig.GITHUB_TOKEN}")
        client.addHeader("User-Agent","request")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                // Jika koneksi berhasil
                list.clear()
                progressBar.visibility = View.INVISIBLE
                img_placeholder_search.visibility = View.INVISIBLE
                tv_placeholder_search.visibility = View.INVISIBLE

                // Parsing JSON
                val result = String(responseBody)
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)
                    val items = responseObject.getJSONArray("items")

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)

                        val username = item.getString("login")
                        val avatar = item.getString("avatar_url")
                        val userId = item.getString("id")
                        val url = item.getString("url")
                        val htmlUrl = item.getString("html_url")

                        val newUser = User(username, avatar, userId, url, htmlUrl)
                        list.add(newUser)
                    }
                    showRecyclerList()

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                // Jika koneksi gagal
                progressBar.visibility = View.INVISIBLE
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    422 -> getString(R.string.search_user_info)
                    else -> "$statusCode : ${error.message}"
                }
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRecyclerList() {
        rv_users.layoutManager = LinearLayoutManager(this)
        listUserAdapter = ListUserAdapter(list)
        rv_users.adapter = listUserAdapter

    }

    private fun showAbout() {
        val moveAboutIntent = Intent(this@MainActivity, AboutActivity::class.java)
        startActivity(moveAboutIntent)
    }

    private fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /*
            Gunakan method ini ketika search selesai atau OK
             */
            override fun onQueryTextSubmit(query: String): Boolean {
                getListUsers(query)
                return true
            }

            /*
            Gunakan method ini untuk merespon tiap perubahan huruf pada searchView
             */
            override fun onQueryTextChange(newText: String): Boolean {
                cdt?.cancel()
                cdt = object : CountDownTimer(waitingTime.toLong(), 500) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.d(
                            "TIME",
                            "seconds remaining: " + millisUntilFinished / 1000
                        )
                    }

                    override fun onFinish() {
                        Log.d("FINISHED", "DONE")
                        getListUsers(newText)
                    }
                }
                (cdt as CountDownTimer).start()
                return false
            }
        })
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
                /*val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(mIntent)*/
            }

            R.id.action_about -> {
                showAbout()
            }
        }
        setActionBarTitle(title)
    }
}