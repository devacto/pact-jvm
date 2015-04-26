package au.com.dius.pact.provider.groovysupport

import au.com.dius.pact.model.Request
import groovy.json.JsonBuilder
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.client.methods.HttpOptions
import org.apache.http.client.methods.HttpPatch
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpTrace
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import scala.collection.JavaConverters$

class ProviderClient {

    Request request
    def provider

    def makeRequest() {
        CloseableHttpClient httpclient = newClient()
        HttpRequest method = newRequest(request)

        if (request.headers().defined) {
            JavaConverters$.MODULE$.mapAsJavaMapConverter(request.headers().get()).asJava().each { key, value ->
                method.addHeader(key, value)
            }

            if (!method.containsHeader('Content-Type')) {
                method.addHeader('Content-Type', 'application/json')
            }
        }

        if (request.body().defined && method instanceof HttpEntityEnclosingRequest) {
            method.setEntity(new StringEntity(request.body().get()))
        }

        if (provider.requestFilter != null) {
            if (provider.requestFilter instanceof Closure) {
                provider.requestFilter(method)
            } else {
                Binding binding = new Binding()
                binding.setVariable("request", method)
                GroovyShell shell = new GroovyShell(binding)
                shell.evaluate(provider.requestFilter as String)
            }
        }

        def response = httpclient.execute(method)
        try {
            return handleResponse(response)
        } finally {
            response.close()
        }
    }

    CloseableHttpResponse makeStateChangeRequest(String url, String state, boolean postStateInBody) {
        CloseableHttpClient httpclient = newClient()
        def urlBuilder = new URIBuilder(url)
        HttpRequest method

        if (postStateInBody) {
            method = new HttpPost(urlBuilder.build())
            method.setEntity(new StringEntity(new JsonBuilder([state: state]).toPrettyString(),
                    ContentType.APPLICATION_JSON))
        } else {
            method = new HttpPost(urlBuilder.setParameter('state', state).build())
        }

        if (provider.stateChangeRequestFilter != null) {
            if (provider.stateChangeRequestFilter instanceof Closure) {
                provider.stateChangeRequestFilter(method)
            } else {
                Binding binding = new Binding()
                binding.setVariable("request", method)
                GroovyShell shell = new GroovyShell(binding)
                shell.evaluate(provider.stateChangeRequestFilter as String)
            }
        }

        httpclient.execute(method)
    }

    def handleResponse(HttpResponse httpResponse) {
        def response = [statusCode: httpResponse.statusLine.statusCode]

        response.headers = [:]
        httpResponse.allHeaders.each { Header header ->
            response.headers[header.name] = header.value
        }

        HttpEntity entity = httpResponse.entity
        if (entity != null) {
            response.data = EntityUtils.toString(entity)
            if (entity.contentType) {
                response.contentType = ContentType.parse(entity.contentType.value)
            } else {
                response.contentType = ContentType.APPLICATION_JSON
            }
        }

        response
    }

    private newClient() {
        HttpClients.createDefault()
    }

    private HttpRequest newRequest(Request request) {
        def urlBuilder = new URIBuilder()
        urlBuilder.scheme = provider.protocol
        urlBuilder.host = provider.host
        urlBuilder.port = provider.port

        String path = ''
        if (provider.path.size() > 0) {
            path = provider.path.toString()
            if (path[-1] == '/') {
                path = path.size() > 1 ? path[0..-2] : ''
            }
        }

        path += URLDecoder.decode(request.path(), 'UTF-8')
        urlBuilder.path = path

        if (request.query().defined) {
            urlBuilder.query = request.query().get()
        }

        def url = urlBuilder.build().toString()
        switch (request.method().toLowerCase()) {
          case 'post':
              return new HttpPost(url)
          case 'put':
              return new HttpPut(url)
          case 'options':
              return new HttpOptions(url)
          case 'delete':
              return new HttpDelete(url)
          case 'head':
              return new HttpHead(url)
          case 'patch':
              return new HttpPatch(url)
          case 'trace':
              return new HttpTrace(url)
          default:
              return new HttpGet(url)
        }
  }

}
