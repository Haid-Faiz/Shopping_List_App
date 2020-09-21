package OnBoard

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.shoppinglist.R

class FirstFrag : Fragment() {

    private lateinit var nextText: TextView
    private lateinit var onClickFirstFrag: OnClickFirstFrag
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextText = view.findViewById(R.id.next0ID)
        nextText.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                onClickFirstFrag.onClickNext()
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onClickFirstFrag = context as OnClickFirstFrag
    }

    interface OnClickFirstFrag{
        fun onClickNext()
    }
}