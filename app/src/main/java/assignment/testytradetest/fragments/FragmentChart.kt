package assignment.testytradetest.fragments

import android.graphics.Paint
import android.os.Bundle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import assignment.testytradetest.R
import assignment.testytradetest.models.HistoryPrice
import assignment.testytradetest.viewModels.ViewModelChart

class FragmentChart : FragmentBase() {

    private lateinit var viewModel: ViewModelChart
    private lateinit var textPaint : Paint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewModelChart::class.java)
        textPaint = Paint()
        textPaint.color = Color.Black.toArgb()
        textPaint.textSize = 15.dp.value * resources.displayMetrics.density
        textPaint.textAlign = Paint.Align.RIGHT
    }

    @Composable
    override fun setContent() {
        val notAvailable = getString(R.string.not_available)
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(1f).padding(start = 5.dp, end = 5.dp, top = 10.dp)) {
                Text(text = getString(R.string.symbol), fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text(text = getString(R.string.bid_price), fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text(text = getString(R.string.ask_price), fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text(text = getString(R.string.last_price), fontSize = 18.sp, modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp, top = 5.dp),
                    thickness = 2.dp)
            if (viewModel.quote.symbol.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth(1f).padding(start = 5.dp, end = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = viewModel.quote.symbol, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text(text = viewModel.quote.bidPrice?.toString() ?: notAvailable, modifier = Modifier.weight(1f))
                    Text(text = viewModel.quote.askPrice?.toString() ?: notAvailable, modifier = Modifier.weight(1f))
                    Text(text = viewModel.quote.lastPrice?.toString() ?: notAvailable, modifier = Modifier.weight(1f))
                }
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp, top = 3.dp),
                    thickness = 5.dp)

            if (viewModel.prices.size > 2) {
                val text = String.format(getString(R.string.from_to), viewModel.prices.last().priceDate, viewModel.prices.first().priceDate)
                Text(modifier = Modifier.align(Alignment.CenterHorizontally),
                     text = text, fontSize = 15.sp)
            }

            Row(modifier = Modifier.fillMaxSize(1f).padding(end = 10.dp, bottom = 10.dp)) {
                Scale(modifier = Modifier.fillMaxHeight().width(70.dp), hPrices = viewModel.prices)
                Chart(modifier = Modifier.fillMaxSize(1f).padding(start = 10.dp), hPrices = viewModel.prices)
            }
        }
    }

    @Composable
    private fun Chart(modifier: Modifier, hPrices:List<HistoryPrice>) {
       Canvas(modifier = modifier) {
           if (hPrices.size == 0) {
               return@Canvas
           }
           var stickWidth = size.width / hPrices.size
           if (stickWidth > 50.dp.toPx()) {
               stickWidth = 50.dp.toPx()
           }

           var maxPrice = 0f
           var minPrice = Float.POSITIVE_INFINITY
           for (hPrice in hPrices) {
               if (maxPrice < hPrice.highestPrice()){
                   maxPrice = hPrice.highestPrice()
               }
               if (minPrice > hPrice.lowestPrice()) {
                   minPrice = hPrice.lowestPrice()
               }
           }
           if (maxPrice == 0f) {
               return@Canvas
           }

           val pixelPerPrice = size.height / (maxPrice - minPrice)
           var xOffset = 0f
           for (price in hPrices) {
               if (price.high != null && price.low != null) {
                   val topY = size.height - pixelPerPrice * (price.high - minPrice)
                   var bottomY = size.height - pixelPerPrice * (price.low - minPrice)
                   if (bottomY - topY < 2.dp.toPx()) {
                       bottomY = topY + 2.dp.toPx()
                   }
                   val x = xOffset + stickWidth / 2
                   drawLine(Color.Black, Offset(x, topY), Offset(x, bottomY), 2.dp.toPx())
               }

               val type = price.getType()
               if (type == HistoryPrice.Type.ASCENDING || type == HistoryPrice.Type.DESCENDING) {
                   val color = if (type == HistoryPrice.Type.ASCENDING) Color.Green else Color.Red
                   val topY = size.height - pixelPerPrice * (price.topPrice() - minPrice)
                   var bottomY = size.height - pixelPerPrice * (price.bottomPrice() - minPrice)
                   if (bottomY - topY < 2.dp.toPx()) {
                       bottomY = topY + 2.dp.toPx()
                   }
                   val size = Size(stickWidth, bottomY - topY)
                   drawRect(color, Offset(xOffset, topY), size)
               } else if (type == HistoryPrice.Type.STEADY) {
                   val y = size.height - pixelPerPrice * (price.topPrice() - minPrice)
                   val xEnd = xOffset + stickWidth
                   drawLine(Color.Black, Offset(xOffset, y), Offset(xEnd, y), 2.dp.toPx())
               }
               xOffset+=stickWidth
           }
       }
    }

    @Composable
    fun Scale(modifier: Modifier, hPrices:List<HistoryPrice>) {
        Canvas(modifier = modifier) {
            if (hPrices.size == 0) {
                return@Canvas
            }

            var maxPrice = 0f
            var minPrice = Float.POSITIVE_INFINITY
            for (hPrice in hPrices) {
                if (maxPrice < hPrice.highestPrice()){
                    maxPrice = hPrice.highestPrice()
                }
                if (minPrice > hPrice.lowestPrice()) {
                    minPrice = hPrice.lowestPrice()
                }
            }
            if (maxPrice == 0f) {
                return@Canvas
            }

            val priceRange = maxPrice - minPrice
            var segmentSize = 50.dp.toPx()
            val segmentCount : Int = size.height.toInt() / segmentSize.toInt()
            segmentSize = size.height / segmentCount
            var segmentPrice = priceRange / segmentCount

            val endX = size.width
            val x = endX - 5.dp.toPx()
            val textX = x - 5.dp.toPx()
            var y = 0f
            var price = maxPrice
            drawLine(Color.Black, Offset(x, 0f), Offset(x, size.height), 2.dp.toPx())

            y += segmentSize
            price -= segmentPrice
            while (y < size.height - 1) {
                val textY : Float = y + textPaint.textSize / 2.5f
                drawContext.canvas.nativeCanvas.apply {
                    val text = String.format("%.2f", price)
                    drawText(text, textX, textY, textPaint)
                }
                drawLine(Color.Black, Offset(x, y), Offset(endX, y), 2.dp.toPx())
                y += segmentSize
                price -= segmentPrice
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startQuoteGetter()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopQuoteGetter()
    }
}