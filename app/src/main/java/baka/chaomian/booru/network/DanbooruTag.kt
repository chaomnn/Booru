package baka.chaomian.booru.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class DanbooruTag(
    @JsonProperty("label") val label: String,
    @JsonProperty("value") val name: String,
    @JsonProperty("category") val category: Int,
    @JsonProperty("post_count") val postCount: Int,
    @JsonProperty("antecedent") val antecedent: String?
)
