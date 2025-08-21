package com.mops.mopsdownloader.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Streaming
import retrofit2.http.Url

interface MopsApiService {
    // 獲取財報列表頁面 (HTML)
    @GET("server-java/t57sb01")
    suspend fun getReportListPage(
        @QueryMap options: Map<String, String>
    ): Response<String>

    // POST 請求，其回應可能是 PDF 也可能是中介 HTML
    @FormUrlEncoded
    @POST("server-java/t57sb01")
    suspend fun postForPdfOrHtml( // <--- 這就是 MopsRepository 需要的函式
        @FieldMap fields: Map<String, String>
    ): Response<ResponseBody> // 返回 ResponseBody

    // 使用完整 URL 下載最終的 PDF 檔案
    @GET
    @Streaming
    suspend fun downloadFinalPdf(
        @Url url: String
    ): Response<ResponseBody>
}