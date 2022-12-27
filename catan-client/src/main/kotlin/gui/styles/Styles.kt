package gui.styles

import com.sun.prism.paint.Color
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.shape.StrokeType
import javafx.scene.text.FontWeight
import tornadofx.Stylesheet
import tornadofx.c
import tornadofx.px

class Styles : Stylesheet() {
    init {
        Companion.label {
            fontSize = 12.px
//            backgroundColor += c("#cecece")
        }

    }
}