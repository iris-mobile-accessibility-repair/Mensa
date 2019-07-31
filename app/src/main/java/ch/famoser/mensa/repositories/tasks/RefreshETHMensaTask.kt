package ch.famoser.mensa.repositories.tasks

import android.os.AsyncTask
import ch.famoser.mensa.events.MensaMenuUpdatedEvent
import ch.famoser.mensa.events.RefreshMensaFinishedEvent
import ch.famoser.mensa.events.RefreshMensaProgressEvent
import ch.famoser.mensa.events.RefreshMensaStartedEvent
import ch.famoser.mensa.services.providers.ETHMensaProvider
import org.greenrobot.eventbus.EventBus
import java.util.*

class RefreshETHMensaTask(
    private val mensaProvider: ETHMensaProvider,
    private val date: Date,
    private val language: String,
    private val ignoreCache: Boolean
) :
    AsyncTask<String, Int, Unit>() {

    private val asyncTaskId = UUID.randomUUID()

    override fun doInBackground(vararg times: String) {
        for ((index, source) in times.withIndex()) {
            val refreshedMensas = mensaProvider.getMenus(source, date, language, ignoreCache)

            if (isCancelled) return
            publishProgress(times.size, index)

            for (mensa in refreshedMensas) {
                EventBus.getDefault().post(MensaMenuUpdatedEvent(mensa.id))
            }
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        EventBus.getDefault().post(RefreshMensaStartedEvent(asyncTaskId))
    }

    override fun onPostExecute(result: Unit?) {
        super.onPostExecute(result)
        EventBus.getDefault().post(RefreshMensaFinishedEvent(asyncTaskId))
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        EventBus.getDefault().post(RefreshMensaProgressEvent(asyncTaskId, values[0]!!, values[1]!!))
    }
}
