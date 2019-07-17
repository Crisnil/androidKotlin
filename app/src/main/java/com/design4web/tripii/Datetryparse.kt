package com.design4web.tripii

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class Datetryparse{

    fun tryParse(dateString:String) :Date?{
        val formatStrings = Arrays.asList("dd-MM-yyyy", "MM/dd/yyyy")

        for (formatString in formatStrings) {
            try {
                return SimpleDateFormat(formatString).parse(dateString)
            } catch (e: ParseException) {
            }

        }

        return null
    }
}