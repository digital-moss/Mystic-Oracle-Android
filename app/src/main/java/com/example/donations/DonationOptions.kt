package com.example.donations

/**
 * Public contribution links are intentionally kept in one place so the Android UI
 * can be reused by a future Compose Multiplatform client.
 *
 * Replace the blank values with the project's public handles or support page.
 * Never put private keys, wallet seed phrases, or payment credentials here.
 */
data class DonationOption(
    val name: String,
    val description: String,
    val uri: String
)

object DonationOptions {
    val all: List<DonationOption> = listOf(
        DonationOption("Support website", "One page for cards and other payment methods", "https://www.handmeups.io"),
        DonationOption("PayPal", "Send a contribution through PayPal", "https://www.paypal.me/handmeupsonly"),
        DonationOption("Cash App", "Send a contribution through Cash App", "https://cash.app/\$maechle"),
        DonationOption("Chime", "Send a contribution through Chime", "https://app.chime.com/link/qr?u=Maechle"),
        DonationOption("Bitcoin", "Open the project's public crypto wallet page", "bitcoin:bc1q5wkxyfr7qpva9n3t9wdxxqtakj0n84mfsmazx7")
    )
}
