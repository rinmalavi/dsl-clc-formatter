val props = new java.util.Properties

xml.XML.load("format.xml") \ "profile" \ "setting" foreach { setting =>
  val id = setting \ "@id" text
  val value = setting \ "@value" text

  props.setProperty(id, value)
}

val fos = new java.io.FileOutputStream("fos.xml")
props.store(fos, "hhihi")
