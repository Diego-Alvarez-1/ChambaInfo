package com.chambainfo.app.data.model

import com.google.gson.annotations.SerializedName

data class ReniecResponse(
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("first_last_name")
    val firstLastName: String,
    @SerializedName("second_last_name")
    val secondLastName: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("document_number")
    val documentNumber: String
)