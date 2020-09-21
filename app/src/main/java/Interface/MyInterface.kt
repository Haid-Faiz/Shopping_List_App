package Interface

import Model.ListData

interface MyInterface {
    fun onItemClick(myData: ListData, position : Int)
}