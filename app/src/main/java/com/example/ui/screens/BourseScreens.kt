package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.BourseViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.StockWatchItem
import java.text.NumberFormat
import java.util.Locale

// Helper extension to format currency
fun Double.formatFcfa(): String {
    val formatter = NumberFormat.getInstance(Locale.FRANCE)
    return "${formatter.format(this.toInt())} FCFA"
}

@Composable
fun BourseMainLayout(viewModel: BourseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe state flows for toast/status messages
    val transactionStatusFlow = viewModel.transactionStatus
    LaunchedEffect(Unit) {
        transactionStatusFlow.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            // Display bottom bar navigation on dashboard screens only
            if (currentScreen in listOf(Screen.DASHBOARD, Screen.MARKET, Screen.PORTFOLIO, Screen.HISTORY, Screen.HELP, Screen.PROFILE)) {
                BourseBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated screen transition
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.WELCOME -> WelcomeScreen(viewModel)
                    Screen.ONBOARDING -> OnboardingScreen(viewModel)
                    Screen.SIGNATURE -> SignatureScreen(viewModel)
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.MARKET -> MarketScreen(viewModel)
                    Screen.DEPOSIT -> DepositScreen(viewModel)
                    Screen.PORTFOLIO -> PortfolioScreen(viewModel)
                    Screen.HISTORY -> HistoryScreen(viewModel)
                    Screen.HELP -> HelpScreen(viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun BourseBottomNavBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.DASHBOARD,
            onClick = { onNavigate(Screen.DASHBOARD) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
            label = { Text("Accueil", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_home")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.MARKET,
            onClick = { onNavigate(Screen.MARKET) },
            icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Marché") },
            label = { Text("Marché", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_market")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.PORTFOLIO,
            onClick = { onNavigate(Screen.PORTFOLIO) },
            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Portefeuille") },
            label = { Text("Portfolio", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_portfolio")
        )
        NavigationBarItem(
            selected = currentScreen == Screen.PROFILE,
            onClick = { onNavigate(Screen.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

// 1. WELCOME SCREEN
@Composable
fun WelcomeScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Glow effect spacer
        Spacer(modifier = Modifier.height(24.dp))

        // Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            ElephantLogoCanvas(
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Bienvenue sur\nÉléphant Bourse",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = DarkOnBackground,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Votre passerelle vers le marché financier ivoirien.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google login simulation
            Button(
                onClick = {
                    if (userProfile != null && userProfile!!.kycStep >= 5) {
                        viewModel.navigateTo(Screen.DASHBOARD)
                    } else {
                        viewModel.navigateTo(Screen.ONBOARDING)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_login_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DarkOnBackground
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google icon procedural sketch
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .drawBehind {
                                drawCircle(color = Color(0xFFEA4335), radius = this.size.width / 2.5f)
                            }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Se connecter avec Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Standard Email signup simulation
            Button(
                onClick = {
                    if (userProfile != null && userProfile!!.kycStep >= 5) {
                        viewModel.navigateTo(Screen.DASHBOARD)
                    } else {
                        viewModel.navigateTo(Screen.ONBOARDING)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("email_login_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Mail, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continuer avec l'e-mail",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trust Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Sécurité",
                        tint = OrangeBrand,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Votre sécurité est notre priorité. Vos données sont chiffrées selon les standards bancaires.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer Linkages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Besoin d'aide ?",
                style = MaterialTheme.typography.labelLarge,
                color = OrangeBrand,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { viewModel.navigateTo(Screen.HELP) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(GrayBorder)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Conditions d'utilisation",
                style = MaterialTheme.typography.labelLarge,
                color = OrangeBrand,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "© 2026 ÉLÉPHANT BOURSE • CÔTE D'IVOIRE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: BourseViewModel) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()

    val firstName by viewModel.firstNameInput.collectAsStateWithLifecycle()
    val lastName by viewModel.lastNameInput.collectAsStateWithLifecycle()
    val birthDate by viewModel.birthDateInput.collectAsStateWithLifecycle()

    val progressValue = when (step) {
        1 -> 0.33f
        2 -> 0.66f
        else -> 1.0f
    }

    val stepTitle = when (step) {
        1 -> "Informations Personnelles"
        2 -> "Vérification d'identité"
        else -> "Justificatif de domicile"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Back toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Inscription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }, enabled = false) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Transparent)
            }
        }

        // Onboarding Progress Header
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Étape $step sur 3 : $stepTitle",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrangeBrand,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progressValue * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ForestGreen,
                trackColor = GrayBorder
            )
        }

        // Step Contents card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (step) {
                    1 -> {
                        Text(
                            text = "Commençons par vous",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Veuillez entrer vos informations de base telles qu'elles apparaissent sur vos documents officiels.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { viewModel.firstNameInput.value = it },
                            label = { Text("Prénom(s)") },
                            placeholder = { Text("ex: Jean-Marc") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firstname_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { viewModel.lastNameInput.value = it },
                            label = { Text("Nom de famille") },
                            placeholder = { Text("ex: Kouassi") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lastname_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { viewModel.birthDateInput.value = it },
                            label = { Text("Date de naissance") },
                            placeholder = { Text("JJ/MM/AAAA") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("birthdate_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        Button(
                            onClick = { viewModel.submitPersonalDetails() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_next_1"),
                            enabled = firstName.isNotBlank() && lastName.isNotBlank() && birthDate.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
                        ) {
                            Text("Continuer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                    2 -> {
                        Text(
                            text = "Vérification d'identité",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sécurisez votre compte en vérifiant votre identité grâce à notre système conforme AMF.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Identity Scan Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = ForestGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pièce d'identité", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("CNI ivoirienne ou Passeport valide", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OrangeBrand)
                        }

                        // Face recognition
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = ForestGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reconnaissance Faciale", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Preuve de vie par selfie vidéo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.CameraFront, contentDescription = null, tint = OrangeBrand)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = OrangeBrand.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = OrangeBrand)
                                Text(
                                    text = "Pourquoi cette étape ? La réglementation de la BRVM nous impose de vérifier l'identité de chaque investisseur pour prévenir la fraude financière.",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.onboardingStep.value = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retour", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.completeIdentityBiometrics() },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp)
                                    .testTag("onboarding_next_2"),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
                            ) {
                                Text("Valider l'ID", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "Justificatif de domicile",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Téléchargez un justificatif datant de moins de 3 mois pour valider l'adresse légale de votre compte.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Big Dashed upload zone simulator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, OrangeBrand.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .clickable { }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(44.dp))
                                Text("Uploader mon document (CIE, SODECI)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("PDF, JPG, PNG (Max 5Mo)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Text("Facture d'électricité (CIE) ou d'eau (SODECI)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Text("Le nom de famille doit correspondre à votre ID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.onboardingStep.value = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retour", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.completeAddressUpload() },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp)
                                    .testTag("onboarding_submit"),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Text("Soumettre mon dossier", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.RocketLaunch, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Compliances footer logos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = DarkOnBackground.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                Text("Chiffrement AES-256", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkOnBackground.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DarkOnBackground.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                Text("RGPD Conforme", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkOnBackground.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.GppGood, contentDescription = null, tint = DarkOnBackground.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                Text("Agréé AMF-UMOA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkOnBackground.copy(alpha = 0.5f))
            }
        }
    }
}

// 3. SIGNATURE / CONTRACT SCREEN
@Composable
fun SignatureScreen(viewModel: BourseViewModel) {
    val context = LocalContext.current
    var signatureMethodIsPad by remember { mutableStateOf(true) } // true = Pad on screen, false = OTP sms
    var signatureDrawn by remember { mutableStateOf(false) }

    val otpList by viewModel.smsOtpCode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Validation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }, enabled = false) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Transparent)
            }
        }

        // Header progress
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dernière étape : Signature du contrat", style = MaterialTheme.typography.labelMedium, color = OrangeBrand, fontWeight = FontWeight.Bold)
                Text("95%", style = MaterialTheme.typography.labelMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { 0.95f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ForestGreen,
                trackColor = GrayBorder
            )
        }

        Text(
            text = "Finalisez votre inscription",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Veuillez réviser les conditions juridiques et apposer votre signature légale pour valider l'ouverture de votre compte titres BRVM.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        // Part 1: Legal Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = OrangeBrand)
                    Text("Engagements Juridiques", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                var checked1 by remember { mutableStateOf(false) }
                var checked2 by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = checked1,
                        onCheckedChange = { checked1 = it },
                        colors = CheckboxDefaults.colors(checkedColor = OrangeBrand)
                    )
                    Column {
                        Text("Tarification SGI en vigueur", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Je déclare accepter la tarification de courtage de la SGI partenaire (0,5% par transaction).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Consulter la grille tarifaire",
                            color = OrangeBrand,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = checked2,
                        onCheckedChange = { checked2 = it },
                        colors = CheckboxDefaults.colors(checkedColor = OrangeBrand)
                    )
                    Column {
                        Text("Politique de protection des données", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "J'autorise Éléphant Bourse à collecter et traiter mes données personnelles aux fins d'ouverture de compte.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Part 2: Signature Selector Tab
        TabRow(
            selectedTabIndex = if (signatureMethodIsPad) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = signatureMethodIsPad,
                onClick = { signatureMethodIsPad = true },
                text = { Text("Signer sur l'Écran", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = !signatureMethodIsPad,
                onClick = { signatureMethodIsPad = false },
                text = { Text("Code SMS OTP", fontWeight = FontWeight.Bold) }
            )
        }

        // Signature interactive component container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (signatureMethodIsPad) {
                SignaturePad(
                    modifier = Modifier.fillMaxSize(),
                    onSignatureDrawn = { signatureDrawn = it }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Entrez le code OTP reçu au +225 •• •• •• 89", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0 until 6) {
                                OutlinedTextField(
                                    value = otpList[i],
                                    onValueChange = { value ->
                                        if (value.length <= 1) {
                                            val newList = otpList.toMutableList()
                                            newList[i] = value
                                            viewModel.smsOtpCode.value = newList
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("otp_digit_$i"),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirmation Actions
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.verifySmsOtp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_signature_button"),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Valider mon compte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    android.widget.Toast.makeText(context, "Téléchargement du contrat d'ouverture...", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Télécharger le contrat (PDF)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. DASHBOARD / ACCUEIL
@Composable
fun DashboardScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangeBrand.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OrangeBrand)
                }
                Column {
                    val displayName = if (userProfile != null && userProfile!!.firstName.isNotBlank()) {
                        "Bonjour, ${userProfile!!.firstName}"
                    } else {
                        "Bonjour, Investisseur"
                    }
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Propulsez votre avenir financier.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(
                onClick = {
                    android.widget.Toast.makeText(context, "Aucune nouvelle notification", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Bourse branding tag
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(100.dp))
                .background(ForestGreen.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text("BOURSE HORIZON", color = ForestGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        // Main Portfolio Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GrayBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "SOLDE TOTAL ESTIMÉ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                val netValue = (userProfile?.portfolioValue ?: 14520000.0) + (userProfile?.cashBalance ?: 125000.0)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = netValue.formatFcfa().substringBefore(" FCFA"),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkOnBackground,
                        fontSize = 32.sp
                    )
                    Text("FCFA", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(ForestGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                        Text("+2.5% (Aujourd'hui)", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.MARKET) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPremium),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("place_order_button")
                    ) {
                        Text("Placer un ordre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats card layout
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(Screen.PORTFOLIO) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIONS DÉTENUES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${holdings.size} Sociétés", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DIVIDENDES REÇUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("45 800 FCFA", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ForestGreen)
                }
            }
        }

        // Grid Menu Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.MARKET) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Acheter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.PORTFOLIO) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Suivre", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.HISTORY) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldPremium, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Historique", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.HELP) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Académie", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Did you know block (Académie / Pédagogie)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeBrand)
                        Text("Le saviez-vous ?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Académie",
                        color = ForestGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.HELP) }
                    )
                }

                Text(
                    text = "Une action est une part du capital d'une entreprise. En en achetant une, vous devenez copropriétaire et pouvez recevoir une partie des bénéfices appelés dividendes.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("PROGRESSION DU MODULE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ForestGreen,
                    trackColor = GrayBorder
                )
            }
        }

        // Market Watch mini list
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("MARCHÉ BRVM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            viewModel.watchlist.take(3).forEach { stock ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectStockAndNavigate(stock.ticker) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stock.ticker, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                            Column {
                                Text(stock.companyName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(stock.sector, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(stock.price.formatFcfa(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val color = if (stock.isGaining) ForestGreen else RedLoss
                            val prefix = if (stock.isGaining) "+" else ""
                            Text("$prefix${stock.changePercent}%", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// 5. MARKET / DETAIL STOCK SCREEN
@Composable
fun MarketScreen(viewModel: BourseViewModel) {
    val selectedStockFlow by viewModel.selectedStock.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Set default selected stock if null
    LaunchedEffect(selectedStockFlow) {
        if (selectedStockFlow == null) {
            viewModel.selectStockAndNavigate("SNTS")
        }
    }

    val stock = selectedStockFlow ?: return

    val qtyString by viewModel.orderQuantity.collectAsStateWithLifecycle()
    val limitPriceString by viewModel.orderLimitPrice.collectAsStateWithLifecycle()
    val isMarketOrder by viewModel.orderTypeIsMarket.collectAsStateWithLifecycle()

    val priceUnit = if (isMarketOrder) stock.price else (limitPriceString.toDoubleOrNull() ?: stock.price)
    val quantity = qtyString.toIntOrNull() ?: 0
    val subtotal = quantity * priceUnit
    val brokerageFees = Math.round(subtotal * 0.005).toDouble()
    val grandTotal = subtotal + brokerageFees

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
                }
                Text(
                    text = "Achat d'Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.navigateTo(Screen.PORTFOLIO) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Portefeuille", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Search ticker bar lookup simulation
        var searchQuery by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    dropdownExpanded = it.isNotBlank()
                },
                placeholder = { Text("Rechercher une action (ex: SNTS, ORAC...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Dynamic dropdown results based on search query
            if (dropdownExpanded) {
                val filtered = viewModel.watchlist.filter {
                    it.ticker.contains(searchQuery, ignoreCase = true) || it.companyName.contains(searchQuery, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 58.dp)
                            .zIndex(10f),
                        border = BorderStroke(1.dp, GrayBorder),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            filtered.forEach { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectStockAndNavigate(match.ticker)
                                            searchQuery = ""
                                            dropdownExpanded = false
                                        }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${match.ticker} - ${match.companyName}", fontWeight = FontWeight.Bold)
                                    Text(match.price.formatFcfa(), color = OrangeBrand)
                                }
                                HorizontalDivider(color = GrayBorder)
                            }
                        }
                    }
                }
            }
        }

        // Ticker details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stock.ticker, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Column {
                        Text(stock.companyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${stock.ticker} • BRVM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(stock.price.formatFcfa(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    val color = if (stock.isGaining) ForestGreen else RedLoss
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (stock.isGaining) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("+${stock.changePercent}%", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Performance Chart Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PERFORMANCE 24H", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ForestGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("DIRECT", color = ForestGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LineTrendChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    color = if (stock.isGaining) ForestGreen else RedLoss,
                    isGaining = stock.isGaining
                )
            }
        }

        // Order Form Widget
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Placer un Ordre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Segmented tabs: Market vs Limit
            TabRow(
                selectedTabIndex = if (isMarketOrder) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = isMarketOrder,
                    onClick = { viewModel.orderTypeIsMarket.value = true },
                    text = { Text("Marché", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = !isMarketOrder,
                    onClick = { viewModel.orderTypeIsMarket.value = false },
                    text = { Text("Cours Limite", fontWeight = FontWeight.Bold) }
                )
            }

            // Info text explanation
            Card(
                colors = CardDefaults.cardColors(containerColor = OrangeBrand.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangeBrand)
                    val textDesc = if (isMarketOrder) {
                        "Ordre au Marché : Votre ordre sera exécuté immédiatement au meilleur prix disponible actuellement sur le carnet d'ordres."
                    } else {
                        "Ordre à cours limité : Vous fixez le prix maximal que vous voulez payer. L'achat ne se fera que si le prix descend à ou sous votre limite."
                    }
                    Text(text = textDesc, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
                }
            }

            // Numeric Inputs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { viewModel.orderQuantity.value = it },
                    label = { Text("Unités (Quantité)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("order_qty_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (!isMarketOrder) {
                    OutlinedTextField(
                        value = limitPriceString,
                        onValueChange = { viewModel.orderLimitPrice.value = it },
                        label = { Text("Prix Limite (FCFA)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_limit_price_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Total recap table
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimation totale", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(subtotal.toDouble().formatFcfa(), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Frais de courtage (0.5%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(brokerageFees.formatFcfa(), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = GrayBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL ESTIMÉ", fontWeight = FontWeight.Bold, color = OrangeBrand)
                        Text(grandTotal.formatFcfa(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = OrangeBrand)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                val availCash = userProfile?.cashBalance ?: 125000.0
                Text(
                    text = "Solde disponible : ${availCash.formatFcfa()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { viewModel.executeOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_order_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Confirmer l'achat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 6. PORTFOLIO / PORTEFEUILLE SCREEN
@Composable
fun PortfolioScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangeBrand.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = OrangeBrand)
                }
                Column {
                    Text("Mon Portefeuille", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Suivez vos investissements.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(
                onClick = { viewModel.navigateTo(Screen.DEPOSIT) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(OrangeBrand.copy(alpha = 0.12f))
                    .testTag("add_funds_header")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dépôt", tint = OrangeBrand)
            }
        }

        // Dashboard Hero value card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "VALEUR TOTALE DU PORTEFEUILLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                val netValue = (userProfile?.portfolioValue ?: 14520000.0) + (userProfile?.cashBalance ?: 125000.0)
                Text(
                    text = netValue.formatFcfa(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkOnBackground,
                    fontSize = 30.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ForestGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                    Text("+5.2% / +75 000 FCFA", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Micro-visualizer graph
                BarVisualizer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }
        }

        // Available Balance box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SOLDE DISPONIBLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    val availCash = userProfile?.cashBalance ?: 125000.0
                    Text(availCash.formatFcfa(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("Prêt à investir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.DEPOSIT) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Déposer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Mes Actions holdings list
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mes Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${holdings.size} Actifs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (holdings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GrayBorder, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aucune action détenue", fontWeight = FontWeight.Bold)
                        Text("Achetez des actions sur l'onglet Marché.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                holdings.forEach { holding ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(holding.ticker, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(holding.companyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val priceVal = holding.currentPrice * holding.sharesCount
                                    Text(priceVal.formatFcfa(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    val prefix = if (holding.changePercent >= 0) "+" else ""
                                    val color = if (holding.changePercent >= 0) ForestGreen else RedLoss
                                    Text("$prefix${holding.changePercent}%", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("TITRES", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${holding.sharesCount}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("VALEUR D'ACHAT MOYENNE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(holding.averagePrice.formatFcfa(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            // Interactive inline sell simulator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.selectStockAndNavigate(holding.ticker) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand.copy(alpha = 0.12f), contentColor = OrangeBrand),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Acheter plus", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        viewModel.executeSale(holding.ticker, holding.sharesCount, holding.currentPrice)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tout Vendre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trading open hour note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeBrand)
                Column {
                    Text("Le saviez-vous ?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Le marché de la BRVM est ouvert de 09:00 à 15:00 GMT. Vos ordres passés en dehors de ces horaires seront placés en file d'attente pour l'ouverture.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// 7. DEPOSIT / RECHARGE FUNDS SCREEN
@Composable
fun DepositScreen(viewModel: BourseViewModel) {
    val amountInput by viewModel.depositAmountInput.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.depositPaymentMethod.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val amtDouble = amountInput.toDoubleOrNull() ?: 0.0
    val formattedAmt = amtDouble.formatFcfa()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
                }
                Text(
                    text = "Alimenter mon compte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { }, enabled = false) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.Transparent)
            }
        }

        // Amount Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Montant du dépôt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = amountInput,
                onValueChange = { viewModel.depositAmountInput.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deposit_amount_field"),
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = OrangeBrand, fontWeight = FontWeight.Bold),
                trailingIcon = { Text("FCFA", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(14.dp))
                Text("Dépôt minimum : 1 000 FCFA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Payment method selector
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Moyen de paiement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val paymentOptions = listOf(
                Pair("Orange Money", "Orange"),
                Pair("Wave CI", "Wave"),
                Pair("Moov Money", "Moov"),
                Pair("Carte Bancaire", "Carte")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paymentOptions.size) { index ->
                    val option = paymentOptions[index]
                    val isSelected = paymentMethod == option.first

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OrangeBrand.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                            .border(2.dp, if (isSelected) OrangeBrand else GrayBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.depositPaymentMethod.value = option.first }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Render nice text placeholder for logo
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (option.second) {
                                            "Orange" -> Color(0xFFFF7900)
                                            "Wave" -> Color(0xFF1DA1F2)
                                            "Moov" -> Color(0xFF005DA4)
                                            else -> GrayBorder
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (option.second == "Carte") {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = DarkOnBackground)
                                } else {
                                    Text(
                                        text = option.second.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            Text(option.first, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Detailed recap
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = OrangeBrand)
                    Text("Récapitulatif", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Montant saisi", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formattedAmt, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Frais de transaction (0%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("0 FCFA", fontWeight = FontWeight.Bold, color = ForestGreen)
                }
                HorizontalDivider(color = GrayBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total à créditer", fontWeight = FontWeight.Bold)
                    Text(formattedAmt, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = OrangeBrand)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Cette transaction sera automatiquement enregistrée dans votre historique d'activités pour un suivi de votre patrimoine.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = { viewModel.executeDeposit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_deposit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirmer le dépôt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

// 8. TRANSACTION HISTORY SCREEN
@Composable
fun HistoryScreen(viewModel: BourseViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tout") } // "Tout", "Dépôts", "Transactions"

    val filteredList = transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) || tx.reference.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            "Dépôts" -> tx.type == "DEPOSIT"
            "Transactions" -> tx.type == "BUY" || tx.type == "SELL"
            else -> true
        }
        matchesSearch && matchesTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top app bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
                }
                Text(
                    text = "Historique",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.Download, contentDescription = "Télécharger", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une entreprise ou référence...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Tabs
        TabRow(
            selectedTabIndex = when (selectedTab) {
                "Dépôts" -> 1
                "Transactions" -> 2
                else -> 0
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(selected = selectedTab == "Tout", onClick = { selectedTab = "Tout" }, text = { Text("Tout") })
            Tab(selected = selectedTab == "Dépôts", onClick = { selectedTab = "Dépôts" }, text = { Text("Dépôts") })
            Tab(selected = selectedTab == "Transactions", onClick = { selectedTab = "Transactions" }, text = { Text("Transactions") })
        }

        // List
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = GrayBorder, modifier = Modifier.size(64.dp))
                    Text("Aucune transaction trouvée", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { tx ->
                    val colorLeftBorder = when (tx.status) {
                        "ANNULÉ" -> GrayBorder
                        "EN ATTENTE" -> OrangeBrand
                        else -> if (tx.type == "DEPOSIT" || tx.type == "SELL") ForestGreen else RedLoss
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    val canvasHeight = this.size.height
                                    val widthPx = 4.dp.toPx()
                                    // Colored vertical left border
                                    drawRect(
                                        color = colorLeftBorder,
                                        topLeft = Offset(0f, 0f),
                                        size = Size(widthPx, canvasHeight)
                                    )
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (tx.type) {
                                                "DEPOSIT" -> ForestGreen.copy(alpha = 0.12f)
                                                "SELL" -> ForestGreen.copy(alpha = 0.12f)
                                                else -> RedLoss.copy(alpha = 0.12f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (tx.type) {
                                            "DEPOSIT" -> Icons.Default.ArrowUpward
                                            "SELL" -> Icons.Default.ShowChart
                                            else -> Icons.Default.ShoppingCart
                                        },
                                        contentDescription = null,
                                        tint = if (tx.type == "DEPOSIT" || tx.type == "SELL") ForestGreen else RedLoss,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${tx.date} • Réf: ${tx.reference}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(
                                                    when (tx.status) {
                                                        "ANNULÉ" -> RedLoss.copy(alpha = 0.15f)
                                                        "EN ATTENTE" -> OrangeBrand.copy(alpha = 0.15f)
                                                        else -> ForestGreen.copy(alpha = 0.15f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tx.status,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (tx.status) {
                                                    "ANNULÉ" -> RedLoss
                                                    "EN ATTENTE" -> OrangeBrand
                                                    else -> ForestGreen
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val prefix = if (tx.type == "DEPOSIT" || tx.type == "SELL") "+" else "-"
                                val textVal = "${prefix} ${Math.abs(tx.amount).formatFcfa()}"
                                Text(
                                    text = textVal,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (tx.type == "DEPOSIT" || tx.type == "SELL") ForestGreen else DarkOnBackground,
                                    fontSize = 14.sp
                                )
                                if (tx.sharesQty > 0) {
                                    Text("${tx.sharesQty} actions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. HELP CENTER / ACADÉMIE SCREEN
@Composable
fun HelpScreen(viewModel: BourseViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedIndex by remember { mutableStateOf<Int?>(null) } // Accordion expansion index

    val faqs = listOf(
        Pair("Comment valider mon KYC ?", "Pour valider votre KYC, rendez-vous dans Profil > Vérification. Vous devrez fournir une pièce d'identité ivoirienne valide (CNI ou Passeport) et un justificatif de domicile de moins de 3 mois (facture CIE ou SODECI)."),
        Pair("Délais pour un retrait Mobile Money ?", "Les retraits vers Orange Money, Wave ou MTN MoMo sont généralement traités en moins de 15 minutes pendant les heures d'ouverture de la bourse (09:00 - 15:00 GMT)."),
        Pair("Comment acheter des actions BRVM ?", "Allez dans l'onglet 'Marché', recherchez une entreprise cotée (ex: Sonatel), cliquez sur 'Acheter', saisissez la quantité désirée et confirmez votre ordre d'achat."),
        Pair("Qu'est-ce que la capitalisation boursière ?", "La capitalisation représente la valeur globale d'une entreprise sur le marché financier. Elle est calculée en multipliant le nombre total d'actions par le cours actuel de l'action."),
        Pair("Quelle est la fiscalité sur les dividendes ?", "En Côte d'Ivoire, les dividendes des sociétés cotées à la BRVM sont totalement exonérés d'impôts (IRVM) pour tous les résidents fiscaux de l'UEMOA, ce qui en fait un investissement très attractif.")
    )

    val filteredFaqs = faqs.filter {
        it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Centre d'Aide & Académie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text("Comment pouvons-nous vous aider ?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une solution...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Grid Bento Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(OrangeBrand.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(18.dp))
                    }
                    Text("Mon Compte", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("KYC, Profil, Sécurité", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                    }
                    Text("Fonds", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Dépôts & Retraits", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(GoldPremium.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldPremium, modifier = Modifier.size(18.dp))
                    }
                    Text("Ordres BRVM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Achat, Vente, Limites", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Red.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                    Text("Sécurité", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("2FA, AMF-UMOA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // FAQs Accordeon
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("FAQ Populaires", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            filteredFaqs.forEachIndexed { index, faq ->
                val isExpanded = expandedIndex == index
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Column(modifier = Modifier.clickable { expandedIndex = if (isExpanded) null else index }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(faq.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Text(
                                text = faq.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Call Support Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Vous ne trouvez pas de réponse ?", fontWeight = FontWeight.Bold)
                Text("Nos conseillers sont à votre disposition de 8h à 18h GMT.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lancer le Chat en direct", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Appeler un conseiller", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 10. USER PROFILE SCREEN
@Composable
fun ProfileScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val nameText = if (userProfile != null && userProfile!!.firstName.isNotBlank()) {
        "${userProfile!!.firstName} ${userProfile!!.lastName}"
    } else {
        "Koffi Kouassi"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Profil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Profile Card Heading
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(96.dp)) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(OrangeBrand.copy(alpha = 0.12f))
                        .border(3.dp, ForestGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(54.dp))
                }

                // Verified Badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(ForestGreen)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Text(nameText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val refID = "EB-${(100000..999999).random()}"
            Text("ID: $refID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ForestGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                    Text("Vérifié KYC", color = ForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Account Details Stats card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("STATUT DU COMPTE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Investisseur Premium", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OrangeBrand)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("MEMBRE DEPUIS", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Janvier 2023", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // Menu items Column list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column {
                val menuItems = listOf(
                    Pair(Icons.Default.Person, "Informations Personnelles"),
                    Pair(Icons.Default.Shield, "Sécurité & Double Facteur (2FA)"),
                    Pair(Icons.Default.AccountBalance, "Coordonnées Bancaires"),
                    Pair(Icons.Default.Notifications, "Préférences de Notifications"),
                    Pair(Icons.Default.Description, "Documents & Contrats"),
                    Pair(Icons.Default.GroupAdd, "Parrainer un ami")
                )

                menuItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(item.first, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(22.dp))
                            Text(item.second, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))
                }
            }
        }

        // Signout / Reset Data action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.resetDemoData() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("reset_demo_button")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Réinitialiser", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.resetDemoData() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Déconnexion", fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Éléphant Bourse v2.4.1 (Stable)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
