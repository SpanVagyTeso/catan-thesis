package gui.views

import controller.ViewController
import tornadofx.*

abstract class BaseView: View(title = "Catan") {
    protected val controller: ViewController by inject()

    abstract fun refresh()

    override fun onDock() {
        super.onDock()
        controller.currentView = this
        controller.refreshCurrentView = {
            this.refresh()
        }
    }
}