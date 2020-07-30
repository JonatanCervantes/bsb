package cervantes.jonatan.pruebahorario.utilidades


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import cervantes.jonatan.pruebahorario.R
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.dialog_loading.*
import kotlinx.coroutines.*

class LoadingDialog() : DialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.dialog_loading, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.isCancelable = false
    }

    fun changeAnimationLaunch(completado: Boolean) = CoroutineScope(Dispatchers.Main).launch {
        changeAnimation(completado)
    }

    fun changeAnimation(completado: Boolean) {
        this.isCancelable = true
        if(completado) {
            val animacion = view!!.findViewById<LottieAnimationView>(R.id.icon_loadingAnimation)
            animacion.pauseAnimation()
            animacion.setAnimation(R.raw.mcheck)
            animacion.playAnimation()
            tv_animacion.text = resources.getString(R.string.operacion_exitosa)
        } else {
            val animacion = view!!.findViewById<LottieAnimationView>(R.id.icon_loadingAnimation)
            animacion.pauseAnimation()
            animacion.setAnimation(R.raw.errorcross)
            animacion.playAnimation()
            tv_animacion.text = resources.getString(R.string.operacion_fallida)
        }
    }

    fun dismissDialog() = CoroutineScope(Dispatchers.Main).launch  {
        dialog?.dismiss()
    }



}