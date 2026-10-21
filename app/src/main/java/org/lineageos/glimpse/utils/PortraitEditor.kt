/*
 * SPDX-FileCopyrightText: The DiamaneOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.lineageos.glimpse.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.lineageos.glimpse.R

/** Per-photo protocol: no originals, masks or editing data cross the app boundary. */
object PortraitEditor {
    private fun provider(context: Context): Uri {
        val camera = context.getString(R.string.config_portrait_editor_package)
        val authority = "$camera.portrait"
        // Only the configured camera may answer: another app cannot claim the authority first.
        val owner = context.packageManager.resolveContentProvider(authority, 0)?.packageName
        if (owner != camera) throw SecurityException("Portrait provider is not the camera's")
        return Uri.parse("content://$authority")
    }

    fun isEditable(context: Context, uri: Uri): Boolean = try {
        context.contentResolver.call(provider(context), "portraitEditable", uri.toString(), null)
            ?.getBoolean("editable") == true
    } catch (_: RuntimeException) {
        false
    }

    fun editIntent(context: Context, uri: Uri): Intent? = try {
        context.contentResolver.call(provider(context), "createPortraitEdit", uri.toString(), null)
            ?.getString("token")?.let { token ->
                Intent("de.diamaneos.intent.action.EDIT_PORTRAIT")
                    .setClassName(context.getString(R.string.config_portrait_editor_package),
                        "app.grapheneos.camera.ui.activities.ExternalPortraitEditorActivity")
                    .setDataAndType(uri, "image/jpeg")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra("token", token)
            }
    } catch (_: RuntimeException) {
        null
    }
}
