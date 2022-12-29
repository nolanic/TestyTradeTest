package assignment.testytradetest.activities

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import assignment.testytradetest.InAppMessageDispatcher
import assignment.testytradetest.R
import assignment.testytradetest.fragments.FragmentAddQuotes
import assignment.testytradetest.fragments.FragmentChart
import assignment.testytradetest.fragments.FragmentWatchlist
import assignment.testytradetest.viewModels.ViewModelAddQuotes
import assignment.testytradetest.viewModels.ViewModelChart
import assignment.testytradetest.viewModels.ViewModelWatchlist

class ActivityMain : AppCompatActivity() {
    companion object {
        private var isOperational = false
    }
    private val inAppCallback = InAppCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isOperational) {
            super.onCreate(savedInstanceState)
        } else {        // This is a little trick to avoid app crash when it's being restarted after a system kill
            super.onCreate(null)
        }
        isOperational = true

        InAppMessageDispatcher.register(inAppCallback)

        val mainView = FrameLayout(this)
        mainView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        mainView.id = R.id.containerViewId
        setContentView(mainView)

        if (supportFragmentManager.findFragmentById(R.id.containerViewId) == null) {
            showFragment(FragmentWatchlist(), false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        InAppMessageDispatcher.unregister(inAppCallback)
    }

    private fun showFragment(fragment: Fragment, addToBackStack:Boolean = true, containerId:Int = R.id.containerViewId) {
        val currentFragment = supportFragmentManager.findFragmentById(containerId)
        if (currentFragment != null) {
            if (currentFragment::class == fragment::class) {
                return
            }
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(containerId, fragment)
        if (addToBackStack) {
            transaction.addToBackStack(fragment::class.qualifiedName)
        }
        transaction.commit()
    }

    private inner class InAppCallback : InAppMessageDispatcher.Callback {
        override fun onMessage(senderId: String, message: Any) {
            if (message is ViewModelWatchlist.MessageAddQuotes) {
                InAppMessageDispatcher.addData(ViewModelAddQuotes::class.simpleName!!, message.watchList)
                val fragment = FragmentAddQuotes()
                showFragment(fragment, true)
            } else if (message is ViewModelAddQuotes.MessageSymbolsAdded) {
                supportFragmentManager.popBackStack()
            } else if (message is ViewModelWatchlist.MessageShowChart) {
                InAppMessageDispatcher.addData(ViewModelChart::class.simpleName!!, message.symbol)
                val fragment = FragmentChart()
                showFragment(fragment, true)
            }
        }
    }
}

