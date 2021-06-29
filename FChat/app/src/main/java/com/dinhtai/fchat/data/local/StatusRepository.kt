package com.dinhtai.fchat.data.local

data class StatusRepository(val code: Int, var status: Int, val token:String?, val user_id: String?,val user:User?) {
    companion object{
        val SUCCESS = 1;
        val FAILE = 0;
    }
}
