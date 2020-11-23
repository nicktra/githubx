package com.nicktra.consumerapp.helper

import android.database.Cursor
import com.nicktra.consumerapp.db.DatabaseContract
import com.nicktra.consumerapp.model.User

object MappingHelper {

    fun mapCursorToArrayList(userCursor: Cursor?): ArrayList<User> {
        val userList = ArrayList<User>()

        userCursor?.apply {
            while (moveToNext()) {
                val username = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USERNAME))
                val avatar = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_AVATAR_URL))
                val userId = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USER_ID))
                val url = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USER_URL))
                val htmlUrl = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USER_HTML_URL))
                userList.add(User(username, avatar, userId, url, htmlUrl))
            }
        }
        return userList
    }

    fun mapCursorToObject(usersCursor: Cursor?): User {
        var user = User()
        usersCursor?.apply {
            moveToFirst()
            val username = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USERNAME))
            val avatar = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_AVATAR_URL))
            val userId = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USER_ID))
            val url = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USER_URL))
            val htmlUrl = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.COLUMN_NAME_USER_HTML_URL))
            user = User(username, avatar, userId, url, htmlUrl)

        }
        return user
    }
}