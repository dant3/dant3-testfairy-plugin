package testfairy

import org.kohsuke.stapler.DataBoundConstructor
import hudson.util.Secret

case class TestFairyApiKey @DataBoundConstructor() (name: String, apiKey: Secret) {
    override def toString = s"$name: $apiKey"

    def decrypt: String = Secret.toString(apiKey)
}