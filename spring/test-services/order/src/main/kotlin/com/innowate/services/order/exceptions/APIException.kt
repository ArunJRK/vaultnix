package com.innowate.services.order.exceptions

class APIException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)
}