package com.nicktra.consumerapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.nicktra.consumerapp.BuildConfig
import com.nicktra.consumerapp.R
import com.nicktra.consumerapp.adapter.ListUserAdapter
import com.nicktra.consumerapp.model.User
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.fragment_follower.progressBar
import kotlinx.android.synthetic.main.fragment_following.*
import org.json.JSONArray

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FollowingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FollowingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val list = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_following.setHasFixedSize(true)

        val username= arguments?.getString(FollowingFragment.ARG_USERNAME)
        if (username != null) {
            getListUsers(username)
        }
    }

    private fun getListUsers(query: String) {
        progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        val url = "https://api.github.com/users/$query/following"
        client.addHeader("Authorization","token ${BuildConfig.GITHUB_TOKEN}")
        client.addHeader("User-Agent","request")
        client.get(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {
                // Jika koneksi berhasil
                list.clear()
                progressBar.visibility = View.INVISIBLE

                // Parsing JSON
                val result = String(responseBody)
                Log.d("Result : ", result)
                try {
                    val responseObject = JSONArray(result)

                    for (i in 0 until responseObject.length()) {
                        val item = responseObject.getJSONObject(i)

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
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
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
                    else -> "$statusCode : ${error.message}"
                }
                Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRecyclerList() {
        rv_following.layoutManager = LinearLayoutManager(activity)
        val listUserAdapter = ListUserAdapter(list)
        rv_following.adapter = listUserAdapter

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FollowingFragment.
         */
        // TODO: Rename and change types and number of parameters
        private val ARG_USERNAME = "username"

        @JvmStatic
        fun newInstance(username: String?): FollowingFragment {
            val fragment = FollowingFragment()
            val bundle = Bundle()
            bundle.putString(FollowingFragment.ARG_USERNAME, username)
            fragment.arguments = bundle
            return fragment
        }
    }
}