package com.dinhtai.fchat.ui.search.usernear

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dinhtai.fchat.base.RxViewModel
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.Location
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.network.api.Api
import com.dinhtai.fchat.network.api.ApiBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.io.IOException
import java.util.*

class UserNearViewModel : RxViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address
    private fun getUsersNearByLocation(token: String, location: Location, distance: Double) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java)
                .getUsersByNearLocation(token, location.latitude, location.longitude, distance)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ users ->
                    _users.value = users
                    Log.d("user near", users.toString())
                }, {
                    it.printStackTrace()
                    Log.d("friend model", it.toString())
                })
        )
    }

    fun setLocation(token: String, location: Location) {
        disposables.add(
            ApiBuilder.retrofit.create(Api::class.java).setLocation(token, location.latitude,location.longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {})
        )
    }

    fun getAddress(location: Location, context: Context) {
        try {
            var geoCoder = Geocoder(context, Locale.getDefault())
            var addr =  geoCoder.getFromLocation(location.latitude, location.longitude, 3).get(0)
            _address.value = addr.getAddressLine(0)
            Log.d("dia chi ",geoCoder.getFromLocation(location.latitude, location.longitude, 3)
                .toString())
        }  catch (e:IOException) {
            _address.value = "Đã có lỗi với JPS"
        }

    }

    fun getUsersNearByLocation(location: Location, distance: Double) {
        InfoYourself.token?.let { getUsersNearByLocation(it, location, distance) }
    }
}
