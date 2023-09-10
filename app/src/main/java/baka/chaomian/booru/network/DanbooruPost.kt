package baka.chaomian.booru.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DanbooruPost(
    @JsonProperty("id") val id: Long,
    @JsonProperty("preview_file_url") val previewUrl : String?, // preview
    @JsonProperty("file_url") val originalUrl : String?, // original image
    @JsonProperty("large_file_url") val largeUrl : String? // compressed image
)
