package com.devpa.app

import android.app.Application
import com.devpa.app.data.repository.SeedDataUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DevPAApp : Application() {

    @Inject
    lateinit var seedDataUseCase: SeedDataUseCase

    override fun onCreate() {
        super.onCreate()
        GlobalScope.launch(Dispatchers.IO) {
            seedDataUseCase.execute()
        }
    }
}
