package com.praktikum.playlistmaker2.data.repository

import com.praktikum.playlistmaker2.data.dto.TracksSearchResponse
import com.praktikum.playlistmaker2.data.mapper.toDomain
import com.praktikum.playlistmaker2.data.network.ItunesApi
import com.praktikum.playlistmaker2.domain.model.SearchResult
import com.praktikum.playlistmaker2.domain.repository.Cancellable
import com.praktikum.playlistmaker2.domain.repository.TracksRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(private val api: ItunesApi) : TracksRepository {
    override fun search(query: String, callback: (SearchResult) -> Unit): Cancellable {
        val call = api.search(query)
        call.enqueue(object : Callback<TracksSearchResponse> {
            override fun onResponse(call: Call<TracksSearchResponse>, response: Response<TracksSearchResponse>) {
                if (call.isCanceled) return
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    callback(SearchResult.Success(body.results.orEmpty().map { it.toDomain() }))
                } else {
                    callback(SearchResult.Error)
                }
            }

            override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
                if (!call.isCanceled) callback(SearchResult.Error)
            }
        })
        return Cancellable { call.cancel() }
    }
}
