package assignment.testytradetest.fragments

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import assignment.testytradetest.App
import assignment.testytradetest.R
import assignment.testytradetest.composables.StandardDialog
import assignment.testytradetest.models.Quote
import assignment.testytradetest.models.WatchList
import assignment.testytradetest.ui.theme.Colors
import assignment.testytradetest.viewModels.ViewModelWatchlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FragmentWatchlist : FragmentBase() {

    private val notAvailable = App.getInstance().getString(R.string.not_available)
    private lateinit var viewModel : ViewModelWatchlist

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewModelWatchlist::class.java)
    }

    @Composable
    override fun setContent() {
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        if (viewModel.showCreateWatchlistDialog) {
            DialogNewWatchlist(listState, scope)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val watchListName = viewModel.getSelectedWatchList()?.name ?: ""
            Text(text = watchListName, fontSize = 35.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(start = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                LazyRow(modifier = Modifier.weight(1f),
                        state = listState,
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    items(viewModel.watchLists) { watchList ->
                        WatchListItem(watchList = watchList)
                    }
                }
                IconButton(modifier = Modifier.size(50.dp),
                           onClick = { viewModel.showCreateWatchlistDialog = true }) {
                    Image(painterResource(id = R.drawable.icon_plus), null)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(1f).padding(start = 5.dp, end = 5.dp)) {
                Text(text = App.getInstance().getString(R.string.symbol), fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(text = App.getInstance().getString(R.string.bid_price), fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(text = App.getInstance().getString(R.string.ask_price), fontSize = 15.sp, modifier = Modifier.weight(1f))
                Text(text = App.getInstance().getString(R.string.last_price), fontSize = 15.sp, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp, top = 5.dp),
                    thickness = 2.dp)
            LazyColumn(modifier = Modifier.padding(start = 5.dp, end = 5.dp).fillMaxWidth(1f).weight(1f)) {
                items(viewModel.quotes) { quote ->
                    Quote(quote = quote)
                }
            }

            Box(modifier = Modifier.fillMaxWidth(1f).padding(start = 15.dp, end = 15.dp)) {
                Button(modifier = Modifier.align(Alignment.CenterStart),
                       onClick = {viewModel.requestMoreQuotes()},
                       enabled = viewModel.selectedWatchListIndex != -1) {
                    Text(text = getString(R.string.add_quotes_to_track))
                }

                Button(modifier = Modifier.align(Alignment.CenterEnd),
                       colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.White),
                       enabled = viewModel.selectedWatchListIndex != -1,
                       onClick = {
                           viewModel.deleteWatchlist()
                           if (viewModel.selectedWatchListIndex != -1) {
                               scope.launch {
                                   listState.animateScrollToItem(viewModel.selectedWatchListIndex)
                               }
                           }
                       }) {
                    Text(text = getString(R.string.delete_watchlist))
                }
            }
        }
    }

    @Composable
    private fun Quote(quote: Quote) {
        Row(modifier = Modifier.fillMaxWidth(1f).padding(bottom = 10.dp).clickable { viewModel.showChart(quote) },
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = quote.symbol, modifier = Modifier.weight(1f))
            Text(text = quote.bidPrice?.toString() ?: notAvailable, modifier = Modifier.weight(1f))
            Text(text = quote.askPrice?.toString() ?: notAvailable, modifier = Modifier.weight(1f))
            Text(text = quote.lastPrice?.toString() ?: notAvailable, modifier = Modifier.weight(1f))
            Box(modifier = Modifier.weight(1f)) {
                Text(text = getString(R.string.remove), maxLines = 1, color = Color.White,
                     modifier = Modifier.align(Alignment.CenterEnd)
                         .background(Color.Red, RoundedCornerShape(5.dp))
                         .padding(5.dp)
                         .clickable { viewModel.removeQuote(quote) })
            }
        }
    }

    @Composable
    private fun DialogNewWatchlist(listState : LazyListState, scope: CoroutineScope) {
        var name by rememberSaveable { mutableStateOf("") }

        StandardDialog(title = getString(R.string.new_watchlist),
                       content = {
                           TextField(value = name,
                                     modifier = Modifier.fillMaxWidth(1f),
                                     singleLine = true,
                                     onValueChange = {name = it})
                       },
                       buttons = arrayOf(getString(R.string.create)),
                       onCancel = {viewModel.showCreateWatchlistDialog = false},
                       onButtonClick = {
                           viewModel.createWatchList(name)
                           if (!viewModel.showCreateWatchlistDialog) {
                               if (viewModel.selectedWatchListIndex != -1) {
                                   scope.launch {
                                       listState.animateScrollToItem(viewModel.selectedWatchListIndex)
                                   }
                               }
                           }
                       }
        )
    }

    @Composable
    private fun WatchListItem(watchList: WatchList) {
        val selectionColor : Color
        if (viewModel.getSelectedWatchList() == watchList) {
            selectionColor = Colors.purple200
        } else {
            selectionColor = Color.Transparent
        }
        Text(modifier = Modifier.background(color = selectionColor).clickable { viewModel.selectWatchList(watchList) },
             text = watchList.name, fontSize = 20.sp)
        if (viewModel.watchLists.indexOf(watchList) != viewModel.watchLists.lastIndex) {
            Divider(modifier = Modifier.padding(start = 10.dp, end = 10.dp).height(25.dp).width(2.dp),
                    color = Colors.purple200)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startGettingQuotes()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopGettingQuotes()
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveState()
    }
}