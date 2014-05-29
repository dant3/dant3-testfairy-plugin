package testfairy

import org.specs2.mutable.Specification
import scala.reflect.io.File
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import org.specs2.matcher.ThrownExpectations
import testfairy.TestFairy.Response
import scala.util.Try
import java.net.URI
import org.mockserver.integration.ClientAndServer.startClientAndServer

class TestFairyUploaderSpec extends Specification with AfterAll with ThrownExpectations {
    val mockServer = startClientAndServer(8080)

    "Test fairy uploader" should {
        "not be able to upload fake apk" in {
            val apkFile = File.makeTemp(suffix = ".apk", dir = File("/tmp").jfile)
            val apiKey  = ???

            apkFile.writer().write("test", 0, 4).flush()

            val uploader = new TestFairyUploader
            val future = uploader.upload(TestFairy.Request(apiKey, apkFile))
            val response = Await.ready(future, Duration(1, TimeUnit.MINUTES))
            val value: Option[Try[Response]] = response.value
            value must beSome
            val Some(result) = value
            result must beFailedTry
        }
    }

    override protected def afterAll(): Unit = mockServer.stop()
}


class TestingTestFairyUploader extends TestFairyUploader {
    override def uploadUri = new URI("http://localhost:8080/api/upload")
}