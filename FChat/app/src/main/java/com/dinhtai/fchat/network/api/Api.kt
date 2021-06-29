package com.dinhtai.fchat.network.api

import com.dinhtai.fchat.data.local.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import java.lang.reflect.Member
import java.sql.Timestamp


interface Api {
    @FormUrlEncoded
    @POST("/user/{token}/notification/create")
    fun createNotification(
        @Path("token") token: String,
        @Field("type") type: String,
        @Field("content") content: String,
        @Field("sender_id") senderId: String,
    ):Observable<StatusRepository>

    @GET("/user/{token}/notification/get")
    fun getNotification(
        @Path("token") token: String
    ): Observable<List<Notification>>

    @PUT("/user/{token}/notification/update/{id}")
    fun updateNotification(
        @Path("token") token: String,
        @Path("id") id: Int
    ): Observable<StatusRepository>

    @PUT("/user/{token}/notification/update-all")
    fun updateAllNotification(
        @Path("token") token: String
    ): Observable<StatusRepository>

    @DELETE("/user/{token}/notification/delete/{id}")
    fun deleteNotification(
        @Path("token") token: String,
        @Path("id") id: Int
    ): Observable<StatusRepository>

    @DELETE("/user/{token}/notification/delete-all")
    fun deleteAllNotification(
        @Path("token") token: String
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/{token}/contact/create")
    fun setContact(
        @Path("token") token: String,
        @Field("contacts") contacts: String
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/{token}/location/set")
    fun setLocation(
        @Path("token") token: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double
    ): Observable<StatusRepository>

    @GET("/user/{token}/location/{latitude}/{longitude}/{radius}/get-user-near")
    fun getUsersByNearLocation(
        @Path("token") token: String,
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double,
        @Path("radius") radius: Double,
    ): Observable<List<User>>

    @GET("/account/{token}/user")
    fun getUser(@Path("token") token: String): Observable<User>

    @GET("/account/{token}/user/logout")
    fun logout(@Path("token") token: String): Observable<StatusRepository>

    @GET("/account/{token}/user/login")
    fun login(@Path("token") token: String): Observable<StatusRepository>

    @GET("/account/{token}/user-by-phone/")
    fun getUsersByPhone(@Path("token") token: String): Observable<List<User>>

    @DELETE("/user/{token}/friend/delete/{id2}")
    fun unFriend(@Path("token") token: String, @Path("id2") id2: String): Observable<StatusRepository>

    @GET("/account/{token}/check/{id2}")
    fun checkFriendOrFollower(
        @Path("token") token: String,
        @Path("id2") id2: String
    ): Observable<StatusRepository>

    @GET("/account/client/{user_id}")
    fun getClient(@Path("user_id") id: String): Observable<User>

    @GET("/account/user-by-phone/{phone}")
    fun getMyUsersByPhone(phone: String): Observable<List<User>>

    @GET("/user/{token}/message")
    fun getShortMessages(@Path("token") token: String): Observable<List<Message>>

    @GET("/user/{token}/message/{id}")
    fun getMessagesById(
        @Path("token") token: String,
        @Path("id") id: String
    ): Observable<List<Message>>

    @GET("/user/{token}/follow")
    fun getFollows(@Path("token") token: String): Observable<List<User>>

    @FormUrlEncoded
    @POST("/user/{token}/create-follow")
    fun createFollow(
        @Path("token") token: String,
        @Field("id2") id2: String,
        @Field("preface") preface: String?
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/{token}/accept-follow")
    fun acceptFollow(
        @Path("token") token: String,
        @Field("id2") id2: String
    ): Observable<StatusRepository>

    @DELETE("/user/{token}/{id2}/refuse-follow")
    fun refuseFollow(
        @Path("token") token: String,
        @Path("id2") id2: String
    ): Observable<StatusRepository>

    @DELETE("/user/{token}/{id2}/cancel-follow")
    fun cancelFollow(
        @Path("token") token: String,
        @Path("id2") id2: String
    ): Observable<Void>

    @GET("/user/{token}/friend")
    fun getFriends(@Path("token") token: String): Observable<List<User>>

    @FormUrlEncoded
    @POST("/account/create")
    fun createUser(
        @Field("phone") phone: String,
        @Field("public_key") publicKey: String
    ): Observable<StatusRepository>

    @PUT("/account/{token}/update/name/{name}")
    fun updateNameUser(
        @Path("token") token: String,
        @Path("name") name: String
    ): Observable<StatusRepository>

    @PUT("/account/{token}/update/sex/{sex}")
    fun updateSexUser(
        @Path("token") token: String,
        @Path("sex") sex: String
    ): Observable<StatusRepository>

    @Multipart
    @POST("/account/{token}/update/avatar")
    fun updateAvatarUser(
        @Path("token") token: String,
        @Part avatar: MultipartBody.Part
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/chat")
    fun sendUser(
        @Field("sender_id") sender_id: String,
        @Field("receiver_id") receiver_id: String,
        @Field("content") content: String,
        @Field("create_date") createDate: Timestamp,
        @Field("type") type: TypeMessege
    ): Observable<Void>

    @FormUrlEncoded
    @Multipart
    @POST("/upload")
    fun createGroup(
        @Body file: MultipartBody.Part, @Path("token") token: String,
        @Field("name") name: String,
        @Field("members") member: String,
        @Field("avatar") avatar: String?
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/{token}/group/create")
    fun createGroup(
        @Path("token") token: String,
        @Field("name") name: String,
        @Field("members") member: String,
        @Field("avatar") avatar: String?
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/{token}/group/add-member")
    fun addMemberToGroup(
        @Path("token") token: String,
        @Field("id") groupId: Int,
        @Field("user_id") newMemberId: String
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @POST("/user/{token}/group/del-member")
    fun deleteMemberToGroup(
        @Path("token") token: String,
        @Field("id") groupId: Int,
        @Field("user_id") newMemberId: String
    ): Observable<StatusRepository>

    @FormUrlEncoded
    @PUT("/user/{token}/group/update")
    fun updateGroup(
        @Path("token") token: String,
        @Field("name") name: String,
        @Field("id") id: Int,
        @Field("avatar") avatar: String?
    ): Observable<StatusRepository>

    @GET("/user/{token}/group/member")
    fun getAllGroup(@Path("token") token: String): Observable<List<Group>>

    @DELETE("/user/{token}/group/delete/{id}")
    fun deleteGroup(@Path("token") token: String,@Path("id") id: Int): Observable<StatusRepository>

    @GET("/user/{token}/message/group/{groupId}/member")
    fun getMessageGroupByGroupId(
        @Path("token") token: String,
        @Path("groupId") groupId: Int
    ): Observable<Group>

    @GET("/user/{token}/search/{name}")
    fun searchFriend(@Path("token") token :String, @Path("name") name:String): Observable<List<User>>

    @FormUrlEncoded
    @PUT("/user/{token}/message/update-status")
    fun updateStatusMessage(
        @Path("token") token: String,
        @Field("id") userId: String
    ): Observable<StatusRepository>
}
