package testfairy

import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.impl.client._
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import java.io.StringWriter
import org.apache.commons.io.IOUtils
import scala.util.Try
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.config.RequestConfig
import testfairy.TestFairy._
import java.net.URI


class TestFairyUploader {
    // see http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0
    System.setProperty("jsse.enableSNIExtension", "false")


    import scala.concurrent._
    import ExecutionContext.Implicits.global

    protected def uploadUri: URI = new URI("https://app.testfairy.com/api/upload/")

    def upload(request: Request, proxy: Option[TestFairyUploader.Proxy] = None): Future[Response] = future {
        val result = uploadSync(request, proxy)
        result.get
    }

    def uploadSync(request: Request, proxyParams: Option[TestFairyUploader.Proxy] = None): Try[Response] = Try {
        val httpClient = createHttpClient(proxyParams)
        val httpPost = new HttpPost(uploadUri)

        val entityBuilder = MultipartEntityBuilder.create()
        entityBuilder.addTextBody("api_key", request.apiKey)
        entityBuilder.addBinaryBody("apk_file", request.apkFile.jfile)

        httpPost.setEntity(entityBuilder.build())

        val response = httpClient.execute(httpPost)
        val resEntity = response.getEntity

        // Improved error handling.
        val statusCode = response.getStatusLine.getStatusCode
        if (statusCode != 200) {
            throw UploadError(statusCode, "HTTP response failure " + response.getEntity.toString)
        }

        val inputStream = resEntity.getContent
        val writer = new StringWriter()
        IOUtils.copy(inputStream, writer, "UTF-8")

        import spray.json._

        writer.toString.parseJson.convertTo[Response]
    }

    private[this] def createHttpClient(proxyParams: Option[TestFairyUploader.Proxy] = None): CloseableHttpClient = {
        httpClientBuilder(proxyParams).build()
    }

    protected def httpClientBuilder(proxyParams: Option[TestFairyUploader.Proxy] = None): HttpClientBuilder = {
        val builder = HttpClientBuilder.create()
        proxyParams foreach { proxy => {
            builder.setDefaultCredentialsProvider(createCredentialsProvider(proxy))
            builder.setDefaultRequestConfig(RequestConfig.custom().setProxy(proxy.host).build())
        }}
        builder
    }

    protected final def createCredentialsProvider(proxy: TestFairyUploader.Proxy): CredentialsProvider = {
        val credentialsProvider = new BasicCredentialsProvider()
        val proxyCredentials = for (user <- proxy.username) yield new UsernamePasswordCredentials(user, proxy.password.orNull)
        credentialsProvider.setCredentials(new AuthScope(proxy.host), proxyCredentials.orNull)
        credentialsProvider
    }
}

object TestFairyUploader {
    case class Proxy(hostName: String, port: Int, username: Option[String], password: Option[String]) {
        def host: HttpHost = new HttpHost(hostName, port)
    }

}
