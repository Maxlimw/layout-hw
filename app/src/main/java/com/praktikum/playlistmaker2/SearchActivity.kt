package com.praktikum.playlistmaker2

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val ITUNES_BASE_URL = "https://itunes.apple.com"
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val itunesApi = retrofit.create(ItunesApi::class.java)

    private val tracksAdapter = TracksAdapter()

    private lateinit var backButton: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var placeholderContainer: LinearLayout
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var placeholderRefreshButton: Button

    private var searchText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_root)) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        backButton = findViewById(R.id.button_back)
        searchEditText = findViewById(R.id.search_edit_text)
        clearButton = findViewById(R.id.clear_icon)
        tracksRecyclerView = findViewById(R.id.tracks_recycler_view)
        placeholderContainer = findViewById(R.id.placeholder_container)
        placeholderImage = findViewById(R.id.placeholder_image)
        placeholderMessage = findViewById(R.id.placeholder_message)
        placeholderRefreshButton = findViewById(R.id.placeholder_refresh_button)

        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = tracksAdapter

        backButton.setOnClickListener {
            finish()
        }

        searchEditText.setText(searchText)
        updateClearButtonVisibility(searchText)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchText = s?.toString() ?: ""
                updateClearButtonVisibility(searchText)
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                search()
                true
            } else {
                false
            }
        }

        clearButton.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard()
            searchEditText.clearFocus()
            tracksAdapter.setTracks(emptyList())
            hidePlaceholder()
        }

        placeholderRefreshButton.setOnClickListener {
            search()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        searchEditText.setText(searchText)
        searchEditText.setSelection(searchEditText.text.length)
        updateClearButtonVisibility(searchText)
    }

    private fun search() {
        val query = searchText.trim()
        if (query.isEmpty()) {
            return
        }

        itunesApi.search(query).enqueue(object : Callback<TracksSearchResponse> {
            override fun onResponse(
                call: Call<TracksSearchResponse>,
                response: Response<TracksSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    if (tracks.isEmpty()) {
                        showEmptyResult()
                    } else {
                        showTracks(tracks)
                    }
                } else {
                    showError()
                }
            }

            override fun onFailure(call: Call<TracksSearchResponse>, t: Throwable) {
                showError()
            }
        })
    }

    private fun showTracks(tracks: List<Track>) {
        hidePlaceholder()
        tracksAdapter.setTracks(tracks)
        tracksRecyclerView.visibility = View.VISIBLE
    }

    private fun showEmptyResult() {
        tracksAdapter.setTracks(emptyList())
        tracksRecyclerView.visibility = View.GONE
        placeholderImage.setImageResource(R.drawable.ic_nothing_found)
        placeholderMessage.setText(R.string.nothing_found)
        placeholderRefreshButton.visibility = View.GONE
        placeholderContainer.visibility = View.VISIBLE
    }

    private fun showError() {
        tracksAdapter.setTracks(emptyList())
        tracksRecyclerView.visibility = View.GONE
        placeholderImage.setImageResource(R.drawable.ic_connection_error)
        placeholderMessage.setText(R.string.connection_error)
        placeholderRefreshButton.visibility = View.VISIBLE
        placeholderContainer.visibility = View.VISIBLE
    }

    private fun hidePlaceholder() {
        placeholderContainer.visibility = View.GONE
        tracksRecyclerView.visibility = View.VISIBLE
    }

    private fun updateClearButtonVisibility(text: String) {
        clearButton.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }
}
