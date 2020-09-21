package OnBoard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.shoppinglist.R

class SecondFrag : Fragment() {

    private lateinit var onClickSecondFrag: OnClickSecondFrag
    private lateinit var backText: TextView
    private lateinit var doneText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backText = view.findViewById(R.id.backID)
        doneText = view.findViewById(R.id.doneID)

        backText.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onClickSecondFrag.onClickBack()
            }
        })

        doneText.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onClickSecondFrag.onClickDone()
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onClickSecondFrag = context as OnClickSecondFrag
    }

    interface OnClickSecondFrag{
        fun onClickBack()
        fun onClickDone()
    }
}