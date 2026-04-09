package com.mihab.expensetracker.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mihab.expensetracker.ui.components.AdBanner
import com.mihab.expensetracker.util.LocaleHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: ExpenseViewModel,
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = LocaleHelper.getCurrency(context)
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val totalSpend = categoryTotals.sumOf { it.totalAmount }
    val isBengali = Locale.getDefault().language == "bn"
    var monthOffset by remember { mutableIntStateOf(0) }

    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.MONTH, monthOffset)
    val monthName = if (isBengali) {
        val months = arrayOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        months[calendar.get(java.util.Calendar.MONTH)] + " " + calendar.get(java.util.Calendar.YEAR)
    } else {
        java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    LaunchedEffect(monthOffset) {
        viewModel.selectMonth(monthOffset)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isBengali) "মাসিক সারসংক্ষেপ" else "Monthly Summary",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            //AdBanner()
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            //item { Spacer(modifier = Modifier.height(8.dp)) }

            // Month Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { monthOffset-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
                    }
                    Text(
                        monthName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { monthOffset++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
                    }
                }
            }

            // Total Spend Hero Card
            item {
                val isDark = isSystemInDarkTheme()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isDark) {
                                        listOf(
                                            Color(0xFF00BFA5),
                                            Color(0xFF00796B)
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.tertiary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            )
                            .padding(24.dp)
                    ) {
                        // Background decorative shapes
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .offset(x = 30.dp, y = 30.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        )

                        Column {
                            Text(
                                if (isBengali) "মোট মাসিক খরচ" else "Total Monthly Spend",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.5.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$currency ${"%.2f".format(totalSpend)}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp
                                )
                            )
                        }
                    }
                }
            }

            // Pie Chart Section
            if (categoryTotals.isNotEmpty()) {
                item {
                    Text(
                        if (isBengali) "চিত্রভিত্তিক বিভাজন" else "Visual Breakdown", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        ExpensePieChart(categoryTotals = categoryTotals)
                    }
                }
            }

            item {
                Text(
                    if (isBengali) "বিভাগভিত্তিক পরিসংখ্যান" else "Category Statistics",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (categoryTotals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isBengali) "এখনও কোন তথ্য নেই" else "No data available yet", color = Color.Gray)
                    }
                }
            } else {
                items(categoryTotals) { total ->
                    val percentage = if (totalSpend > 0) (total.totalAmount / totalSpend).toFloat() else 0f

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(getCategoryEmoji(total.category), fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        getCategoryName(total.category, isBengali),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    "$currency ${"%.2f".format(total.totalAmount)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(percentage)
                                        .fillMaxHeight()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBengali)
                                    "মোট মাসিক বাজেটের ${(percentage * 100).toInt()}%"
                                else
                                    "${(percentage * 100).toInt()}% of total monthly budget",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ExpensePieChart(categoryTotals: List<com.mihab.expensetracker.data.local.CategoryTotal>) {
    val entries = categoryTotals.map { 
        PieEntry(it.totalAmount.toFloat(), it.category)
    }

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.TRANSPARENT)
                setEntryLabelColor(android.graphics.Color.BLACK)
                setEntryLabelTextSize(12f)
                setTransparentCircleAlpha(0)
                centerText = "Breakdown"
                setCenterTextSize(16f)
                setCenterTextColor(android.graphics.Color.GRAY)
                legend.isEnabled = false
            }
        },
        modifier = Modifier.fillMaxSize().padding(16.dp),
        update = { chart ->
            val dataSet = PieDataSet(entries, "").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 12f
                sliceSpace = 4f
            }
            chart.data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(chart))
            }
            chart.setUsePercentValues(true)
            chart.animateY(1000)
            chart.invalidate()
        }
    )
}
