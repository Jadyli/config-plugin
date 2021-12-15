package com.jady.app

import android.app.Application
import android.content.Context
import com.jady.utils.application
import dagger.hilt.android.HiltAndroidApp


/**
 * @author jady
 * @since 2021-12-14, 周二, 23:53
 * email: 1257984872@qq.com
 */
@HiltAndroidApp
class MainApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }
}
