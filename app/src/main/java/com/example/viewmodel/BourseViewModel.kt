package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.BourseRepository
import com.example.data.local.AppDatabase
import com.example.data.local.HoldingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    WELCOME,
    ONBOARDING,
    SIGNATURE,
    DASHBOARD,
    MARKET,
    PORTFOLIO,
    DEPOSIT,
    HISTORY,
    HELP,
    PROFILE
}

data class StockWatchItem(
    val ticker: String,
    val companyName: String,
    val sector: String,
    val price: Double,
    val changePercent: Double,
    val isGaining: Boolean = changePercent >= 0
)

class BourseViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "bourse_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val repository = BourseRepository(database.bourseDao())

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.WELCOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen navigation stack to support going back (simplifies single activity routing)
    private val screenHistory = mutableListOf<Screen>()

    // Selected stock for Market Detail View
    private val _selectedStock = MutableStateFlow<StockWatchItem?>(null)
    val selectedStock: StateFlow<StockWatchItem?> = _selectedStock.asStateFlow()

    // Observable Flows from Room
    val userProfile: StateFlow<UserEntity?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val holdings: StateFlow<List<HoldingsEntity>> = repository.allHoldings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Watchlist stock constants
    val watchlist = listOf(
        StockWatchItem("SNTS", "Sonatel CI", "Télécoms", 16850.0, 2.45),
        StockWatchItem("BOAB", "BOA Bénin", "Banque", 5900.0, -0.45),
        StockWatchItem("ORAC", "Orange CI", "Télécoms", 10450.0, -0.85),
        StockWatchItem("ETI", "Ecobank Transnational", "Banque", 18.0, 1.12),
        StockWatchItem("ONAB", "Onatel BF", "Télécoms", 3100.0, 0.0),
        StockWatchItem("SOGC", "SOGB CI", "Agriculture", 4200.0, 1.85)
    )

    // Order state variables (bound to view)
    val orderQuantity = MutableStateFlow("10")
    val orderLimitPrice = MutableStateFlow("16850")
    val orderTypeIsMarket = MutableStateFlow(true) // true = Marché, false = Limite

    // Deposit state variables
    val depositAmountInput = MutableStateFlow("")
    val depositPaymentMethod = MutableStateFlow("Orange Money")

    // Onboarding form state
    val firstNameInput = MutableStateFlow("")
    val lastNameInput = MutableStateFlow("")
    val birthDateInput = MutableStateFlow("")
    val onboardingStep = MutableStateFlow(1) // 1 = Personal Details, 2 = Biometrics KYC, 3 = Proof of Address

    // SMS contract validation variables
    val smsOtpCode = MutableStateFlow(listOf("", "", "", "", "", ""))
    val signatureBytes = MutableStateFlow<ByteArray?>(null)

    // General transaction status / toast message
    private val _transactionStatus = MutableSharedFlow<String>()
    val transactionStatus: SharedFlow<String> = _transactionStatus.asSharedFlow()

    init {
        viewModelScope.launch {
            // Seed database if empty
            repository.initializeDefaultData()
            
            // Check if user is already signed up and verified to auto-skip splash
            val profile = repository.userProfile.first()
            if (profile != null && profile.kycStep >= 5) {
                _currentScreen.value = Screen.DASHBOARD
            }
        }
    }

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        } else {
            _currentScreen.value = Screen.WELCOME
        }
    }

    fun selectStockAndNavigate(ticker: String) {
        val stock = watchlist.find { it.ticker == ticker }
        if (stock != null) {
            _selectedStock.value = stock
            orderLimitPrice.value = stock.price.toInt().toString()
            navigateTo(Screen.MARKET)
        }
    }

    // Onboarding Form actions
    fun submitPersonalDetails() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(
                firstName = firstNameInput.value,
                lastName = lastNameInput.value,
                birthDate = birthDateInput.value,
                kycStep = 2 // Move to Identity validation step
            )
            repository.saveUserProfile(updated)
            onboardingStep.value = 2
        }
    }

    fun completeIdentityBiometrics() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(kycStep = 3) // Move to Proof of address
            repository.saveUserProfile(updated)
            onboardingStep.value = 3
        }
    }

    fun completeAddressUpload() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(kycStep = 4) // Move to Legal Signature
            repository.saveUserProfile(updated)
            navigateTo(Screen.SIGNATURE)
        }
    }

    fun verifySmsOtp() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(kycStep = 5) // Fully verified
            repository.saveUserProfile(updated)
            _transactionStatus.emit("Inscription validée avec succès !")
            navigateTo(Screen.DASHBOARD)
        }
    }

    // Reset profile back to standard demo values
    fun resetDemoData() {
        viewModelScope.launch {
            val profile = repository.userProfile.first()
            if (profile != null) {
                val resetProfile = UserEntity(
                    firstName = "",
                    lastName = "",
                    birthDate = "",
                    kycStep = 0,
                    cashBalance = 125000.0,
                    portfolioValue = 14520000.0
                )
                repository.saveUserProfile(resetProfile)
                onboardingStep.value = 1
                firstNameInput.value = ""
                lastNameInput.value = ""
                birthDateInput.value = ""
                _currentScreen.value = Screen.WELCOME
                screenHistory.clear()
            }
        }
    }

    // Transaction Actions
    fun executeDeposit() {
        val amt = depositAmountInput.value.toDoubleOrNull()
        if (amt == null || amt < 1000) {
            viewModelScope.launch {
                _transactionStatus.emit("Le dépôt minimum est de 1 000 FCFA.")
            }
            return
        }

        viewModelScope.launch {
            val success = repository.depositFunds(amt, depositPaymentMethod.value)
            if (success) {
                depositAmountInput.value = ""
                _transactionStatus.emit("Dépôt de ${amt.toInt()} FCFA effectué avec succès !")
                navigateTo(Screen.DASHBOARD)
            } else {
                _transactionStatus.emit("Échec du dépôt.")
            }
        }
    }

    fun executeOrder() {
        val qty = orderQuantity.value.toIntOrNull() ?: 0
        if (qty <= 0) {
            viewModelScope.launch { _transactionStatus.emit("Veuillez saisir une quantité valide.") }
            return
        }

        val stock = _selectedStock.value ?: return
        val price = if (orderTypeIsMarket.value) stock.price else (orderLimitPrice.value.toDoubleOrNull() ?: stock.price)

        viewModelScope.launch {
            val result = repository.buyStock(
                ticker = stock.ticker,
                companyName = stock.companyName,
                sharesQty = qty,
                price = price,
                sector = stock.sector
            )

            if (result == "SUCCESS") {
                _transactionStatus.emit("Achat de $qty actions ${stock.companyName} effectué !")
                navigateTo(Screen.DASHBOARD)
            } else {
                _transactionStatus.emit(result)
            }
        }
    }

    fun executeSale(ticker: String, sharesQty: Int, price: Double) {
        viewModelScope.launch {
            val result = repository.sellStock(ticker, sharesQty, price)
            if (result == "SUCCESS") {
                _transactionStatus.emit("Vente de $sharesQty actions effectuée !")
            } else {
                _transactionStatus.emit(result)
            }
        }
    }
}
