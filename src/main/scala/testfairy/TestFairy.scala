package testfairy

import spray.json.{DeserializationException, JsValue, RootJsonFormat, DefaultJsonProtocol}
import scala.reflect.io.File

object TestFairy {
    // http://docs.testfairy.com/Upload_API.html
    case class Metric private(name: String)
    object Metric extends Enumeration {
        val CPU = Metric("cpu")
        val Memory = Metric("memory")
        val Network = Metric("network")
        val PhoneSignal = Metric("phone-signal")
        val Logcat = Metric("logcat")
        val GPS = Metric("gps")
        val Battery = Metric("battery")
        val Mic = Metric("mic")
    }


    case class VideoRecording private(value: String)
    object VideoRecording extends Enumeration {
        val On = VideoRecording("on")
        val Off = VideoRecording("off")
        val Wifi = VideoRecording("wifi")
    }


    case class VideoQuality private(value: String)
    object VideoQuality extends Enumeration {
        val High = VideoQuality("high")
        val Medium = VideoQuality("medium")
        val Low = VideoQuality("low")
    }


    case class IconWatermark private(value: String)
    object IconWatermark extends Enumeration {
        def apply(on: Boolean): IconWatermark = if (on) On else Off

        val On = IconWatermark("on")
        val Off = IconWatermark("off")
    }

    case class Request(apiKey: String, apkFile: File,
                       proguardMappingFile: Option[File] = None,
                       testers: Option[Set[String]] = None,
                       metrics: Option[Set[Metric]] = None,
                       maxDuration: Option[String] = None,
                       video: Option[VideoRecording] = None,
                       videoQuality: Option[VideoQuality] = None,
                       videoRate: Option[Double] = None,
                       watermark: Option[Boolean] = None,
                       comment: Option[String] = None) extends Serializable

    case class Response(appName: String, appVersion: String, fileSize: Long, buildUrl: String,
                        inviteUrl: String, instrumentedApkUrl: String, iconUrl: String)

    object Response {
        private def isOk(status: String):Boolean = status == "ok"

        implicit val Format = new RootJsonFormat[Response] with DefaultJsonProtocol {
            val delegate = jsonFormat7(Response.apply)
            val errorDelegate = jsonFormat2(UploadError.apply)

            override def write(obj: Response): JsValue = delegate.write(obj)

            override def read(json: JsValue): Response = try {
                delegate.read(json)
            } catch {
                case e: DeserializationException => throw errorDelegate.read(json)
            }
        }
    }

    case class UploadError(code: Int, message: String) extends Exception {
        override def toString = s"TestFairy upload error - code: $code, message: $message"
    }
}
