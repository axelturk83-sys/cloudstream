package com.lagradost.cloudstream3.plugins

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class TainiomaniaProvider : MainAPI() {
    override var mainUrl = "https://tainio-mania.online"
    override var name = "Tainiomania"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override var lang = "el"
    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: HomePageRequest): HomePageResponse? {
        val document = app.get(mainUrl).document
        val homeItems = mutableListOf<SearchResponse>()
        
        document.select(".eTitle a, .mov-title a").forEach {
            val title = it.text()
            val url = it.attr("href")
            homeItems.add(newMovieSearchResponse(title, url, TvType.Movie))
        }
        
        return newHomePageResponse(listOf(HomePageList("Πρόσφατα", homeItems)), false)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrl = "$mainUrl/search/?q=$query"
        val document = app.get(searchUrl).document
        
        return document.select(".search-item a, .eTitle a").map {
            val title = it.text()
            val url = it.attr("href")
            newMovieSearchResponse(title, url, TvType.Movie)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.select("h1").text()
        val isTvSeries = url.contains("/serial") || document.select(".season-list").isNotEmpty()

        if (isTvSeries) {
            val episodes = mutableListOf<Episode>()
            return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes)
        } else {
            return newMovieLoadResponse(title, url, TvType.Movie, url)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCdn: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        document.select("iframe").forEach { iframe ->
            val src = iframe.attr("src")
            if (src.isNotEmpty()) {
                loadExtractor(src, mainUrl, subtitleCallback, callback)
            }
        }
        return true
    }
}

