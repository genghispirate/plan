package app.haven.intercept.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * Hosts a full-screen Jetpack Compose overlay in a WindowManager window
 * (TYPE_APPLICATION_OVERLAY), independent of any Activity.
 *
 * A ComposeView added directly to WindowManager has no ViewTree owners, so
 * Compose crashes on first composition. This class supplies its own
 * [Lifecycle], [ViewModelStore] and [SavedStateRegistry] and installs them as
 * the ViewTree owners — the minimal contract Compose needs to run detached.
 *
 * Must be created and driven on the main thread (WindowManager requirement).
 */
class OverlayHost(
    private val context: Context,
) : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    private var composeView: ComposeView? = null

    val isShowing: Boolean get() = composeView != null

    init {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    /** Adds the overlay (or no-ops if already showing). */
    fun show(content: @Composable () -> Unit) {
        if (composeView != null) return

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@OverlayHost)
            setViewTreeViewModelStoreOwner(this@OverlayHost)
            setViewTreeSavedStateRegistryOwner(this@OverlayHost)
            setContent(content)
        }
        windowManager.addView(view, buildLayoutParams())
        composeView = view
    }

    /** Removes the overlay if present. Safe to call repeatedly. */
    fun hide() {
        composeView?.let { view ->
            runCatching { windowManager.removeView(view) }
            composeView = null
        }
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    /** Tear down entirely; call from the owning service's onDestroy. */
    fun destroy() {
        hide()
        store.clear()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            // Focusable + touch-modal: the gate must fully capture interaction so
            // the blocked app underneath cannot be tapped through.
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT,
        )
    }
}
