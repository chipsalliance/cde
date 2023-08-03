import mill._
import scalalib._

trait CDEModule
  extends ScalaModule

trait CDETestModule
  extends TestModule
    with ScalaModule
    with TestModule.Utest {

  def cdeModule: CDEModule

  def utestIvy: Dep

  override def moduleDeps = super.moduleDeps ++ Some(cdeModule)

  override def ivyDeps = T(
    super.ivyDeps() ++ Agg(
      utestIvy
    )
  )

  override def defaultCommandName() = "test"
}
