package com.cao.repairshop.register.repository.converter

import com.cao.repairshop.register.domain.Email
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class EmailConverter : AttributeConverter<Email, String> {

    override fun convertToDatabaseColumn(attribute: Email?): String? =
        attribute?.value

    override fun convertToEntityAttribute(dbData: String?): Email? =
        dbData?.let { Email(it) }
}
