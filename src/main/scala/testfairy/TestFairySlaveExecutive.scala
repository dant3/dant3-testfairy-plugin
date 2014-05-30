package testfairy

import testfairy.TestFairySlaveExecutive.Parameters
import hudson.model.BuildListener
import testfairy.TestFairy.{Request, Response}
import hudson.remoting.Callable
import scala.reflect.io.File
import hudson.Util
import scala.collection.JavaConversions
import scala.util.Try

class TestFairySlaveExecutive(workspace:String, uploadRequestParameters:Parameters, listener:BuildListener)
                             extends Callable[Seq[TestFairy.Response], TestFairy.UploadError] {
    private val uploader = new TestFairyUploader

    override def call(): Seq[Response] = {
        val apkFiles = resolveFiles( uploadRequestParameters.apkFilesPath.getOrElse(TestFairySlaveExecutive.defaultApkFilesPath) )
        apkFiles map { (apkFile: File) => uploader.uploadSync(
            Request(uploadRequestParameters.apiKey, apkFile, comment = uploadRequestParameters.comment) ).get
        }
    }

    private def resolveFiles(filePathMask: String):Seq[File] = {
        Util.createFileSet(File(workspace).jfile, filePathMask) map {
            (file: Any) => File(file.toString)
        } toSeq
    }

    type IterableLike = {
        def iterator():java.util.Iterator[_]
    }

    implicit private def toScalaIterable(iterableLike: this.IterableLike): Iterator[_] =
        JavaConversions.asScalaIterator(iterableLike.iterator())
}


object TestFairySlaveExecutive {
    val defaultApkFilesPath = "**/*.apk"
    case class Parameters(apiKey: String, apkFilesPath: Option[String], comment: Option[String])

    object Parameters {

        def apply(apiKey: String, apkFilesPath: String, comment: String):Parameters = {
            Parameters(apiKey, emptyStringToNone(apkFilesPath), emptyStringToNone(comment))
        }

        def emptyStringToNone(string: String): Option[String] = if (string.trim.isEmpty) {
            None
        } else {
            Some(string.trim)
        }
    }
}
