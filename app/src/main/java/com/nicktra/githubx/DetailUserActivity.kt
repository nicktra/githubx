package com.nicktra.githubx

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.nicktra.githubx.adapter.SectionsPagerAdapter
import com.nicktra.githubx.db.DatabaseContract
import com.nicktra.githubx.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.nicktra.githubx.model.User
import com.nicktra.githubx.widget.FavoriteBannerWidget
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail_user.*
import org.json.JSONObject

class DetailUserActivity : AppCompatActivity() {
    private var statusFavorite: Boolean = false
    private lateinit var uriWithId: Uri

    companion object {
        const val EXTRA_USER = "extra_user"
        private val TAG = DetailUserActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_user)

        val dataUser = intent.getParcelableExtra<User>(EXTRA_USER)

        tv_detail_username.text = dataUser?.username
        Glide.with(this)
            .load(dataUser?.avatar)
            .into(img_detail_photo)
        Glide.with(this)
            .load(dataUser?.avatar)
            .into(img_card)

        val url = dataUser?.url.toString()
        getDetailUser(url)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        sectionsPagerAdapter.username = dataUser?.username
        view_pager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(view_pager)
        supportActionBar?.elevation = 0f

        uriWithId = Uri.parse(CONTENT_URI.toString() + "/" + dataUser?.userId)
        val cursor = contentResolver?.query(uriWithId, null, null, null, null)

        if (cursor != null) {
            statusFavorite = cursor.count > 0
        }
        cursor?.close()

        Log.d("status",statusFavorite.toString())
        setStatusFavorite(statusFavorite)

        fab_fav.setOnClickListener {
            statusFavorite = !statusFavorite
            if (statusFavorite) {
                val values = ContentValues()
                values.put(DatabaseContract.UserColumns.COLUMN_NAME_USERNAME, dataUser?.username)
                values.put(DatabaseContract.UserColumns.COLUMN_NAME_AVATAR_URL, dataUser?.avatar)
                values.put(DatabaseContract.UserColumns.COLUMN_NAME_USER_ID, dataUser?.userId)
                values.put(DatabaseContract.UserColumns.COLUMN_NAME_USER_URL, dataUser?.url)
                values.put(DatabaseContract.UserColumns.COLUMN_NAME_USER_HTML_URL, dataUser?.htmlUrl)
                contentResolver.insert(CONTENT_URI, values)

                showSnackbarMessage("Success Adding Favorite")
            } else {
                contentResolver.delete(uriWithId, null, null)

                showSnackbarMessage("Success Delete Favorite")
            }

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(this, FavoriteBannerWidget::class.java))
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view)

            setStatusFavorite(statusFavorite)
        }
    }

    private fun getDetailUser(url: String) {
        progressBarDetail.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization","token ${BuildConfig.GITHUB_TOKEN}")
        client.addHeader("User-Agent","request")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                // Jika koneksi berhasil
                progressBarDetail.visibility = View.INVISIBLE

                // Parsing JSON
                val result = String(responseBody)
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)

                    supportActionBar?.title = responseObject.getString("name")
                    tv_detail_repository.text = responseObject.getString("public_repos")
                    tv_detail_follower.text = responseObject.getString("followers")
                    tv_detail_following.text = responseObject.getString("following")
                    tv_detail_company.text = responseObject.getString("company")
                    tv_detail_location.text = responseObject.getString("location")

                } catch (e: Exception) {
                    Toast.makeText(this@DetailUserActivity, e.message, Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {
                // Jika koneksi gagal
                progressBarDetail.visibility = View.INVISIBLE
                val errorMessage = when (statusCode) {
                    401 -> "$statusCode : Bad Request"
                    403 -> "$statusCode : Forbidden"
                    404 -> "$statusCode : Not Found"
                    else -> "$statusCode : ${error.message}"
                }
                Toast.makeText(this@DetailUserActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share_menu -> {
                val dataUser = intent.getParcelableExtra<User>(EXTRA_USER)
                val username = dataUser?.username
                val htmlUrl = dataUser?.htmlUrl
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Github username: $username\nUrl: $htmlUrl\n\nFind more about $username on GithubX Apps")
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, "Share to ")
                startActivity(shareIntent)

                return true
            }
            else -> return true
        }
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(layout1, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun setStatusFavorite(statusFavorite: Boolean) {
        if (statusFavorite) {
            fab_fav.setImageResource(R.drawable.ic_baseline_star_white_36dp)
        } else {
            fab_fav.setImageResource(R.drawable.ic_baseline_star_border_white_36dp)
        }
    }

}