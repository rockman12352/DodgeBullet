package com.csg.codeit

import com.fasterxml.jackson.module.kotlin.jsonMapper
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
@Order(1)
class Logger : Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        response as HttpServletResponse
        chain.doFilter(request, response)
        println("request on: ${request.requestURI}, body: ${String(request.inputStream.readAllBytes())}")
    }
}