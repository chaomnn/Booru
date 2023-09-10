package baka.chaomian.booru.data

import android.os.Parcel
import android.os.Parcelable

data class Post(val id: Long,
            val previewUrl: String,
            val originalUrl: String,
            val largeUrl: String) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(previewUrl)
        dest.writeString(originalUrl)
        dest.writeString(largeUrl)
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}
