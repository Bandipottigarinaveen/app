package com.simats.echohealth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.google.android.material.button.MaterialButton

class SubscriptionActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var btnSubscribe: MaterialButton
    private lateinit var btnSkipForNow: MaterialButton
    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null

    companion object {
        private const val TAG = "SubscriptionActivity"
        private const val SUBSCRIPTION_SKU = "echohealth_premium_subscription"
        private const val TEST_SUBSCRIPTION_SKU = "android.test.purchased" // For testing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        // If user is already premium, skip this screen
        val isPremium = getSharedPreferences("subscription_prefs", MODE_PRIVATE)
            .getBoolean("is_premium_user", false)
        if (isPremium) {
            navigateToMain()
            return
        }

        addDebugInformation()
        initializeViews()
        try { btnSubscribe.bringToFront() } catch (e: Exception) {}
        setupBillingClient()
        setupClickListeners()
    }

    private fun addDebugInformation() {
        Log.d(TAG, "=== DEBUG INFORMATION ===")
        Log.d(TAG, "Package name: ${packageName}")

        // Get version info from PackageManager
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            Log.d(TAG, "Version code: ${packageInfo.longVersionCode}")
            Log.d(TAG, "Version name: ${packageInfo.versionName}")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to get package info: ${e.message}")
        }

        Log.d(TAG, "Product ID: $SUBSCRIPTION_SKU")
        Log.d(TAG, "Test Product ID: $TEST_SUBSCRIPTION_SKU")
        Log.d(TAG, "=========================")
    }

    private fun initializeViews() {
        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnSkipForNow = findViewById(R.id.btnSkipForNow)
        btnSubscribe.isEnabled = true
        btnSubscribe.text = "Start Premium"
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    querySubscriptionDetails()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
            }
        })
    }

    private fun querySubscriptionDetails() {
        // First try to query your real subscription product
        querySpecificProduct(SUBSCRIPTION_SKU, BillingClient.ProductType.SUBS) { success ->
            if (!success) {
                Log.w(TAG, "Real subscription product not found, trying test products...")
                // If real product fails, try test product for development
                querySpecificProduct(TEST_SUBSCRIPTION_SKU, BillingClient.ProductType.INAPP) { testSuccess ->
                    if (!testSuccess) {
                        Log.e(TAG, "Both real and test products failed")
                        showNoProductsAvailable()
                        runOnUiThread { btnSubscribe.isEnabled = true }
                    } else {
                        runOnUiThread { btnSubscribe.isEnabled = true }
                    }
                }
            } else {
                runOnUiThread { btnSubscribe.isEnabled = true }
            }
        }
    }

    private fun querySpecificProduct(productId: String, productType: String, callback: (Boolean) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList != null && !productDetailsList.isEmpty()) {
                    productDetails = productDetailsList.get(0)
                    Log.d(TAG, "Product details retrieved successfully for: $productId")

                    // Log subscription offers for debugging
                    if (productType == BillingClient.ProductType.SUBS) {
                        val offers = productDetails?.subscriptionOfferDetails
                        if (offers != null && !offers.isEmpty()) {
                            Log.d(TAG, "Available subscription offers: ${offers.size}")
                            var index = 0
                            for (offer in offers) {
                                Log.d(TAG, "Offer ${index}: basePlanId=${offer.basePlanId}, offerToken=${offer.offerToken}")
                                index += 1
                            }
                        } else {
                            Log.w(TAG, "No subscription offers found")
                        }
                    }
                    callback(true)
                } else {
                    Log.e(TAG, "No product details found for: $productId")
                    callback(false)
                }
            } else {
                Log.e(TAG, "Failed to query product details for $productId: ${billingResult.debugMessage}")
                callback(false)
            }
        }
    }

    private fun showNoProductsAvailable() {
        runOnUiThread {
            Toast.makeText(this, "No subscription products available. Check your setup in Play Console.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        btnSkipForNow.setOnClickListener {
            // Navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        btnSubscribe.setOnClickListener {
            Log.d(TAG, "Start Premium clicked")
            if (!::billingClient.isInitialized || !billingClient.isReady) {
                Toast.makeText(this, "Initializing billing...", Toast.LENGTH_SHORT).show()
                setupBillingClient()
                return@setOnClickListener
            }
            if (productDetails == null) {
                // Try subscription first, then force a test in-app product so you can see the sheet
                querySpecificProduct(SUBSCRIPTION_SKU, BillingClient.ProductType.SUBS) { subsOk ->
                    if (subsOk) {
                        launchSubscriptionFlow()
                    } else {
                        querySpecificProduct(TEST_SUBSCRIPTION_SKU, BillingClient.ProductType.INAPP) { testOk ->
                            if (testOk) {
                                Toast.makeText(this, "Test product loaded", Toast.LENGTH_SHORT).show()
                                launchSubscriptionFlow()
                            } else {
                                Toast.makeText(this, "Products not available. Check Play Console/test setup.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                return@setOnClickListener
            }
            launchSubscriptionFlow()
        }
    }

    private fun launchSubscriptionFlow() {
        // Check if billing client is ready
        if (!billingClient.isReady) {
            Log.e(TAG, "Billing client is not ready")
            Toast.makeText(this, "Billing service not ready. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        if (productDetails != null) {
            val productDetailsParamsList = if (productDetails!!.productType == BillingClient.ProductType.SUBS) {
                // Handle subscription products
                val subscriptionOfferDetails = productDetails!!.subscriptionOfferDetails

                if (subscriptionOfferDetails == null || subscriptionOfferDetails.isEmpty()) {
                    Log.e(TAG, "No subscription offers available")
                    Toast.makeText(this, "No subscription offers available", Toast.LENGTH_SHORT).show()
                    return
                }

                val selectedOffer = subscriptionOfferDetails.get(0)
                Log.d(TAG, "Using subscription offer: basePlanId=${selectedOffer.basePlanId}, offerToken=${selectedOffer.offerToken}")

                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails!!)
                        .setOfferToken(selectedOffer.offerToken)
                        .build()
                )
            } else {
                // Handle in-app products (including test products)
                Log.d(TAG, "Using in-app product: ${productDetails!!.productId}")
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails!!)
                        .build()
                )
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Billing flow launched successfully")
            } else {
                Log.e(TAG, "Failed to launch billing flow: ${billingResult.debugMessage}")
                Toast.makeText(this, "Failed to start subscription process: ${billingResult.debugMessage}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e(TAG, "No product details available")
            Toast.makeText(this, "Subscription not available. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated called - Response Code: ${billingResult.responseCode}")
        Log.d(TAG, "Debug Message: ${billingResult.debugMessage}")

        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Purchase successful")
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled the purchase")
                Toast.makeText(this, "Purchase canceled", Toast.LENGTH_SHORT).show()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                Log.d(TAG, "Item already owned")
                Toast.makeText(this, "You already have an active subscription", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.e(TAG, "Item unavailable - This usually means:")
                Log.e(TAG, "1. App version doesn't match Play Console")
                Log.e(TAG, "2. Subscription not active in Play Console")
                Log.e(TAG, "3. App not downloaded from Play Store")
                Log.e(TAG, "4. Testing account not properly configured")
                Toast.makeText(this, "Subscription unavailable. Please download app from Play Store for testing.", Toast.LENGTH_LONG).show()
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.e(TAG, "Developer error - Check:")
                Log.e(TAG, "1. App signature in Play Console")
                Log.e(TAG, "2. Package name matches exactly")
                Log.e(TAG, "3. App uploaded to testing track")
                Toast.makeText(this, "Configuration error. Check Play Console setup.", Toast.LENGTH_LONG).show()
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Log.e(TAG, "Billing service unavailable")
                Toast.makeText(this, "Google Play services unavailable. Try again later.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Log.e(TAG, "Purchase failed with code: ${billingResult.responseCode}")
                Log.e(TAG, "Debug message: ${billingResult.debugMessage}")
                Toast.makeText(this, "Purchase failed: ${getResponseCodeMessage(billingResult.responseCode)}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getResponseCodeMessage(responseCode: Int): String {
        return when (responseCode) {
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "Service timeout"
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "Feature not supported"
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Service disconnected"
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing unavailable"
            BillingClient.BillingResponseCode.NETWORK_ERROR -> "Network error"
            else -> "Unknown error (Code: $responseCode)"
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged successfully")
                        onSubscriptionSuccess()
                    } else {
                        Log.e(TAG, "Failed to acknowledge purchase: ${billingResult.debugMessage}")
                    }
                }
            } else {
                onSubscriptionSuccess()
            }
        }
    }

    private fun onSubscriptionSuccess() {
        Toast.makeText(this, "Subscription successful! Welcome to Premium!", Toast.LENGTH_LONG).show()

        // Save subscription status
        val sharedPref = getSharedPreferences("subscription_prefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_premium_user", true)
            putLong("subscription_time", System.currentTimeMillis())
            apply()
        }

        navigateToMain()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}
