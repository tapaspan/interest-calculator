package com.example.excelformula

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FormulaCalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun FormulaCalculatorScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var dateFromText by remember { mutableStateOf("") }
    var dateToText by remember { mutableStateOf(LocalDate.now().toString()) }
    var deliveryAmountText by remember { mutableStateOf("2500") }
    var interestRateText by remember { mutableStateOf("0.02") }
    var kText by remember { mutableStateOf("2860") }
    var oText by remember { mutableStateOf("6.8") }

    val dateFrom = parseLocalDateOrNull(dateFromText)
    val dateTo = parseLocalDateOrNull(dateToText) ?: LocalDate.now()

    val months = remember(dateFrom, dateTo) {
        if (dateFrom == null) 0 else datedifMonths(dateFrom, dateTo)
    }

    val deliveryAmount = deliveryAmountText.toDoubleOrNull() ?: 0.0
    val interestRate = interestRateText.toDoubleOrNull() ?: 0.0
    val k = kText.toDoubleOrNull() ?: 0.0
    val o = oText.toDoubleOrNull() ?: 0.0

    val interest = interestRate * deliveryAmount * months
    val total = deliveryAmount + interest
    val costing = k * o

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
        horizontalAlignment = Alignment.Start) {

        Text("Excel → Android: loan/costing calculator", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = dateFromText,
            onValueChange = { dateFromText = it },
            label = { Text("Date From (yyyy-MM-dd)") },
            placeholder = { Text("2016-07-10") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dateToText,
            onValueChange = { dateToText = it },
            label = { Text("Current Date (yyyy-MM-dd) — default TODAY") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = deliveryAmountText,
                onValueChange = { deliveryAmountText = it },
                label = { Text("Delivery Amount (F)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = interestRateText,
                onValueChange = { interestRateText = it },
                label = { Text("Interest Rate (monthly decimal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = kText,
                onValueChange = { kText = it },
                label = { Text("K (quantity)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = oText,
                onValueChange = { oText = it },
                label = { Text("O (rate per unit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Results:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(6.dp))

        ResultRow(label = "Completed months (DATEDIF m):", value = months.toString())
        ResultRow(label = "Interest (interestRate * F * months):", value = formatDouble(interest))
        ResultRow(label = "Total (F + Interest):", value = formatDouble(total))
        ResultRow(label = "Costing (K * O):", value = formatDouble(costing))

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val csv = "dateFrom,dateTo,months,delivery,interestRate,interest,total,k,o,costing\n" +
                        "${dateFromText},${dateToText},$months,$deliveryAmount,$interestRate,${formatDouble(interest)},${formatDouble(total)},$k,$o,${formatDouble(costing)}"
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(csv))
                Toast.makeText(context, "Results copied to clipboard (CSV)", Toast.LENGTH_SHORT).show()
            }) {
                Text("Copy CSV")
            }

            Button(onClick = {
                if (dateFrom == null) {
                    Toast.makeText(context, "Please enter valid Date From (yyyy-MM-dd)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Computation done — see results", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Validate / Run")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Notes: Months are computed as Excel's DATEDIF(...,\"m\"). Interest rate expected as decimal (0.02 = 2%).",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(value)
    }
}

fun parseLocalDateOrNull(text: String): LocalDate? {
    return try {
        if (text.isBlank()) null else LocalDate.parse(text)
    } catch (e: Exception) {
        null
    }
}

fun datedifMonths(start: LocalDate, end: LocalDate): Int {
    var months = (end.year - start.year) * 12 + (end.monthValue - start.monthValue)
    if (end.dayOfMonth < start.dayOfMonth) months -= 1
    if (months < 0) months = 0
    return months
}

fun formatDouble(v: Double): String {
    return String.format("%.2f", v)
}
