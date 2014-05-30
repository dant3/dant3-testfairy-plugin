package testfairy

import hudson.tasks.{BuildStepMonitor, Recorder}
import hudson.model._
import hudson.{EnvVars, Launcher}
import hudson.model.AbstractProject

import collection.JavaConversions._
import scala.collection.JavaConversions
import org.kohsuke.stapler.DataBoundConstructor
import hudson.scm.ChangeLogSet
import scala.util.Success

class TestFairyRecorder @DataBoundConstructor() (apiKeyName: String,
                                                 apkFilesPath: String,
                                                 proguardMappingPath: String,
                                                 comment: String,
                                                 appendChangeLog: Boolean,
                                                 testers: String,
                                                 debug: Boolean) extends Recorder {
    def getApiKeyName:String = apiKeyName
    def getApkFilesPath:String = apkFilesPath
    def getProguardMappingPath:String = proguardMappingPath
    def getComment:String = comment
    def getAppendChangeLog:Boolean = appendChangeLog
    def getTesters:String = testers
    def getDebug:Boolean = debug

    override def getDescriptor: TestFairyDescriptor = super.getDescriptor.asInstanceOf[TestFairyDescriptor]

    override def perform(build: AbstractBuild[_, _], launcher: Launcher, listener: BuildListener) = {
        if (build.getResult isWorseOrEqualTo Result.FAILURE) {
            false
        } else {
            val environment = build.getEnvironment(listener)
            val workspace = environment.expand(Jenkins.workspace)

            try {
                val uploadRequestParameters = prepareUploadRequests(environment, build)
                val slave = new TestFairySlaveExecutive(workspace, uploadRequestParameters, listener)

                val testFairyUploadResult = launcher.getChannel.call(slave)
                populateTestFairyApplicationLink(build, listener, testFairyUploadResult)
                true
            } catch {
                case mje: MisconfiguredJobException =>
                    listener.getLogger.println(mje.configurationMessage)
                    false
                case uploadError: TestFairy.UploadError =>
                    listener.getLogger.println(uploadError.toString)
                    false
            }
        }
    }

    override def getProjectActions(project: AbstractProject[_, _]) = {
        val okBuilds: Seq[_] = project.getBuilds filter buildOk
        val actions: Seq[TestFairyBuildAction] = (okBuilds map collectActions).flatten
        JavaConversions.asJavaCollection(actions)
    }

    override def getRequiredMonitorService: BuildStepMonitor = BuildStepMonitor.NONE


    private def prepareUploadRequests(environment: EnvVars, build: AbstractBuild[_, _]): TestFairySlaveExecutive.Parameters = {
        val apkFilesPath = environment.expand(this.apkFilesPath.trim)
        val apiKey = getApiKey(apiKeyName) map (_.decrypt)
        val comment = getBuildComment(environment.expand(this.comment), build.getChangeSet)

        apiKey match {
            case Some(key) => TestFairySlaveExecutive.Parameters(key, apkFilesPath, comment)
            case _ => throw new MisconfiguredJobException(Messages._TestFairyRecorder_ApiKeyNotFound(apiKeyName))
        }
    }

    private def getApiKey(keyName: String):Option[TestFairyApiKey] = {
        getDescriptor.getKeys find ( _.name == keyName )
    }

    private def getBuildComment(userEnteredComment: String, changeSet: ChangeLogSet[_]):String = {
        if (appendChangeLog) {
            s"""$userEnteredComment
               |
               |${createBuildCommentFromChangeLog(changeSet)}
             """.stripMargin
        } else {
            userEnteredComment
        }
    }

    private def createBuildCommentFromChangeLog(changeSet: ChangeLogSet[_]):String = {
        val lineNumbers = 1 to changeSet.size
        val changeLogLines = lineNumbers zip changeSet map {
            case (lineNumber: Int, change: String) => s"$lineNumber. $change"
        }
        val changeLog = createChangeLogTitle(changeSet) +: changeLogLines
        changeLog.reduceLeft { _ + "\n" + _ }
    }

    private def createChangeLogTitle(changeLog: ChangeLogSet[_]): String = if (changeLog.isEmptySet) {
        Messages.TestFairyRecorder_EmptyChangeSet()
    } else {
        Messages.TestFairyRecorder_Changelog()
    }

    private def buildOk(build: Any): Boolean = build match {
        case realBuild: AbstractBuild[_, _] => buildOk(realBuild)
        case _ => false
    }

    private def buildOk(build: AbstractBuild[_, _]): Boolean = build.getResult match {
        case null => false
        case result: Result => result isBetterOrEqualTo Result.SUCCESS
    }


    private def collectActions(build: Any): List[TestFairyBuildAction] = build match {
        case build:AbstractBuild[_, _] => collectActions(build)
        case _ => Nil
    }

    private def collectActions(build: AbstractBuild[_, _]): List[TestFairyBuildAction] = {
        build.getActions(classOf[TestFairyBuildAction]) match {
            case null => Nil
            case list: List[TestFairyBuildAction] if !list.isEmpty => list
            case _ => Nil
        }
    }

    private def populateTestFairyApplicationLink(build: AbstractBuild[_, _],
                                                 listener: BuildListener, uploadResult: Seq[TestFairy.Response]) = {

    }
}
