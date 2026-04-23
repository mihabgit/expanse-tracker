package com.mihab.expensetracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

data class OnboardingPage(
    val title: String,
    val titleBn: String,
    val description: String,
    val descriptionBn: String,
    val icon: String,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    showSkip: Boolean = false
) {
    val context = LocalContext.current
    val isBengali = Locale.getDefault().language == "bn"
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPage(
            title = "Track Your Expenses",
            titleBn = "আপনার খরচ ট্র্যাক করুন",
            description = "Keep record of every penny you spend and manage your budget efficiently.",
            descriptionBn = "আপনার খরচ করা প্রতি পয়সার রেকর্ড রাখুন এবং আপনার বাজেট দক্ষভাবে পরিচালনা করুন।",
            icon = "💰",
            color = Color(0xFF6200EE)
        ),
        OnboardingPage(
            title = "Quick Add Feature",
            titleBn = "দ্রুত যোগ করার সুবিধা",
            description = "Add frequent expenses with just one tap using our customizable Quick Add feature.",
            descriptionBn = "আমাদের কাস্টমাইজযোগ্য কুইক অ্যাড ফিচার ব্যবহার করে মাত্র এক ট্যাপে সাধারণ খরচগুলো যোগ করুন।",
            icon = "⚡",
            color = Color(0xFF03DAC5)
        ),
        OnboardingPage(
            title = "Visual Summaries",
            titleBn = "ভিজ্যুয়াল সারাংশ",
            description = "Get a clear picture of your spending habits with intuitive charts and category totals.",
            descriptionBn = "সহজ চার্ট এবং ক্যাটাগরি ভিত্তিক টোটালের মাধ্যমে আপনার খরচের অভ্যাস সম্পর্কে একটি পরিষ্কার ধারণা পান।",
            icon = "📊",
            color = Color(0xFFFF9800)
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (showSkip) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                TextButton(onClick = onFinished) {
                    Text(if (isBengali) "এড়িয়ে যান" else "Skip")
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { position ->
            val page = pages[position]
            OnboardingPageUI(page = page, isBengali = isBengali)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Page Indicator
            Row {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(10.dp)
                    )
                }
            }

            // Buttons
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp).width(120.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) {
                        if (isBengali) "শুরু করুন" else "Get Started"
                    } else {
                        if (isBengali) "পরবর্তী" else "Next"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageUI(page: OnboardingPage, isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(200.dp),
            shape = CircleShape,
            color = page.color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(page.icon, fontSize = 100.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = if (isBengali) page.titleBn else page.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isBengali) page.descriptionBn else page.description,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (page.icon == "📊") { // Show on last page
            val context = LocalContext.current
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/shorts/W3UfxJrjliQ?feature=share"))
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📺 " + (if (isBengali) "ভিডিও টিউটোরিয়াল দেখুন" else "Watch Video Tutorial"))
            }
        }
    }
}
