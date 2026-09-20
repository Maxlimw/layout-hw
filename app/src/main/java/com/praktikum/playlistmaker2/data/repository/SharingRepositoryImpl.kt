package com.praktikum.playlistmaker2.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.praktikum.playlistmaker2.R
import com.praktikum.playlistmaker2.domain.repository.SharingRepository

class SharingRepositoryImpl(context: Context) : SharingRepository {
    private val context = context.applicationContext

    override fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message))
        }
        launch(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)))
    }

    override fun contactSupport() {
        launch(Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.support_subject))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.support_message))
        })
    }

    override fun openTerms() {
        launch(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.terms_url))))
    }

    private fun launch(intent: Intent) {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
