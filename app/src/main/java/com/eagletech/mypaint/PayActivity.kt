package com.eagletech.mypaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazon.device.drm.LicensingService
import com.amazon.device.iap.PurchasingListener
import com.amazon.device.iap.PurchasingService
import com.amazon.device.iap.model.FulfillmentResult
import com.amazon.device.iap.model.ProductDataResponse
import com.amazon.device.iap.model.PurchaseResponse
import com.amazon.device.iap.model.PurchaseUpdatesResponse
import com.amazon.device.iap.model.UserDataResponse
import com.eagletech.mypaint.dataApp.MyData
import com.eagletech.mypaint.databinding.ActivityPayBinding

class PayActivity : AppCompatActivity() {
    private lateinit var buyBinding: ActivityPayBinding
    private lateinit var myData: MyData
    private lateinit var currentUserId: String
    private lateinit var currentMarketplace: String

    // Phải thêm sku các gói vào ứng dụng
    companion object {
        const val save5image = "com.eagletech.rainbow.save5"
        const val save10image = "com.eagletech.rainbow.save10"
        const val save15image = "com.eagletech.rainbow.save15"
        const val subsave = "com.eagletech.rainbow.subpremium"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buyBinding = ActivityPayBinding.inflate(layoutInflater)
        setContentView(buyBinding.root)
        myData = MyData.getInstance(this)
        setupIAPOnCreate()
        setClickItems()

    }

    private fun setClickItems() {
        buyBinding.save5img.setOnClickListener {
            myData.addSaves(2)
//            PurchasingService.purchase(save5image)
        }
        buyBinding.save10img.setOnClickListener {
            PurchasingService.purchase(save10image)
        }
        buyBinding.save15img.setOnClickListener {
            PurchasingService.purchase(save15image)
        }
        buyBinding.subsaveimg.setOnClickListener {
            PurchasingService.purchase(subsave)
//            dataSharedPreferences.isPremium = true
        }
        buyBinding.finish.setOnClickListener { finish() }
    }

    private fun setupIAPOnCreate() {
        val purchasingListener: PurchasingListener = object : PurchasingListener {
            override fun onUserDataResponse(response: UserDataResponse) {
                when (response.requestStatus!!) {
                    UserDataResponse.RequestStatus.SUCCESSFUL -> {
                        currentUserId = response.userData.userId
                        currentMarketplace = response.userData.marketplace
                        myData.currentUserId(currentUserId)
                        Log.v("IAP SDK", "loaded userdataResponse")
                    }

                    UserDataResponse.RequestStatus.FAILED, UserDataResponse.RequestStatus.NOT_SUPPORTED ->
                        Log.v("IAP SDK", "loading failed")
                }
            }

            override fun onProductDataResponse(productDataResponse: ProductDataResponse) {
                when (productDataResponse.requestStatus) {
                    ProductDataResponse.RequestStatus.SUCCESSFUL -> {
                        val products = productDataResponse.productData
                        for (key in products.keys) {
                            val product = products[key]
                            Log.v(
                                "Product:", String.format(
                                    "Product: %s\n Type: %s\n SKU: %s\n Price: %s\n Description: %s\n",
                                    product!!.title,
                                    product.productType,
                                    product.sku,
                                    product.price,
                                    product.description
                                )
                            )
                        }
                        //get all unavailable SKUs
                        for (s in productDataResponse.unavailableSkus) {
                            Log.v("Unavailable SKU:$s", "Unavailable SKU:$s")
                        }
                    }

                    ProductDataResponse.RequestStatus.FAILED -> Log.v("FAILED", "FAILED")
                    else -> {}
                }
            }

            override fun onPurchaseResponse(purchaseResponse: PurchaseResponse) {
                when (purchaseResponse.requestStatus) {
                    PurchaseResponse.RequestStatus.SUCCESSFUL -> {

                        if (purchaseResponse.receipt.sku == save5image) {
                            myData.addSaves(5)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == save10image) {
                            myData.addSaves(10)
                            finish()
                        }
                        if (purchaseResponse.receipt.sku == save15image) {
                            myData.addSaves(15)
                            finish()
                        }

                        if (purchaseResponse.receipt.sku == subsave) {
                            myData.isPremiumSaves = true
                            finish()
                        }
                        PurchasingService.notifyFulfillment(
                            purchaseResponse.receipt.receiptId,
                            FulfillmentResult.FULFILLED
                        )


                        Log.v("FAILED", "FAILED")
                    }

                    PurchaseResponse.RequestStatus.FAILED -> {}
                    else -> {}
                }
            }

            override fun onPurchaseUpdatesResponse(response: PurchaseUpdatesResponse) {
                // Process receipts
                when (response.requestStatus) {
                    PurchaseUpdatesResponse.RequestStatus.SUCCESSFUL -> {
                        for (receipt in response.receipts) {
                            myData.isPremiumSaves = !receipt.isCanceled
                        }
                        if (response.hasMore()) {
                            PurchasingService.getPurchaseUpdates(false)
                        }

                    }

                    PurchaseUpdatesResponse.RequestStatus.FAILED -> Log.d("FAILED", "FAILED")
                    else -> {}
                }
            }
        }
        PurchasingService.registerListener(this, purchasingListener)
        Log.d(
            "DetailBuyAct",
            "Appstore SDK Mode: " + LicensingService.getAppstoreSDKMode()
        )
    }

    override fun onResume() {
        super.onResume()
        PurchasingService.getUserData()
        val productSkus: MutableSet<String> = HashSet()
        productSkus.add(subsave)
        productSkus.add(save5image)
        productSkus.add(save10image)
        productSkus.add(save15image)
        PurchasingService.getProductData(productSkus)
        PurchasingService.getPurchaseUpdates(false)
    }
}