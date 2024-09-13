package com.innowate.services.order.exceptions

class ResourceNotFoundException : RuntimeException {

    val resourceName: String
    val field: String
    var fieldName: String? = null
    var fieldId: Long? = null

    constructor() : super() {
        this.resourceName = ""
        this.field = ""
        this.fieldName = null
        this.fieldId = null
    }


    constructor(resourceName: String, field: String, fieldName: String) : super(
        String.format("%s not found with %s: %s", resourceName, field, fieldName)
    ) {
        this.resourceName = resourceName
        this.field = field
        this.fieldName = fieldName
    }

    constructor(resourceName: String, field: String, fieldId: Long) : super(
        String.format("%s not found with %s: %d", resourceName, field, fieldId)
    ) {
        this.resourceName = resourceName
        this.field = field
        this.fieldId = fieldId
    }
}
