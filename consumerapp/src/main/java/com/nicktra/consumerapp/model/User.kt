package com.nicktra.consumerapp.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var username: String? =  null,
    var avatar: String? = null,
    var userId: String? = null,
    var url: String? = null,
    var htmlUrl: String? = null
) : Parcelable