package com.myapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.myapp.utils.MIN_PASSWORD

/**
 * Campo de contraseña reutilizable (Login y Registro).
 *
 * Efecto visual:
 *  - ROJO  mientras la contraseña tenga menos de MIN_PASSWORD caracteres
 *  - VERDE cuando alcanza (o supera) MIN_PASSWORD caracteres
 *
 * El cambio de color es animado (animateColorAsState): en vez de saltar de un
 * color a otro, el borde y la etiqueta se van "mezclando" durante 300 ms.
 *
 * Es un composable SIN estado propio ("stateless"): recibe el texto (value) y avisa
 * los cambios (onValueChange). Quien lo usa decide dónde vive ese estado
 * (en un ViewModel, en un remember, etc.). Esto se llama "state hoisting".
 */
@Composable
fun CampoPassword(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Contraseña"
) {
    val esValida = value.length >= MIN_PASSWORD

    // Verde más claro en modo oscuro para que se lea bien sobre fondo oscuro
    val verde = if (isSystemInDarkTheme()) Color(0xFF66BB6A) else Color(0xFF2E7D32)
    val rojo = MaterialTheme.colorScheme.error

    // El color "objetivo" cambia según la validez; animateColorAsState lo transiciona.
    val colorActual by animateColorAsState(
        targetValue = if (esValida) verde else rojo,
        animationSpec = tween(durationMillis = 300),
        label = "colorPassword"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        // Contador: da retroalimentación textual además del color (accesibilidad:
        // no depender solo del color para comunicar el estado).
        supportingText = {
            Text(
                if (esValida) "${value.length}/$MIN_PASSWORD caracteres ✓"
                else "${value.length}/$MIN_PASSWORD caracteres"
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorActual,
            unfocusedBorderColor = colorActual,
            focusedLabelColor = colorActual,
            unfocusedLabelColor = colorActual,
            cursorColor = colorActual,
            focusedSupportingTextColor = colorActual,
            unfocusedSupportingTextColor = colorActual
        ),
        modifier = modifier
    )
}
