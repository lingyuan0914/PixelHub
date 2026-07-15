package com.pixelhub.app.data.remote

import com.pixelhub.app.data.remote.model.LoliconResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface PixelHubApi {
    @GET("https://api.lolicon.app/setu/v2")
    suspend fun getLoliconImages(
        @Query("r18") r18: Int = 0,
        @Query("num") num: Int = 20,
        @Query("tag") tag: String? = null,
        @Query("uid") uid: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("size") size: String = "regular",
        @Query("proxy") proxy: String = "i.pixiv.cat",
        @Query("excludeAI") excludeAI: Boolean = true
    ): LoliconResponse

    @GET
    suspend fun fetchFromUrl(@Url url: String): Response<ResponseBody>
}
