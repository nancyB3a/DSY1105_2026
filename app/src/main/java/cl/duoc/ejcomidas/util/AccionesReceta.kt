package cl.duoc.ejcomidas.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import cl.duoc.ejcomidas.data.model.Comida

/** URL pública con el detalle de la receta (la misma que usaba la app Ionic). */
fun urlReceta(comida: Comida): String =
    "https://www.themealdb.com/meal.php?c=${comida.id}"

private fun textoReceta(comida: Comida): String = buildString {
    appendLine("¿Te gusta esta receta? ${comida.nombre}")
    appendLine()
    appendLine("Ver receta: ${urlReceta(comida)}")
    if (comida.imagenUrl.isNotBlank()) appendLine("Foto: ${comida.imagenUrl}")
    appendLine()
    append("Enviado desde Que Cocino App")
}

/**
 * "Compartir en…": abre el selector nativo de Android con todas las apps
 * que aceptan texto (WhatsApp, Telegram, Gmail, Bluetooth, Drive, etc.).
 * Reemplaza al plugin SocialSharing de Ionic.
 */
fun compartirReceta(context: Context, comida: Comida) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Receta: ${comida.nombre}")
        putExtra(Intent.EXTRA_TEXT, textoReceta(comida))
    }
    context.startActivity(Intent.createChooser(intent, "Compartir en…"))
}

/**
 * "Enviar por email": ACTION_SENDTO con "mailto:" hace que SOLO
 * aparezcan apps de correo. Se puede indicar un destinatario opcional.
 * Devuelve false si el dispositivo no tiene una app de correo configurada.
 */
fun enviarRecetaPorEmail(context: Context, comida: Comida, destinatario: String = ""): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        if (destinatario.isNotBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
        putExtra(Intent.EXTRA_SUBJECT, "Receta: ${comida.nombre}")
        putExtra(Intent.EXTRA_TEXT, textoReceta(comida))
    }
    return try {
        context.startActivity(Intent.createChooser(intent, "Enviar receta por email"))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
