package com.aber.app.data

import com.aber.app.R

/** Sample posts copied from the Figma design (local only, no server). */
object PostRepository {

    val posts: List<Post> = listOf(
        Post(
            id = 1,
            type = PostType.LOST,
            poster = "Juan Dela Cruz",
            postedAgo = "2h",
            itemType = "Wallet",
            place = "Burnham Park",
            date = "September 23, 2026",
            details = "Brown leather wallet with light-colored stitching along the edges. " +
                "There is an embroidered letter on the lower right corner of the wallet.",
            claimAt = null,
            imageRes = R.drawable.sample_wallet,
            keywords = setOf("wallet", "leather", "bag", "handbag", "brown", "coin purse", "fashion accessory", "tan")
        ),
        Post(
            id = 2,
            type = PostType.LOST,
            poster = "Maria Magdalena",
            postedAgo = "4h",
            itemType = "Tumbler",
            place = "Lower Session Road",
            date = "September 30, 2026",
            details = "Mint green tumbler with flowers and leaf design. " +
                "There is an attached yellow green flower accessory on the straw.",
            claimAt = null,
            imageRes = R.drawable.sample_tumbler,
            keywords = setOf("tumbler", "cup", "mug", "drinkware", "bottle", "water bottle", "straw", "drink", "green", "tableware")
        ),
        Post(
            id = 3,
            type = PostType.FOUND,
            poster = "Juan Dela Cruz",
            postedAgo = "2h",
            itemType = "Cell Phone",
            place = "Irisan Dumpsite",
            date = "October 10, 2026",
            details = "Grey Infinix GT 30 Pro 5G.",
            claimAt = "Baguio City Police Office Station 9",
            imageRes = R.drawable.sample_phone,
            keywords = setOf("cell phone", "phone", "mobile phone", "smartphone", "gadget", "electronic device", "communication device", "telephone", "grey", "gray")
        ),
        Post(
            id = 4,
            type = PostType.FOUND,
            poster = "Maria Magdalena",
            postedAgo = "4h",
            itemType = "Tumbler",
            place = "Melvin Jones Soccer Field",
            date = "August 10, 2026",
            details = "Dark grey Aqua Flask with stickers.",
            claimAt = "Baguio City Police Office Station 1",
            imageRes = R.drawable.sample_aquaflask,
            keywords = setOf("tumbler", "bottle", "water bottle", "drinkware", "flask", "aqua flask", "sticker", "drink", "grey", "gray")
        )
    )
}
