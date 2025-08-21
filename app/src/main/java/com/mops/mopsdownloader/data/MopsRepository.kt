package com.mops.mopsdownloader.data

import android.util.Log
import com.mops.mopsdownloader.network.MopsApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.regex.Pattern

sealed class DownloadResult {
    data class Success(val body: ResponseBody) : DownloadResult()
    data class NotFound(val message: String) : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

class MopsRepository {
    private val baseUrl = "https://doc.twse.com.tw"

    private val apiService: MopsApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(MopsApiService::class.java)

    suspend fun downloadFinancialReport(
        coId: String,
        year: String,
        season: String
    ): DownloadResult = withContext(Dispatchers.IO) {
        val rocYear = try { year.toInt() - 1911 } catch (e: NumberFormatException) { return@withContext DownloadResult.Error("年份格式錯誤") }
        val queryParams = mapOf("step" to "1", "colorchg" to "1", "co_id" to coId, "year" to rocYear.toString(), "seamon" to season, "mtype" to "A")

        try {
            val htmlResponse = apiService.getReportListPage(queryParams)
            if (htmlResponse.isSuccessful && htmlResponse.body() != null) {
                val document = Jsoup.parse(htmlResponse.body()!!)
                if (document.body().text().contains("查無所需資料")) {
                    return@withContext DownloadResult.NotFound("查無所需資料")
                }

                val links = document.select("a[href*='readfile2']")
                for (link in links) {
                    val originalHref = link.attr("href")
                    val cleanHref = originalHref.removePrefix("javascript:")
                    val pattern = Pattern.compile("readfile2\\(\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\"\\)")
                    val matcher = pattern.matcher(cleanHref)

                    if (matcher.find()) {
                        val kind = matcher.group(1); val foundCoId = matcher.group(2); val filename = matcher.group(3)
                        if (filename != null && (filename.contains("AI1") || filename.contains("AI2"))) {
                            Log.i("MopsRepository", "[Financial] Found target link: $filename. Executing POST request...")
                            val postData = mapOf("colorchg" to "1", "step" to "9", "kind" to kind!!, "co_id" to foundCoId!!, "filename" to filename)

                            val postResponse = apiService.postForPdfOrHtml(postData)
                            if (postResponse.isSuccessful && postResponse.body() != null) {
                                val responseBody = postResponse.body()!!

                                val firstFourBytes = responseBody.source().peek().readByteArray(4)
                                val isPdf = firstFourBytes.toString(Charsets.US_ASCII) == "%PDF"

                                if (isPdf) {
                                    return@withContext DownloadResult.Success(responseBody)
                                } else {
                                    val intermediateHtml = responseBody.string()
                                    val intermediateDoc = Jsoup.parse(intermediateHtml)
                                    val finalPdfLinkElement = intermediateDoc.selectFirst("a[href*='.pdf']")
                                    if (finalPdfLinkElement != null) {
                                        val pdfPath = finalPdfLinkElement.attr("href")
                                        val finalUrl = "$baseUrl$pdfPath"
                                        val finalPdfResponse = apiService.downloadFinalPdf(finalUrl)
                                        return@withContext if (finalPdfResponse.isSuccessful && finalPdfResponse.body() != null) {
                                            DownloadResult.Success(finalPdfResponse.body()!!)
                                        } else {
                                            DownloadResult.Error("最終PDF下載失敗: ${finalPdfResponse.message()}")
                                        }
                                    } else {
                                        return@withContext DownloadResult.NotFound("在中介頁面中未找到PDF連結")
                                    }
                                }
                            } else {
                                return@withContext DownloadResult.Error("POST請求失敗: ${postResponse.message()}")
                            }
                        }
                    }
                }
                return@withContext DownloadResult.NotFound("在頁面中未找到可下載的中文財報PDF")
            } else {
                return@withContext DownloadResult.Error("查詢頁面失敗: ${htmlResponse.message()}")
            }
        } catch (e: Exception) {
            return@withContext DownloadResult.Error("網路連線或解析時發生錯誤: ${e.message}")
        }
    }

    suspend fun downloadAnnualReport(
        coId: String,
        year: String
    ): DownloadResult = withContext(Dispatchers.IO) {
        val rocYear = try { year.toInt() - 1911 } catch (e: NumberFormatException) { return@withContext DownloadResult.Error("年份格式錯誤") }
        val queryParams = mapOf("step" to "1", "colorchg" to "1", "co_id" to coId, "year" to rocYear.toString(), "mtype" to "F")

        try {
            val htmlResponse = apiService.getReportListPage(queryParams)
            if (htmlResponse.isSuccessful && htmlResponse.body() != null) {
                val document = Jsoup.parse(htmlResponse.body()!!)
                if (document.body().text().contains("查無所需資料")) {
                    return@withContext DownloadResult.NotFound("查無所需資料")
                }

                val links = document.select("a[href*='readfile2']")
                for (link in links) {
                    val originalHref = link.attr("href")
                    val cleanHref = originalHref.removePrefix("javascript:")
                    val pattern = Pattern.compile("readfile2\\(\"([^\"]*)\",\"([^\"]*)\",\"([^\"]*)\"\\)")
                    val matcher = pattern.matcher(cleanHref)

                    if (matcher.find()) {
                        val kind = matcher.group(1); val foundCoId = matcher.group(2); val filename = matcher.group(3)

                        if (filename != null && filename.contains("F04")) {
                            Log.i("MopsRepository", "[Annual] Found target link: $filename. Executing POST request...")
                            val postData = mapOf("colorchg" to "1", "step" to "9", "kind" to kind!!, "co_id" to foundCoId!!, "filename" to filename)

                            val postResponse = apiService.postForPdfOrHtml(postData)
                            if (postResponse.isSuccessful && postResponse.body() != null) {
                                val responseBody = postResponse.body()!!
                                val firstFourBytes = responseBody.source().peek().readByteArray(4)
                                val isPdf = firstFourBytes.toString(Charsets.US_ASCII) == "%PDF"

                                if (isPdf) {
                                    return@withContext DownloadResult.Success(responseBody)
                                } else {
                                    val intermediateHtml = responseBody.string()
                                    val intermediateDoc = Jsoup.parse(intermediateHtml)
                                    val finalPdfLinkElement = intermediateDoc.selectFirst("a[href*='.pdf']")
                                    if (finalPdfLinkElement != null) {
                                        val pdfPath = finalPdfLinkElement.attr("href")
                                        val finalUrl = "$baseUrl$pdfPath"
                                        val finalPdfResponse = apiService.downloadFinalPdf(finalUrl)
                                        return@withContext if (finalPdfResponse.isSuccessful && finalPdfResponse.body() != null) {
                                            DownloadResult.Success(finalPdfResponse.body()!!)
                                        } else {
                                            DownloadResult.Error("最終PDF下載失敗: ${finalPdfResponse.message()}")
                                        }
                                    } else {
                                        return@withContext DownloadResult.NotFound("在中介頁面中未找到PDF連結")
                                    }
                                }
                            } else {
                                return@withContext DownloadResult.Error("POST請求失敗: ${postResponse.message()}")
                            }
                        }
                    }
                }
                return@withContext DownloadResult.NotFound("在頁面中未找到可下載的中文年報PDF")
            } else {
                return@withContext DownloadResult.Error("查詢頁面失敗: ${htmlResponse.message()}")
            }
        } catch (e: Exception) {
            return@withContext DownloadResult.Error("網路連線或解析時發生錯誤: ${e.message}")
        }
    }
}