package com.nicktra.githubx.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.nicktra.githubx.db.DatabaseContract.AUTHORITY
import com.nicktra.githubx.db.DatabaseContract.UserColumns.Companion.CONTENT_URI
import com.nicktra.githubx.db.DatabaseContract.UserColumns.Companion.TABLE_NAME
import com.nicktra.githubx.db.FavoriteHelper

class GithubxProvider : ContentProvider() {

    companion object {
        private const val USER = 1
        private const val USER_ID = 2
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var favoriteHelper: FavoriteHelper

        init {
            sUriMatcher.addURI(AUTHORITY, "$TABLE_NAME/#", USER_ID)
            sUriMatcher.addURI(AUTHORITY, TABLE_NAME, USER)
        }
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val deleted: Int = when (USER_ID) {
            sUriMatcher.match(uri) -> favoriteHelper.deleteById(uri.lastPathSegment.toString())
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return deleted
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, contentvalues: ContentValues?): Uri? {
        val added: Long = when (USER) {
            sUriMatcher.match(uri) -> favoriteHelper.insert(contentvalues)
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return Uri.parse("$CONTENT_URI/$added")
    }

    override fun onCreate(): Boolean {
        favoriteHelper = FavoriteHelper.getInstance(context as Context)
        favoriteHelper.open()
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return when (sUriMatcher.match(uri)) {
            USER -> favoriteHelper.queryAll()
            USER_ID -> favoriteHelper.queryByUserId(uri.lastPathSegment.toString())
            else -> null
        }
    }

    override fun update(uri: Uri, contentvalues: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val updated: Int = when (USER_ID) {
            sUriMatcher.match(uri) -> favoriteHelper.update(uri.lastPathSegment.toString(), contentvalues)
            else -> 0
        }
        context?.contentResolver?.notifyChange(CONTENT_URI, null)
        return updated
    }
}