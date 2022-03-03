/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.SkuType.INAPP
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import .R
import .common.*
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Vad Nik
 * @version dated Sep 06, 2019.
 * @link https://github.com/vadniks
 */
private class BillingImpl(private val context: Context, im: IIndividualModel) : IndividualModelWrapper(im), Billing {
    private val skuAll = decodeHardcodedWithoutEquals("")
    private val skuAds = decodeHardcodedWithoutEquals("")
    private val skus = arrayOf(skuAll, skuAds)
    private lateinit var client: BillingClient
    private val messages = ArrayList<String>()
    private val onFailure: (msg: String?) -> Unit = {
        if (it != null) {
            model.logToFile(it)
            messages.add(it)
        }

        if (model.isActivityShown)
            onUIRendered()
    }

    init {
        model.crossPresenterModel.subscribeForUIRendered(this)
    }

    override fun onUIRendered() {
        doInCoroutine {
            val iterator = messages.iterator()
            while (iterator.hasNext()) {
                val a = iterator.next()
                doInUI { toast(context.getString(R.string.an_error_occurred) + ' ' + a, context, true) }
                iterator.remove()
            }
            messages.clear()
        }
    }

    override fun startForChecking(callback: (b: Billing) -> Unit) = startForChecking(skus) {
        for (i in it) {
            if (i.first == skuAll)
                Vars.areAllEnabled = i.second
            else
                Vars.areAdsDisabled = i.second
        }

        callback(this)
        client.endConnection()
    }

    /*

    -- All paid features --

    Dark theme,
    Encryption,
    Audios,
    Drawns,
    Widgets,
    Span,
    Colored,
    Sync,
    NoAds

    -- End --

    Changed to All and Ads
    Then this functional was removed

    */

    private fun startForChecking(whats: Array<String>, callback: (whats: Array<Pair<String, Boolean>>) -> Unit) = initClient {
        val _whats = Array(whats.size) { Pair(STR_EMPTY, false) }

        for ((j, i) in whats.withIndex())
            _whats[j] = Pair(i, isPurchased(i))

        callback(_whats)
    }

    override fun startForPurchasing(activity: Activity) = initClient {
        BottomSheetDialog(R.menu.menu_skus, activity, isDark(activity)) { id, instance ->
            instance.dismiss()

            val which =
                when (id) {
                    R.id.menu_skus_all -> skuAll
                    R.id.menu_skus_ads -> skuAds
                    else -> throw IllegalArgumentException()
                }

            getSku(which) { what, br ->
                if (what == null || br.responseCode != OK) {
                    onFailure(context.getString(R.string.err_pur) + ' ' + context.getString(R.string.error_code_) + br.responseCode + ' ' + br.debugMessage)
                    return@getSku
                }

                initFlow(client, what, activity)
            }

        }.show()
    }

    private fun initClient(callback: () -> Unit) {
        client = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this::onPurchasesUpdated).build()
        
        client.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode != OK)
                    onFailure(context.getString(R.string.err_init_billing) + ' ' + context.getString(R.string.error_code_) + billingResult.responseCode + ' ' + billingResult.debugMessage)
                else
                    callback()
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    private fun getSku(which: String, callback: (what: SkuDetails?, br: BillingResult) -> Unit) {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(Collections.singletonList(which))
            .setType(INAPP)
            .build()

        client.querySkuDetailsAsync(params) { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->

            val what =
                if (billingResult.responseCode == OK && skuDetailsList != null)
                    skuDetailsList[0]
                else
                    null

            callback(what, billingResult)
        }
    }

    private fun initFlow(client: BillingClient, skuDetails: SkuDetails, activity: Activity) {
        if (isPurchased(skuDetails.sku)) {
            onFailure(context.getString(R.string.alrd_pur))
            return
        }

        client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build())
    }

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        var needExit = true

        if (billingResult.responseCode == OK && purchases != null) {
            for (i in purchases) {
                if (i.purchaseState == PURCHASED && !i.isAcknowledged) {
                    acknowledge(i)
                    needExit = false
                }
                
                if (i.purchaseState == Purchase.PurchaseState.PENDING)
                    toast(context.getString(R.string.purchase_waiting), context)
            }
        } else
            needExit = false

        if (needExit) {
            client.endConnection()
            restart(context)
        }
    }

    private fun acknowledge(p: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(p.purchaseToken)
            .setDeveloperPayload(p.developerPayload)
            .build()

        client.acknowledgePurchase(params, this::onAcknowledgeResult)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onAcknowledgeResult(billingResult: BillingResult) {
        client.endConnection()
        restart(context)
    }

    private fun isPurchased(sku: String): Boolean =
        client
            .queryPurchases(INAPP)
            .purchasesList
            ?.find { it.sku == sku && it.purchaseState == PURCHASED } != null

    override fun areAllEnabled(): Boolean = Vars.areAllEnabled

    override fun areAdsDisabled(): Boolean = Vars.areAdsDisabled

    private object Vars {
        var areAllEnabled = false
        var areAdsDisabled = false
    }
}
