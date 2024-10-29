package tddy.ko.cardstack.feature

data class Image(
    val asset: String,
    val description: String
)

val images = listOf(
    Image(asset = "file:///android_asset/img_01.jpg", description = "Image 1"),
    Image(asset = "file:///android_asset/img_02.jpg", description = "Image 2"),
    Image(asset = "file:///android_asset/img_03.jpg", description = "Image 3"),
    Image(asset = "file:///android_asset/img_04.jpg", description = "Image 4"),
    Image(asset = "file:///android_asset/img_05.jpg", description = "Image 5"),
    Image(asset = "file:///android_asset/img_06.jpg", description = "Image 6")
)
