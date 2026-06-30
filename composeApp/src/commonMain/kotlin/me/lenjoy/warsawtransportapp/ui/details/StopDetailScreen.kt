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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import me.lenjoy.warsawtransportapp.api.model.Departure
import me.lenjoy.warsawtransportapp.api.model.LineNumber
import me.lenjoy.warsawtransportapp.api.model.ServiceTime
import me.lenjoy.warsawtransportapp.repository.TransportRepository
import me.lenjoy.warsawtransportapp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailScreen(
	stopGroupId: String,
	stopPoleNumber: String,
	stopName: String,
	repository: TransportRepository,
	onBack: () -> Unit = {}
) {
	var departures by remember { mutableStateOf<List<Departure>?>(null) }
	var isLoading by remember { mutableStateOf(true) }

	LaunchedEffect(stopGroupId, stopPoleNumber) {
		isLoading = true
		try {
			val routes = repository.getRoutes()
			val allLines = repository.getStopLines(stopGroupId, stopPoleNumber)

			val allDepartures = mutableListOf<Departure>()
			allLines.forEach { line ->
				allDepartures.addAll(
					repository.getDepartures(
						stopGroupId,
						stopPoleNumber,
						line.line.value,
						routes
					)
				)
			}
			departures = allDepartures.sortedBy { it.serviceTime.hourOfDay * 60 + it.serviceTime.minute }
		} catch (e: Exception) {
			// Handle error
		} finally {
			isLoading = false
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(stopName) },
				navigationIcon = {
					IconButton(onClick = onBack) {
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
			stopName = stopName,
			stopPoleNumber = stopPoleNumber,
			departures = departures ?: emptyList(),
			isLoading = isLoading,
			modifier = Modifier.padding(innerPadding)
		)
	}
}

@Composable
fun StopDetailContent(
	stopName: String,
	stopPoleNumber: String,
	departures: List<Departure>,
	isLoading: Boolean,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(16.dp)
	) {

		Text(
			text = stopName,
			style = MaterialTheme.typography.titleLarge.merge(
				fontWeight = FontWeight.Bold
			)
		)
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = stopPoleNumber,
			style = MaterialTheme.typography.bodyLarge
		)

		Spacer(modifier = Modifier.height(16.dp))
		HorizontalDivider()
		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "Departures",
			style = MaterialTheme.typography.titleMedium
		)
		Spacer(modifier = Modifier.height(8.dp))

		if (isLoading) {
			Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				CircularProgressIndicator()
			}
		} else {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(vertical = 8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				items(departures) { departure ->
					DepartureCard(departure)
				}
			}
		}
	}
}

@Composable
fun DepartureCard(departure: Departure) {
	Card(
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
					title = { Text("Centrum") },
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
				stopName = "Centrum",
				stopPoleNumber = "01",
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
				stopName = "Centrum",
				stopPoleNumber = "01",
				departures = emptyList(),
				isLoading = true,
				modifier = Modifier.padding(innerPadding)
			)
		}
	}
}
