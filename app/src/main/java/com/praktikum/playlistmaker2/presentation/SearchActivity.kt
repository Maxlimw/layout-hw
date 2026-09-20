package com.praktikum.playlistmaker2.presentation

import com.praktikum.playlistmaker2.R
import com.praktikum.playlistmaker2.Creator
import com.praktikum.playlistmaker2.domain.model.Track

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.praktikum.playlistmaker2.domain.api.HistoryInteractor
import com.praktikum.playlistmaker2.domain.api.SearchInteractor
import com.praktikum.playlistmaker2.domain.model.SearchResult
import com.praktikum.playlistmaker2.domain.repository.Cancellable

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val SEARCH_DELAY_MS = 2000L
        private const val CLICK_DELAY_MS = 1000L
    }

    private lateinit var searchInteractor: SearchInteractor

    private val tracksAdapter = TracksAdapter(onTrackClick = ::onTrackClick)
    private val historyAdapter = TracksAdapter(onTrackClick = ::onTrackClick)
    private lateinit var searchHistory: HistoryInteractor
    private lateinit var historyContainer: View
    private var searchCall: Cancellable? = null
    private var requestVersion = 0
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { search() }
    private var isClickAllowed = true
    private val unlockClickRunnable = Runnable { isClickAllowed = true }
    private lateinit var searchProgress: View

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
        historyContainer = findViewById(R.id.history_container)
        searchProgress = findViewById(R.id.search_progress)
        searchInteractor = Creator.createSearchInteractor()
        searchHistory = Creator.createHistoryInteractor()
        findViewById<RecyclerView>(R.id.history_recycler_view).apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = historyAdapter
        }
        findViewById<Button>(R.id.clear_history_button).setOnClickListener {
            searchHistory.clear()
            updateHistoryVisibility()
        }
        searchEditText.setOnFocusChangeListener { _, _ -> updateHistoryVisibility() }

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
                cancelSearch()
                tracksAdapter.setTracks(emptyList())
                hidePlaceholder()
                updateHistoryVisibility()
                searchDebounce()
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
            searchEditText.requestFocus()
            updateHistoryVisibility()
        }

        placeholderRefreshButton.setOnClickListener {
            search()
        }
        updateHistoryVisibility()
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
        handler.removeCallbacks(searchRunnable)
        val query = searchText.trim()
        if (query.isEmpty()) {
            return
        }

        cancelSearch()
        historyContainer.visibility = View.GONE
        hidePlaceholder()
        tracksAdapter.setTracks(emptyList())
        tracksRecyclerView.visibility = View.GONE
        searchProgress.visibility = View.VISIBLE
        val currentVersion = requestVersion
        searchCall = searchInteractor.search(query) { result ->
            if (currentVersion != requestVersion) return@search
            searchCall = null
            searchProgress.visibility = View.GONE
            when (result) {
                is SearchResult.Success -> {
                    if (result.tracks.isEmpty()) showEmptyResult() else showTracks(result.tracks)
                }
                SearchResult.Error -> showError()
            }
        }
    }

    private fun showTracks(tracks: List<Track>) {
        hidePlaceholder()
        tracksAdapter.setTracks(tracks)
        tracksRecyclerView.visibility = View.VISIBLE
    }

    private fun onTrackClick(track: Track) {
        if (!isClickAllowed) return
        isClickAllowed = false
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(unlockClickRunnable, CLICK_DELAY_MS)
        searchHistory.addTrack(track)
        updateHistoryVisibility()
        startActivity(PlayerActivity.createIntent(this, track))
    }

    private fun updateHistoryVisibility() {
        val tracks = searchHistory.getTracks()
        val showHistory = searchEditText.hasFocus() && searchEditText.text.isEmpty() && tracks.isNotEmpty()
        historyAdapter.setTracks(tracks)
        historyContainer.visibility = if (showHistory) View.VISIBLE else View.GONE
        if (searchEditText.text.isEmpty()) {
            tracksRecyclerView.visibility = View.GONE
            placeholderContainer.visibility = View.GONE
        }
    }

    private fun cancelSearch() {
        requestVersion++
        val previousCall = searchCall
        searchCall = null
        previousCall?.cancel()
        searchProgress.visibility = View.GONE
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        if (searchText.isNotBlank()) {
            handler.postDelayed(searchRunnable, SEARCH_DELAY_MS)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        cancelSearch()
        super.onDestroy()
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
