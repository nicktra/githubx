package com.nicktra.githubx.widget

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.nicktra.githubx.R
import com.nicktra.githubx.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.nicktra.githubx.helper.MappingHelper
import com.nicktra.githubx.model.User
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

internal class StackRemoteViewsFactory(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<Bitmap>()
    private var favorite = arrayListOf<User>()
    private var cursor: Cursor? = null

    override fun onCreate() {

    }

    override fun onDataSetChanged() {
        cursor?.close()
        //Ini berfungsi untuk melakukan refresh saat terjadi perubahan.
        val identityToken = Binder.clearCallingIdentity()

        val cursor =  mContext.contentResolver.query(CONTENT_URI, null, null, null, null)
        favorite = MappingHelper.mapCursorToArrayList(cursor)

        mWidgetItems.clear()

        for (i in favorite) {
            mWidgetItems.add(getBitmapFromURL(i.avatar)!!)
        }

        Binder.restoreCallingIdentity(identityToken)
    }

    private fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int = mWidgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setImageViewBitmap(R.id.imageView, mWidgetItems[position])

        val extras = bundleOf(
            FavoriteBannerWidget.EXTRA_ITEM to position
        )
        val fillInIntent = Intent()
        fillInIntent.putExtras(extras)

        rv.setOnClickFillInIntent(R.id.imageView, fillInIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(i: Int): Long = 0

    override fun hasStableIds(): Boolean = false

}