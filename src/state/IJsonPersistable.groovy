package state

// Objects that can be persisted to a json object in our homebrewed persistence mechanism
// This is probably mostly pointless but you never know
interface IJsonPersistable {
  def asJsonObj()
}
