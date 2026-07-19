package com.example.data

import com.example.data.local.BourseDao
import com.example.data.local.HoldingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class BourseRepository(private val bourseDao: BourseDao) {

    val userProfile: Flow<UserEntity?> = bourseDao.getUserProfileFlow()
    val allTransactions: Flow<List<TransactionEntity>> = bourseDao.getAllTransactionsFlow()
    val allHoldings: Flow<List<HoldingsEntity>> = bourseDao.getAllHoldingsFlow()

    suspend fun initializeDefaultData() {
        // Check if user profile already exists
        val currentProfile = bourseDao.getUserProfile()
        if (currentProfile == null) {
            // Set up default User
            val defaultUser = UserEntity(
                firstName = "",
                lastName = "",
                birthDate = "",
                kycStep = 0, // Starts at Login Screen
                cashBalance = 125000.0, // Available balance in FCFA
                portfolioValue = 14520000.0,
                isPremium = true,
                membershipDate = "Janvier 2023"
            )
            bourseDao.insertUserProfile(defaultUser)

            // Insert default holdings
            val initialHoldings = listOf(
                HoldingsEntity("SNTS", "Sonatel", 450, 16200.0, 16200.0, 2.45, "Télécoms"),
                HoldingsEntity("ETI", "Ecobank Transnational", 120000, 18.0, 18.0, 1.12, "Banque"),
                HoldingsEntity("ORAC", "Orange CI", 200, 10450.0, 10450.0, -0.85, "Télécoms")
            )
            for (holding in initialHoldings) {
                bourseDao.insertHolding(holding)
            }

            // Insert default transactions
            val initialTransactions = listOf(
                TransactionEntity(type = "BUY", title = "Achat Sonatel CI", date = "Aujourd'hui, 14:20", reference = "SN-8291", status = "TERMINÉ", amount = -45000.0, sharesQty = 12, stockTicker = "SNTS"),
                TransactionEntity(type = "DEPOSIT", title = "Dépôt Orange Money", date = "Aujourd'hui, 09:15", reference = "Portefeuille", status = "TERMINÉ", amount = 150000.0),
                TransactionEntity(type = "SELL", title = "Vente BOA Bénin", date = "Hier, 16:45", reference = "BJ-4421", status = "TERMINÉ", amount = 82500.0, sharesQty = 15, stockTicker = "BOAB"),
                TransactionEntity(type = "BUY", title = "Achat Onatel BF", date = "Hier, 11:30", reference = "BF-1102", status = "EN ATTENTE", amount = -12400.0, sharesQty = 4, stockTicker = "ONAB"),
                TransactionEntity(type = "DEPOSIT", title = "Dépôt Wave", date = "Hier, 08:00", reference = "Portefeuille", status = "ANNULÉ", amount = 5000.0)
            )
            for (tx in initialTransactions) {
                bourseDao.insertTransaction(tx)
            }
        }
    }

    suspend fun saveUserProfile(user: UserEntity) {
        bourseDao.insertUserProfile(user)
    }

    suspend fun depositFunds(amount: Double, paymentMethod: String): Boolean {
        if (amount <= 0) return false
        val profile = bourseDao.getUserProfile() ?: return false
        
        val updatedProfile = profile.copy(
            cashBalance = profile.cashBalance + amount
        )
        bourseDao.insertUserProfile(updatedProfile)

        val dateFormat = SimpleDateFormat("Aujourd'hui, HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val reference = "REF-" + (100000..999999).random()

        val depositTransaction = TransactionEntity(
            type = "DEPOSIT",
            title = "Dépôt $paymentMethod",
            date = dateString,
            reference = reference,
            status = "TERMINÉ",
            amount = amount
        )
        bourseDao.insertTransaction(depositTransaction)
        return true
    }

    suspend fun buyStock(
        ticker: String,
        companyName: String,
        sharesQty: Int,
        price: Double,
        feesPercent: Double = 0.005,
        sector: String
    ): String {
        if (sharesQty <= 0 || price <= 0) return "Quantité ou prix invalide."
        val profile = bourseDao.getUserProfile() ?: return "Profil utilisateur introuvable."

        val subtotal = sharesQty * price
        val fees = Math.round(subtotal * feesPercent).toDouble()
        val totalCost = subtotal + fees

        if (profile.cashBalance < totalCost) {
            return "Solde insuffisant pour effectuer cet achat."
        }

        // Update profile balance
        val updatedProfile = profile.copy(
            cashBalance = profile.cashBalance - totalCost,
            portfolioValue = profile.portfolioValue + subtotal
        )
        bourseDao.insertUserProfile(updatedProfile)

        // Update stock holding
        val existingHolding = bourseDao.getHoldingByTicker(ticker)
        if (existingHolding != null) {
            val totalShares = existingHolding.sharesCount + sharesQty
            val newAvgPrice = ((existingHolding.sharesCount * existingHolding.averagePrice) + (sharesQty * price)) / totalShares
            val updatedHolding = existingHolding.copy(
                sharesCount = totalShares,
                averagePrice = newAvgPrice,
                currentPrice = price // Update with latest traded price
            )
            bourseDao.insertHolding(updatedHolding)
        } else {
            val newHolding = HoldingsEntity(
                ticker = ticker,
                companyName = companyName,
                sharesCount = sharesQty,
                averagePrice = price,
                currentPrice = price,
                changePercent = 0.0,
                sector = sector
            )
            bourseDao.insertHolding(newHolding)
        }

        // Log transaction
        val dateFormat = SimpleDateFormat("Aujourd'hui, HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val reference = "SN-" + (1000..9999).random()

        val buyTransaction = TransactionEntity(
            type = "BUY",
            title = "Achat $companyName",
            date = dateString,
            reference = reference,
            status = "TERMINÉ",
            amount = -subtotal,
            sharesQty = sharesQty,
            stockTicker = ticker
        )
        bourseDao.insertTransaction(buyTransaction)

        return "SUCCESS"
    }

    suspend fun sellStock(
        ticker: String,
        sharesQty: Int,
        price: Double,
        feesPercent: Double = 0.005
    ): String {
        val existingHolding = bourseDao.getHoldingByTicker(ticker) ?: return "Vous ne possédez pas d'actions de cette entreprise."
        if (existingHolding.sharesCount < sharesQty) return "Nombre d'actions insuffisant."
        
        val profile = bourseDao.getUserProfile() ?: return "Profil utilisateur introuvable."

        val subtotal = sharesQty * price
        val fees = Math.round(subtotal * feesPercent).toDouble()
        val totalCredit = subtotal - fees

        // Update profile balance
        val updatedProfile = profile.copy(
            cashBalance = profile.cashBalance + totalCredit,
            portfolioValue = profile.portfolioValue - subtotal
        )
        bourseDao.insertUserProfile(updatedProfile)

        // Update stock holding
        if (existingHolding.sharesCount == sharesQty) {
            bourseDao.deleteHolding(existingHolding)
        } else {
            val updatedHolding = existingHolding.copy(
                sharesCount = existingHolding.sharesCount - sharesQty,
                currentPrice = price
            )
            bourseDao.insertHolding(updatedHolding)
        }

        // Log transaction
        val dateFormat = SimpleDateFormat("Aujourd'hui, HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val reference = "VS-" + (1000..9999).random()

        val sellTransaction = TransactionEntity(
            type = "SELL",
            title = "Vente ${existingHolding.companyName}",
            date = dateString,
            reference = reference,
            status = "TERMINÉ",
            amount = subtotal,
            sharesQty = sharesQty,
            stockTicker = ticker
        )
        bourseDao.insertTransaction(sellTransaction)

        return "SUCCESS"
    }
}
