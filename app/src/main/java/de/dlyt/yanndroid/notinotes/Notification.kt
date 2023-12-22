package de.dlyt.yanndroid.notinotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.ColorUtils


class Notification {

    companion object {

        val NOTIFICATION_GROUP = "de.dlyt.yanndroid.notinotes.NOTES_GROUP"
        val NOTIFICATION_GROUP_HOLDER = -1
        val NOTIFICATION_CHANNEL = "1234"

        fun showAll(context: Context) {
            for (note in Notes.getNotes(context)) show(context, note)
        }

        fun cancelAll(context: Context) {
            NotificationManagerCompat.from(context).cancelAll()
        }

        fun show(context: Context, note: Notes.Note) {
            val nmc = NotificationManagerCompat.from(context)
            nmc.notify( //for some reason this in needed to make grouping work
                NOTIFICATION_GROUP_HOLDER, NotificationCompat.Builder(
                    context,
                    NOTIFICATION_CHANNEL
                )
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_note)
                    .setGroup(NOTIFICATION_GROUP)
                    .setGroupSummary(true)
                    .build()
            )
            val notiBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setStyle(NotificationCompat.BigTextStyle())
                .setOngoing(note.locked)
                .setVisibility(if (note.secret) NotificationCompat.VISIBILITY_SECRET else NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_note)
                .setContentTitle(note.title)
                .setContentText(note.content)
                .addAction(
                    R.drawable.ic_edit,
                    context.getString(R.string.edit),
                    ActionReceiver.getPendingIntent(context, note, ActionReceiver.ACTION_EDIT)
                )
                .addAction(
                    R.drawable.ic_delete,
                    context.getString(R.string.del),
                    ActionReceiver.getPendingIntent(context, note, ActionReceiver.ACTION_DELETE)
                )
                .setContentIntent(
                    ActionReceiver.getPendingIntent(context, note, ActionReceiver.ACTION_SHOW)
                )
                .setDeleteIntent(
                    ActionReceiver.getPendingIntent(context, note, ActionReceiver.ACTION_DISMISS)
                )
                .setColor(note.color)

            if (note.group) notiBuilder.setGroup(NOTIFICATION_GROUP)

            if (note.bg_tint) {
                val darkMode =
                    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                val blendColor = if (darkMode) Color.BLACK else Color.WHITE
                notiBuilder.setContent(
                    notiBuilder.createContentView()!!.also {
                        it.setInt(
                            LayoutInflater.from(context).inflate(it.layoutId, null).id,
                            "setBackgroundColor",
                            ColorUtils.blendARGB(
                                note.color,
                                blendColor,
                                if (darkMode) 0.5f else 0.3f
                            )
                        )
                    })
                notiBuilder.setCustomBigContentView(
                    notiBuilder.createBigContentView()!!.also {
                        it.setInt(
                            LayoutInflater.from(context).inflate(it.layoutId, null).id,
                            "setBackgroundColor",
                            ColorUtils.blendARGB(
                                note.color,
                                blendColor,
                                if (darkMode) 0.3f else 0.1f
                            )
                        )
                    })
            }

            nmc.notify(note.id, notiBuilder.build())

        }

        fun cancel(context: Context, note: Notes.Note) {
            val nmc = NotificationManagerCompat.from(context)
            nmc.cancel(note.id)
            if (Notes.getNotes(context).size == 0) nmc.cancel(NOTIFICATION_GROUP_HOLDER)
        }

        fun createNotificationChannel(context: Context) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    context.getString(R.string.notes),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

    }
}