package com.wotriders

import groovy.json.JsonSlurper

class HttpClient {
    def connectTimeout = 500
    def readTimeout = 60000 // default to one minute
    def log = [ error: { System.err.println(it) } ]
    String username 
    String password
    boolean followRedirects = false

    static class HttpResponse {
        Map headers
        Integer code
        String message
        Exception exception
        String body
        
        Object getJson() {
            try {
                return new JsonSlurper().parseText(body)
            } catch(Exception e) {
                log.error("Cannot parse body as json. ${e.message}")
            }
            return null
        }
    }
    
    HttpResponse get(String url) {
        def connection = buildConnection(url, "GET")
        connection.connect()
        return buildReturn(connection)
    }

    HttpResponse post(String url, String json) {
        def connection = buildConnectionWithBody(url, "POST", json)
        return buildReturn(connection)
    }

    HttpResponse postForm(String url, String urlEncoded) {
        def connection = buildConnectionWithBody(url, "POST", urlEncoded, "application/x-www-form-urlencoded", "*/*")
        return buildReturn(connection)
    }

    HttpResponse put(String url, String json) {
        def connection = buildConnectionWithBody(url, "PUT", json)
        connection.connect()
        return buildReturn(connection)
    }
    
    HttpResponse putForm(String url, String urlEncoded) {
        def connection = buildConnectionWithBody(url, "PUT", urlEncoded, "application/x-www-form-urlencoded", "*/*")
        return buildReturn(connection)
    }

    HttpResponse delete(String url) {
        def connection = buildConnection(url, "DELETE")
        connection.connect()
        return buildReturn(connection)
    }

    private HttpResponse buildReturn(HttpURLConnection connection) {
        def headers = [:]
        def body
        def exception

        // For 400, 500 error an IOException is thrown.
        // So we catch these and
        // put it in the returned map in case needed.
        try {
            body = connection.content?.text?.toString()?.trim()
        } catch (IOException ioe) {
            body = connection.errorStream?.text?.toString()?.trim()
            exception = ioe
            log.error("connecting to url ${connection.url} failed: ${ioe.message}")
        }

        // Build headers manually to avoid having single
        // item collection returned by header fields
        for (int i = 0;; i++) {
            def key = connection?.getHeaderFieldKey(i)
            def field = connection?.getHeaderField(i)
            headers[key] = field
            if (key == null && field == null) {
                break;
            }
        }

        return new HttpResponse(
                headers: headers,
                code: connection.responseCode,
                message: connection.responseMessage,
                body: body,
                exception: exception
        )
    }

    private HttpURLConnection buildConnection(url, method, contentType = "application/json", accept = "application/json") {
        HttpURLConnection connection = new URL(url).openConnection()
        if(username && password) {
            String encoding = new sun.misc.BASE64Encoder().encode("$username:$password".toString().bytes)
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }
        connection.instanceFollowRedirects = followRedirects
        connection.connectTimeout = connectTimeout
        connection.readTimeout = readTimeout
        connection.setRequestMethod(method)
        connection.setRequestProperty("Content-Type", contentType)
        connection.setRequestProperty("Accept", accept)
        return connection
    }

    private HttpURLConnection buildConnectionWithBody(url, method, body, contentType = "application/json", accept = "application/json") {
        HttpURLConnection connection = buildConnection(url, method, contentType, accept)
        if(username && password) {
            String encoding = new sun.misc.BASE64Encoder().encode("$username:$password".toString().bytes)
            connection.setRequestProperty("Authorization", "Basic " + encoding);
        }
        connection.doOutput = true
        connection.connect()
        Writer writer = new OutputStreamWriter(connection.outputStream)
        writer.write(body)
        writer.flush()
        writer.close()
        return connection
    }
}
