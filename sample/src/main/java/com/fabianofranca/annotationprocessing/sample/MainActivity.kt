package com.fabianofranca.annotationprocessing.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fabianofranca.annotationprocessing.R
import com.fabianofranca.annotations.GenerateClassProvider
import kotlinx.android.synthetic.main.activity_main.*

@GenerateClassProvider
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textHello.text = MainActivityClassProvider().classInstance.simpleName
    }
}
