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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import me.lenjoy.warsawtransportapp.api.model.RouteLine
import me.lenjoy.warsawtransportapp.api.model.RouteStop
import me.lenjoy.warsawtransportapp.api.model.StopLocation
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.Res
import me.lenjoy.warsawtransportapp.composeapp.generated.resources.error_fetching_data
import me.lenjoy.warsawtransportapp.repository.TransportRepository
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
	line: String,
	routeName: String,
	repository: TransportRepository,
	onBack: () -> Unit
) {
	var routeLine by remember { mutableStateOf<RouteLine?>(null) }
	var stops by remember { mutableStateOf<Map<String, StopLocation>>(emptyMap()) }
	var isLoading by remember { mutableStateOf(true) }
	val snackbarHostState = remember { SnackbarHostState() }
	val errorMessage = stringResource(Res.string.error_fetching_data)

	LaunchedEffect(line, routeName) {
		isLoading = true
		try {
			val allRoutes = repository.getRoutes(line)
			routeLine = allRoutes.find { it.routeName == routeName }
			
			val allStops = repository.getAllStops()
			stops = allStops.associateBy { "${it.stopGroupId.value}_${it.stopPoleNumber.value}" }
		} catch (_: Exception) {
			snackbarHostState.showSnackbar(errorMessage)
		} finally {
			isLoading = false
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Route $line") },
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
		},
		snackbarHost = { SnackbarHost(snackbarHostState) }
	) { innerPadding ->
		Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
			if (isLoading) {
				CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
			} else if (routeLine == null) {
				Text(
					text = "Route not found",
					modifier = Modifier.align(Alignment.Center),
					style = MaterialTheme.typography.bodyLarge
				)
			} else {
				RouteContent(routeLine!!, stops)
			}
		}
	}
}

@Composable
fun RouteContent(
	routeLine: RouteLine,
	stops: Map<String, StopLocation>
) {
	Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
		Text(
			text = "Variant: ${routeLine.routeName}",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.secondary
		)
		Spacer(modifier = Modifier.height(16.dp))
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(vertical = 8.dp)
		) {
			items(routeLine.stops.sortedBy { it.sequence }) { stop ->
				RouteStopItem(stop, stops["${stop.stopGroupId}_${stop.stopPoleNumber}"])
				HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
			}
		}
	}
}

@Composable
fun RouteStopItem(
	routeStop: RouteStop,
	stopLocation: StopLocation?
) {
	Row(
		modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = routeStop.sequence.toString(),
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.width(32.dp)
		)
		Column {
			Text(
				text = stopLocation?.stopName ?: "Unknown Stop (${routeStop.stopGroupId})",
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium
			)
			Row {
				Text(
					text = "${routeStop.stopGroupId} ${routeStop.stopPoleNumber}",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.outline
				)
				stopLocation?.direction?.let {
					Text(
						text = " → $it",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.outline
					)
				}
			}
		}
	}
}
