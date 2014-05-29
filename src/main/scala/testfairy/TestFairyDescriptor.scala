package testfairy

import hudson.tasks.{Publisher, BuildStepDescriptor}
import hudson.Extension
import org.kohsuke.stapler.StaplerRequest
import net.sf.json.JSONObject
import hudson.model.{AbstractBuild, AbstractProject}


@Extension // This indicates to Jenkins that this is an implementation of an extension point.
class TestFairyDescriptor extends TestFairyDescriptorBase {
    load()

    override def configure(request: StaplerRequest, json: JSONObject) = {
        // TODO: parse params ???
        save()
        true
    }

    override def getDisplayName: String = ???
}
