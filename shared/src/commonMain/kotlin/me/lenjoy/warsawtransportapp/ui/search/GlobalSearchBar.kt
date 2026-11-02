package me.lenjoy.warsawtransportapp.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.util.normalizeForSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchBar(
    isLoading: Boolean,
    stops: List<StopLocation>,
    onStopSelected: (StopLocation) -> Unit = {}
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val isExpanded = searchBarState.currentValue == SearchBarValue.Expanded

    val filteredStops by remember(stops) {
        derivedStateOf {
            val query = textFieldState.text.toString().normalizeForSearch()
            if (query.isEmpty()) emptyList()
            else stops.filter { it.stopName.normalizeForSearch().contains(query) }
                .sortedBy { it.stopName }
        }
    }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            onSearch = {},
            placeholder = { Text("Search for stops") },
            leadingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
					if (isExpanded)
						IconButton(onClick = {
							scope.launch {
								searchBarState.animateToCollapsed()
							}
						}) {
							Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
						}
					else
                    	Icon(Icons.Default.Search, contentDescription = null)
                }
            }
        )
    }

    AppBarWithSearch(
        inputField = inputField,
        state = searchBarState,
		colors = SearchBarDefaults.appBarWithSearchColors(
			appBarContainerColor = Color.Transparent,
		)
    )
    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
        content = {
            LazyColumn {
                items(filteredStops) { stop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStopSelected(stop)
                                scope.launch {
                                    searchBarState.animateToCollapsed()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stop.stopName} ${stop.stopPoleNumber.value}",
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    )
}
