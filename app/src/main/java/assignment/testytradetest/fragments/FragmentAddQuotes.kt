package assignment.testytradetest.fragments

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import assignment.testytradetest.App
import assignment.testytradetest.R
import assignment.testytradetest.composables.StandardDialog
import assignment.testytradetest.ui.theme.Colors
import assignment.testytradetest.viewModels.ViewModelAddQuotes

class FragmentAddQuotes : FragmentBase() {

    private lateinit var viewModel : ViewModelAddQuotes
    private val selectedFormat = App.getInstance().getString(R.string.selected_format)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ViewModelAddQuotes::class.java)
    }

    @Composable
    override fun setContent() {
        if (viewModel.showInternetError) {
            StandardDialog(title = getString(R.string.internet_connection),
                           buttons = arrayOf(getString(R.string.retry)),
                           onCancel = {viewModel.showInternetError = false},
                           onButtonClick = {viewModel.requestData()}
            )
        }

        Column(modifier = Modifier.fillMaxWidth(),
               horizontalAlignment = Alignment.CenterHorizontally) {
                val color = Color(Color.Blue.red, Color.Blue.green, Color.Blue.blue, 0.05f)
            Column(modifier = Modifier.fillMaxWidth().background(color),
                   horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = getString(R.string.search), fontSize = 25.sp)
                Row(modifier = Modifier.fillMaxWidth(1f)) {
                    TextField(value = viewModel.searchSymbol,
                              onValueChange = {viewModel.applySearchFilter(searchSymbol = it)},
                              modifier = Modifier.width(120.dp),
                              singleLine = true,
                              label = {
                                  Text(text = getString(R.string.by_symbol))
                              })
                    Spacer(modifier = Modifier.width(20.dp))
                    TextField(value = viewModel.searchName,
                              onValueChange = {viewModel.applySearchFilter(searchName = it)},
                              modifier = Modifier.fillMaxWidth(1f),
                              singleLine = true,
                              label = {
                                  Text(text = getString(R.string.by_name))
                              })
                }
            }

            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = String.format(selectedFormat, viewModel.selectedSymbols.count()),
                     fontSize = 20.sp)
                Spacer(modifier = Modifier.width(25.dp))
                Text(text = getString(R.string.clear_selections),
                     color = Color.White,
                     fontSize = 20.sp,
                     modifier = Modifier.background(color = Colors.purple700, shape = CircleShape).
                                padding(5.dp)
                                .clickable {
                    viewModel.selectedSymbols.clear()
                })
            }

            Divider(thickness = 3.dp)

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(viewModel.filteredSymbolDescriptions) { symbolDescription ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val isSelected = viewModel.selectedSymbols.contains(symbolDescription)
                            Checkbox(checked = isSelected,
                                     onCheckedChange = {viewModel.changeSelection(symbolDescription, it)})
                            Text(text = symbolDescription.symbol)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = symbolDescription.name)
                        }
                        Divider()
                    }
                }
            }

            Button(onClick = {viewModel.addSymbolsToTrack()}) {
                Text(text = getString(R.string.add_quotes_to_track))
            }
        }
    }
}