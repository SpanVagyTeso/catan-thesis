import gui.MainView
import gui.styles.Styles
import tornadofx.App
import tornadofx.launch

class CatanApp : App(MainView::class, Styles::class)

fun main(args: Array<String>) {
    launch<CatanApp>(args)
}
