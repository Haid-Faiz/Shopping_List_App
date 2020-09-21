package Model

data class ListData(var item: String, var amount: Int, var note: String, var time: String, var id: String) {

    constructor() : this(item = "", amount = 0, note = "", time = "", id = "")
}