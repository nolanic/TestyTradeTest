package assignment.testytradetest.models

import java.io.Serializable

class SymbolDescription : Serializable {
    companion object {
        private const val serialVersionUID = 0L
    }

    val symbol = ""
    val name = ""

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (!(other is SymbolDescription)) {
            return false
        }

        return other.symbol == symbol && other.name == name
    }

    override fun hashCode(): Int {
        return symbol.hashCode()
    }
}