package assignment.testytradetest.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import assignment.testytradetest.ui.theme.Colors

@Composable
fun StandardDialog(title:String = "", content:@Composable (()->Unit)? = null, cancelOnOutsideTouch:Boolean = true, vararg buttons:String,
                   onCancel:()->Unit = {}, onButtonClick:(buttonIndex:Int)->Unit = {}) {
    AlertDialog(onDismissRequest = { onCancel() },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(1f)) {
                        Text(text = title, fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
                    }
                },
                text = content,
                buttons = {
                    Row(modifier = Modifier.fillMaxWidth(1f).padding(bottom = 10.dp), horizontalArrangement = Arrangement.Center) {
                        for (i in buttons.indices) {
                            Text(text = buttons[i],
                                 fontSize = 20.sp,
                                 color = Color.White,
                                 modifier = Modifier
                                     .padding(end = 10.dp, start = 10.dp)
                                     .background(Colors.purple500, CircleShape)
                                     .padding(end = 10.dp, start = 10.dp)
                                     .clickable { onButtonClick(i) }
                            )
                        }
                    }
                },
                properties = DialogProperties(dismissOnClickOutside = cancelOnOutsideTouch)
    )
}
