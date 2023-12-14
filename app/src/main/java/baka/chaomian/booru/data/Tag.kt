package baka.chaomian.booru.data

import android.os.Parcel
import android.os.Parcelable

data class Tag(
    val label: String,
    val name: String,
    val category: Int,
    val postCount: Int,
    val antecedent: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeString(name)
        parcel.writeInt(category)
        parcel.writeInt(postCount)
        parcel.writeString(antecedent)
    }

    companion object CREATOR : Parcelable.Creator<Tag> {
        override fun createFromParcel(parcel: Parcel): Tag {
            return Tag(parcel)
        }

        override fun newArray(size: Int): Array<Tag?> {
            return arrayOfNulls(size)
        }
    }
}
