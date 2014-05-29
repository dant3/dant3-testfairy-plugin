package testfairy

import org.jvnet.localizer.Localizable

case class MisconfiguredJobException(configurationMessage: Localizable) extends Exception
