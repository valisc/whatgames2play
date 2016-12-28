package net.inkanito.whatgames2play

import co.technius.scalajs.mithril._
import org.scalajs.dom

import scala.scalajs.js


object Main extends js.JSApp {
  def main(): Unit = {
    m.mount(dom.document.getElementById("app"), MyComponent)
  }
}

object MyComponent extends Component {

  override val controller: js.Function = () => new Controller

  val view: js.Function = (ctrl: Controller) => js.Array(
    m("span", s"Hi, ${ctrl.name()}!"),
    m("input[type=text]", js.Dynamic.literal(
      oninput = m.withAttr("value", ctrl.name),
      value = ctrl.name()
    ))
  )

  class Controller {
    val name = m.prop("Name")
  }
}