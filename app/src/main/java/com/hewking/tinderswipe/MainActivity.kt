package com.hewking.tinderswipe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.hewking.tinderstack.TinderStackLayout
import kotlinx.android.synthetic.main.fragment_tinder.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_tinder)

        tinderStack.adapter = TinderCardAdapter().apply {

        }

        tinderStack.setChooseListener {
            if (it == 1) {
                Toast.makeText(this@MainActivity,"right swipe select", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity,"left swipe select", Toast.LENGTH_SHORT).show()

            }
        }
    }

    inner class TinderCardAdapter : TinderStackLayout.BaseCardAdapter{

        val datas = mutableListOf<String>("https://tuimeizi.cn/random?w=665&h=656"
            ,"https://tuimeizi.cn/random?w=665&h=656"
            ,"https://tuimeizi.cn/random?w=667&h=656"
            ,"https://tuimeizi.cn/random?w=668&h=656"
            ,"https://tuimeizi.cn/random?w=669&h=651"
            ,"https://tuimeizi.cn/random?w=669&h=652"
            ,"https://tuimeizi.cn/random?w=669&h=657"
            ,"https://tuimeizi.cn/random?w=669&h=658"
            ,"https://tuimeizi.cn/random?w=669&h=659"
            ,"https://tuimeizi.cn/random?w=669&h=669")

        private var index = 0

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun getView(): View? {
            if (index > datas.size - 1) {
                return null
            }
            val img = ImageView(this@MainActivity)
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            img.layoutParams = ViewGroup.LayoutParams(dp2px(350f),dp2px(350f))
            img.load(datas[index])
            index ++
            return img
        }
    }

    fun dp2px(dp : Float) : Int{
        return (resources?.displayMetrics?.density?.times(dp)?.plus(0.5))?.toInt()?:0
    }
}
