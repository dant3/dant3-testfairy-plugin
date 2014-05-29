package testfairy

import hudson.model._

case class TestFairyBuildAction(iconFileName:String, displayName:String,urlName:String) extends ProminentProjectAction {
    override def getIconFileName = iconFileName
    override def getDisplayName = displayName
    override def getUrlName = urlName
}


object TestFairyBuildAction {
    def apply(action: Action): TestFairyBuildAction =
        new TestFairyBuildAction(action.getIconFileName, action.getDisplayName, action.getUrlName)
}
