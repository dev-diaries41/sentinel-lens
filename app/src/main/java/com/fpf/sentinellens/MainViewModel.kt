package com.fpf.sentinellens


import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.fpf.sentinellens.constants.PrefsKeys
import com.fpf.sentinellens.data.DataSyncHelper
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.logs.LogRepository
import com.fpf.sentinellens.data.oldlogs.DetectionLogDatabase
import com.fpf.sentinellens.data.oldlogs.OldLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class MainViewModel(
    application: Application,
    private val sharedPrefs: SharedPreferences,
    private val logRepository: LogRepository,
    private val facesRepository: FacesRepository
    ) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    val versionName: String? = try {
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        packageInfo.versionName
    } catch (e: Exception) {
        null
    }

    private val hasSyncedData: Boolean
        get() = sharedPrefs.getBoolean(PrefsKeys.DATA_SYNC, false)

    private val storedVersion: String?
        get() = sharedPrefs.getString(PrefsKeys.UPDATES, null)

    private val _isUpdatePopUpVisible = MutableStateFlow(storedVersion != versionName && storedVersion != null)
    val  isUpdatePopUpVisible: StateFlow<Boolean> = _isUpdatePopUpVisible

    fun closeUpdatePopUp(){
        _isUpdatePopUpVisible.value = false
        sharedPrefs.edit { putString(PrefsKeys.UPDATES, versionName.toString()) }
    }

    fun getUpdates(): List<String>{
        return listOf(
            getApplication<Application>().getString(R.string.update_ui_improvements),
            )
    }

    suspend fun prepareApp(){
        if(hasSyncedData) return
        withContext(Dispatchers.IO) {
            val oldLogRepository = OldLogRepository(DetectionLogDatabase.getDatabase(getApplication()).detectionLogsDao())
            DataSyncHelper.transferLogs(oldLogRepository = oldLogRepository, logRepository = logRepository)
            DataSyncHelper.transformFacePaths(getApplication(), facesRepository)
        }
        sharedPrefs.edit { putBoolean(PrefsKeys.DATA_SYNC, true) }

        if(storedVersion == null) setVersion()
    }

   private fun setVersion() = sharedPrefs.edit { putString(PrefsKeys.UPDATES, versionName.toString()) }

}