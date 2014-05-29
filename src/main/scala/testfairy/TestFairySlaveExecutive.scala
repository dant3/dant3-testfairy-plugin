package testfairy

import testfairy.TestFairySlaveExecutive.Parameters
import hudson.model.BuildListener
import testfairy.TestFairy.Response
import hudson.remoting.Callable

class TestFairySlaveExecutive(workspace:String, uploadRequestParameters:Parameters, listener:BuildListener)
                             extends Callable[TestFairy.Response, TestFairy.UploadError] {
    override def call(): Response = ???
}


object TestFairySlaveExecutive {
    case class Parameters(apiKey: String, apkFilesPath: String, comment: String)
}
