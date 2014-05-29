package testfairy

import hudson.Extension
import org.kohsuke.stapler.StaplerRequest
import net.sf.json.JSONObject
import scala.collection.JavaConversions


@Extension // This indicates to Jenkins that this is an implementation of an extension point.
class TestFairyDescriptor extends TestFairyDescriptorBase {
    private var _keys: Seq[TestFairyApiKey] = Nil

    load()

    override def configure(request: StaplerRequest, json: JSONObject) = {
        _keys = JavaConversions.asScalaBuffer(request.bindParametersToList(classOf[TestFairyApiKey], "key."))
        save()
        true
    }

    override def getDisplayName: String = Messages.TestFairyRecorder_UploadLinkText()

    def getKeys: java.util.List[TestFairyApiKey] = JavaConversions.seqAsJavaList(keys)
    def keys: Seq[TestFairyApiKey] = _keys
}
