package scraping

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.TextDelegate
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.window.*
import java.math.RoundingMode


import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.eachText
import it.skrape.selects.html5.*
import it.skrape.fetcher.extractIt
import kotlinx.coroutines.launch


import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment


data class StudioName(var name: String)
data class Studio(var name: String, var email: String, var phone: String, var address: String)
data class ExtractedStudio(var studioList: List<Studio> = listOf())

data class ExtractedStudios(var studios: List<StudioName> = listOf())

data class Member(val name: String)

data class ExtractedTeam(var members: List<Member> = listOf())
enum class MenuState { INVOICES, ABOUT_APP }

@Composable
fun StatusBar(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(meniBarva)
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        when (menuState.value) {
            MenuState.INVOICES -> Text("You are viewing the Invoices tab")
            MenuState.ABOUT_APP -> Text("You are viewing the About App tab")
        }
    }
}

@Composable
@Preview
fun App() {
    val menuState = remember { mutableStateOf(MenuState.ABOUT_APP) }
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxHeight(),
            // verticalArrangement = Arrangement.SpaceBetween
        ) {
            Scaffold(
                topBar = { Menu(menuState) },
                bottomBar = { StatusBar(menuState) }
            ) {
                Content(menuState)
            }
        }


    }
}

var meniBarva = Color(0xFF9575CD)
var izdelkiBarva = Color(0xFFEAE3F5)
var izdelkiBarva2 = Color(0xFFeef5e3)

@Composable
fun Menu(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .background(meniBarva)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier
                .clickable { menuState.value = MenuState.INVOICES }
                .padding(20.dp)

        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = "")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cleverfit Studiji")
        }

        Row(
            modifier = Modifier
                .clickable { menuState.value = MenuState.ABOUT_APP }
                .padding(20.dp)
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "About App")

        }
    }


}

@Composable
fun Content(menuState: MutableState<MenuState>, modifier: Modifier = Modifier) {
    when (menuState.value) {
        MenuState.INVOICES -> StudiosTab()
        MenuState.ABOUT_APP -> AboutAppTab()
    }
}

@Composable
fun StudiosTab() {
    val extractedStudios = skrape(HttpFetcher) {   // <-- pass any Fetcher, e.g. HttpFetcher, BrowserFetcher, ...
        request {
            url = "https://www.clever-fit.com/sl/studios/"
        }

        extractIt<ExtractedStudios> {

            htmlDocument {
                header {
                    withClass = "eael-entry-header"

                    h2 {
                        withClass = "eael-entry-title"
                        a {
                            it.studios = findAll {
                                val studiosName: MutableList<StudioName> = mutableListOf()
                                for (str in eachText) {
                                    studiosName.add(StudioName(str))
                                }
                                studiosName
                            }

                        }
                    }
                }
            }
        }
    }
    var listNames = mutableListOf<String>()
    for (studioName in extractedStudios.studios) {
        listNames.add(studioName.name.lowercase())
    }
    for (i in listNames.indices) {
        var names = listNames[i]
        names = names.replace('š', 's')
        names = names.replace('ž', 'z')
        names = names.replace('č', 'c')
        names = names.replace(" ", "-")
        listNames[i] = names
    }
    val studiji: MutableList<Studio> = mutableListOf()
    for (name in listNames) {
        skrape(HttpFetcher) {   // <-- pass any Fetcher, e.g. HttpFetcher, BrowserFetcher, ...
            request {
                url = "https://www.clever-fit.com/sl/studios/$name/"
            }
            if (name == "ljubljana-rudnik") {
                var studio = Studio(" ", " ", " ", "")
                response {
                    htmlDocument {
                        div {
                            withClass = "studio__info-bar--studio-name"
                            h2 {
                                findFirst {
                                    studio.name = text
                                }
                            }

                        }

                        div {
                            withClass = "studio__info-bar--mail"
                            a {
                                findFirst {
                                    studio.email = text
                                }
                            }

                        }
                        div {
                            withClass = "studio__info-bar--address"
                            a {
                                findFirst {
                                    studio.address = text

                                }
                            }
                        }
                        studiji.add(studio)
                    }
                }
            } else {
                var studio = Studio(" ", " ", " ", "")
                response {
                    htmlDocument {
                        div {
                            withClass = "studio__info-bar--studio-name"
                            h2 {
                                findFirst {
                                    studio.name = text
                                }
                            }

                        }

                        div {
                            withClass = "studio__info-bar--mail"
                            a {
                                findFirst {
                                    studio.email = text
                                }
                            }

                        }
                        div {
                            withClass = "studio__info-bar--phone"
                            a {
                                findFirst {
                                    studio.phone = text

                                }
                            }

                        }
                        div {
                            withClass = "studio__info-bar--address"
                            a {
                                findFirst {
                                    studio.address = text

                                }
                            }
                        }
                        studiji.add(studio)
                    }
                }
            }
        }
    }
        val listState = rememberLazyListState()
        var editingStudio by remember { mutableStateOf<Studio?>(null) }
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(
                    items = studiji
                ) { studio ->
                    if (studio == editingStudio) {
                        EditableItemsRow(studio) {
                            editingStudio = null // Save and exit edit mode
                        }
                    } else {
                        ItemsRow(studio) {
                            editingStudio = studio // Enter edit mode
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(listState)
            )
        }

    }

@Composable
fun ItemsRow(studio: Studio, onEditClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        backgroundColor = izdelkiBarva2
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = studio.name,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )}
            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = studio.email,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = studio.phone,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))

            }
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = studio.address,
                    modifier = Modifier.weight(1f)
                )
            }
        }

    }
}
@Composable
fun EditableItemsRow(studio: Studio, onSaveClick: () -> Unit) {
    var name by remember { mutableStateOf(studio.name) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ime studija") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                studio.name = name
                Button(onClick = onSaveClick) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun AboutAppTab() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About application",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
        )
        Text(text = "Predmet: Principi programskih jezikov")
        Text(text = "Avtorica: Katja Bezjak")
    }
}

fun main() = application {


    val extractedTeam = skrape(HttpFetcher) {   // <-- pass any Fetcher, e.g. HttpFetcher, BrowserFetcher, ...
        request {
            url = "https://bodifit-akademija.si/akademija/"
        }

        extractIt<ExtractedTeam> {

            htmlDocument {
                div {
                    withClass = "elementor-widget-container"
                    h3 {
                        withClass = "widget-title"
                        it.members = findAll {
                            val members: MutableList<Member> = mutableListOf()
                            for (str in eachText) {
                                members.add(Member(str))
                            }
                            members
                        }
                    }

                }
            }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Vodenje računov",
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(800.dp, 800.dp)
        ),
        undecorated = false,
        resizable = true
    ) {
        App()
    }
}






