package com.example.iamhere.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.iamhere.data.network.MeshEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val meshEngine: MeshEngine
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        meshEngine.start()
        return Result.success()
    }
}
