package testfairy

import hudson.tasks.{BuildStepMonitor, Recorder}
import hudson.model._
import hudson.Launcher
import hudson.model.AbstractProject

import collection.JavaConversions._
import scala.collection.JavaConversions

class TestFairyRecorder extends Recorder {
    override def getDescriptor: TestFairyDescriptor = super.getDescriptor.asInstanceOf[TestFairyDescriptor]

    override def perform(build: AbstractBuild[_, _], launcher: Launcher, listener: BuildListener) = {
        if (build.getResult isWorseOrEqualTo Result.FAILURE) {
            false
        } else {
            // TODO: perform uploading here, based on params
            ???
        }
    }

    override def getProjectActions(project: AbstractProject[_, _]) = {
        val okBuilds: Seq[_] = project.getBuilds filter buildOk
        val actions: Seq[TestFairyBuildAction] = (okBuilds map collectActions).flatten
        JavaConversions.asJavaCollection(actions)
    }

    override def getRequiredMonitorService: BuildStepMonitor = BuildStepMonitor.NONE


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

}
