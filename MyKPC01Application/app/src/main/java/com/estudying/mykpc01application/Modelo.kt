package com.estudying.mykpc01application

data class RequestData(val data: List<Float>, val data2: IntArray)
data class ResponseData(val prediction: List<Int>)