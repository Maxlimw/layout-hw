package com.praktikum.playlistmaker2.presentation

import com.praktikum.playlistmaker2.domain.model.Track

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TracksAdapter(
    private val tracks: ArrayList<Track> = ArrayList(),
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onTrackClick(tracks[currentPosition])
            }
        }
    }

    override fun getItemCount(): Int = tracks.size

    @SuppressLint("NotifyDataSetChanged")
    fun setTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }
}
