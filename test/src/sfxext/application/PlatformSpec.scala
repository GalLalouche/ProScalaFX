package sfxext.application

import java.lang.reflect.Modifier
import javafx.{application => jfxa}
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import scalafx.testutil.RunOnApplicationThread


@RunWith(classOf[JUnitRunner])
class PlatformSpec extends FlatSpec with ShouldMatchers with RunOnApplicationThread {

  "Platform" should "declare all public static methods of javafx.application.Platform" in {
    val javaMethods = classOf[jfxa.Platform].getMethods
    val scalaMethods = Platform.getClass.getMethods
    for (jm <- javaMethods if Modifier.isPublic(jm.getModifiers) && Modifier.isStatic(jm.getModifiers)) {
      val found = scalaMethods.exists(
        sm => {
          def firstToUpper(s: String) = s.head.toUpper + s.tail

          val javaName = jm.getName
          val scalaName = sm.getName
          scalaName == javaName ||
            "is" + firstToUpper(scalaName) == javaName ||
            "get" + firstToUpper(scalaName) == javaName ||
            "set" + firstToUpper(scalaName) == javaName
        }
      )

      assert(found, "Declares equivalent of `" + jm.getName + "`")
    }
  }


  it should "support isFxApplicationThread" in {
    Platform.isFxApplicationThread should equal(jfxa.Platform.isFxApplicationThread)
  }


  it should "support implicitExit read/write" in {
    val oldImplicitExit = jfxa.Platform.isImplicitExit
    val newImplicitExit = !oldImplicitExit

    Platform.implicitExit should equal(oldImplicitExit)

    jfxa.Platform.setImplicitExit(newImplicitExit)
    Platform.implicitExit should equal(newImplicitExit)

    Platform.implicitExit = oldImplicitExit
    Platform.implicitExit should equal(oldImplicitExit)
    jfxa.Platform.isImplicitExit should equal(oldImplicitExit)
  }


  it should "support isSupported(ConditionalFeature)" in {
    // Check values for each ConditionalFeature
    for (cf <- jfxa.ConditionalFeature.values()) {
      Platform.isSupported(cf) should equal(jfxa.Platform.isSupported(cf))
    }
  }


  it should "support runLater(Runnable)" in {
    hasMethodWithSingleArgument("runLater", classOf[java.lang.Runnable]) should equal(true)
  }


  it should "support runLater(op: => Unit)" in {
    hasMethodWithSingleArgument("runLater", classOf[() => Unit]) should equal(true)
  }


  /** Check if Platform has a `method` with exactly one parameter of a given `parameterType`. */
  def hasMethodWithSingleArgument(method: String, parameterType: Class[_]): Boolean = {
    val methods = Platform.getClass.getMethods.filter(m => m.getName == method)
    methods.exists(m => {
      val parameterTypes = m.getParameterTypes
      parameterTypes.length == 1 && parameterTypes(0) == parameterType
    })
  }
}