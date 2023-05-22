package gui.custom

import com.catan.sdk.dto.game.fromserver.PlayerTradeOfferDto
import javafx.scene.layout.GridPane
import tornadofx.action
import tornadofx.button
import tornadofx.label
import tornadofx.row

class TradeOffer(
    val dto: PlayerTradeOfferDto,
    val accept: () -> Unit
) : GridPane() {

    init {
        style = "-fx-border-color: black"
        row {
            label {
                text = "User: ${dto.withWho}"
            }
        }
        row {
            label {
                text = "For your ${dto.amount} ${dto.resource}"
            }
        }
        row {
            label {
                text = "You'll get ${dto.toAmount} ${dto.toResource}"
            }
        }
        row {
            button {
                text = "Accept"
                action {
                    accept()
                }
            }
        }
    }
}
