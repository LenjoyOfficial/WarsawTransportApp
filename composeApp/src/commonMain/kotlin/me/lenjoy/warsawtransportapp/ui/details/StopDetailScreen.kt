package me.lenjoy.warsawtransportapp.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.lenjoy.warsawtransportapp.api.model.Departure
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import me.lenjoy.warsawtransportapp.api.model.ServiceTime
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.repository.FavoritesRepository
import me.lenjoy.warsawtransportapp.repository.TransportRepository
import me.lenjoy.warsawtransportapp.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import warsawtransportapp.composeapp.generated.resources.Res
import warsawtransportapp.composeapp.generated.resources.error_fetching_data
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailScreen(
	stop: StopLocation,
	repository: TransportRepository,
	favoritesRepository: FavoritesRepository,
	onBack: () -> Unit = {},
	onDepartureClick: (Departure) -> Unit = {}
) {
	var departures by remember { mutableStateOf<List<Departure>?>(null) }
	var isLoading by remember { mutableStateOf(true) }
	var isFavorite by remember {
		mutableStateOf(favoritesRepository.isFavorite(stop.stopGroupId.value, stop.stopPoleNumber.value))
	}
	val snackbarHostState = remember { SnackbarHostState() }
	val errorMessage = stringResource(Res.string.error_fetching_data)

	LaunchedEffect(stop.stopGroupId.value, stop.stopPoleNumber.value) {
		isLoading = true
		try {
			val routes = repository.getRoutes()
			val allLines = repository.getStopLines(stop.stopGroupId.value, stop.stopPoleNumber.value)

			val allDepartures = coroutineScope {
				allLines.map { line ->
					async {
						repository.getDepartures(
							stop.stopGroupId.value,
							stop.stopPoleNumber.value,
							line.line.value,
							routes
						)
					}
				}.awaitAll().flatten()
			}

			val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
			val currentMinutes = now.hour * 60 + now.minute

			departures = allDepartures
				.filter { (it.serviceTime.dayOffset * 1440 + it.serviceTime.hourOfDay * 60 + it.serviceTime.minute) >= currentMinutes }
				.sortedBy { it.serviceTime.dayOffset * 1440 + it.serviceTime.hourOfDay * 60 + it.serviceTime.minute }

		} catch (_: Exception) {
			snackbarHostState.showSnackbar(errorMessage)
		} finally {
			isLoading = false
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("${stop.stopName} ${stop.stopPoleNumber.value} ${stop.stopGroupId.value}") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "Back"
						)
					}
				},
				actions = {
					IconButton(onClick = {
						favoritesRepository.toggleFavorite(stop)
						isFavorite = favoritesRepository.isFavorite(stop.stopGroupId.value, stop.stopPoleNumber.value)
					}) {
						Icon(
							imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
							contentDescription = "Toggle Favorite",
							tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant
				)
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { innerPadding ->
		StopDetailContent(
			departures = departures ?: emptyList(),
			isLoading = isLoading,
			onDepartureClick = onDepartureClick,
			modifier = Modifier.padding(innerPadding)
		)
	}
}

@Composable
fun StopDetailContent(
	departures: List<Departure>,
	isLoading: Boolean,
	onDepartureClick: (Departure) -> Unit = {},
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(16.dp)
	) {
		Text(
			text = "Departures",
			style = MaterialTheme.typography.titleMedium
		)
		Spacer(modifier = Modifier.height(8.dp))

		if (isLoading) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				CircularProgressIndicator()
			}
		} else if (departures.isEmpty()) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				Text(
					text = "No more departures today",
					style = MaterialTheme.typography.bodyLarge
				)
			}
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(vertical = 8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(departures) { departure ->
					DepartureCard(
						departure = departure,
						onClick = { onDepartureClick(departure) }
					)
				}
			}
		}
	}
}

@Composable
fun DepartureCard(
	departure: Departure,
	onClick: () -> Unit = {}
) {
	Card(
		onClick = onClick,
		modifier = Modifier.fillMaxWidth(),
	) {
		Row(
			modifier = Modifier
				.padding(16.dp)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Column {
				Text(
					text = departure.line.value,
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.Bold
				)
				departure.direction?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
			Text(
				text = "${departure.serviceTime.hourOfDay.toString().padStart(2, '0')}:${
					departure.serviceTime.minute.toString().padStart(2, '0')
				}",
				style = MaterialTheme.typography.headlineSmall,
				color = MaterialTheme.colorScheme.primary
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun StopDetailScreenPreview() {
	val sampleDepartures = listOf(
		Departure(
			line = LineNumber("17"),
			route = "A",
			direction = "Winnica",
			serviceTime = ServiceTime("12:05:00", 0, 12, 5, 0),
			brigade = "1",
			symbol1 = null,
			symbol2 = null
		),
		Departure(
			line = LineNumber("189"),
			route = "B",
			direction = "Sadyba",
			serviceTime = ServiceTime("12:15:00", 0, 12, 15, 0),
			brigade = "2",
			symbol1 = null,
			symbol2 = null
		)
	)

	AppTheme {
		Scaffold(
			topBar = {
				TopAppBar(
					title = { Text("Centrum 01") },
					navigationIcon = {
						IconButton(onClick = {}) {
							Icon(
								imageVector = Icons.AutoMirrored.Filled.ArrowBack,
								contentDescription = "Back"
							)
						}
					},
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = MaterialTheme.colorScheme.surfaceVariant
					)
				)
			}
		) { innerPadding ->
			StopDetailContent(
				departures = sampleDepartures,
				isLoading = false,
				modifier = Modifier.padding(innerPadding)
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun StopDetailScreenLoadingPreview() {
	AppTheme {
		Scaffold { innerPadding ->
			StopDetailContent(
				departures = emptyList(),
				isLoading = true,
				modifier = Modifier.padding(innerPadding)
			)
		}
	}
}
