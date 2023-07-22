package com.idiotleon.demo.demolookaheadapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadLayout
import androidx.compose.ui.layout.LookaheadLayoutScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

private const val TAG = "demoLookaheadApplication"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LookaheadLayoutCoordinatesSample()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LookaheadLayoutCoordinatesSample() {
    val colors = listOf(
        Color(0xffff6f69),
        Color(0xffffcc5c),
        Color(0xff264653),
        Color(0xff2a9d84)
    )

    val item0 = remember {
        movableContentWithReceiverOf<LookaheadLayoutScope> {
            Box(
                Modifier
                    .padding(15.dp)
                    .size(100.dp, 80.dp)
                    .animatePlacement(this)
                    .background(colors[0], RoundedCornerShape(20))
            )
        }
    }

    val item1 = remember {
        movableContentWithReceiverOf<LookaheadLayoutScope> {
            Box(
                Modifier
                    .padding(15.dp)
                    .size(100.dp, 80.dp)
                    .animatePlacement(this)
                    .background(colors[1], RoundedCornerShape(20))
            )
        }
    }

    val item2 = remember {
        movableContentWithReceiverOf<LookaheadLayoutScope> {
            Box(
                Modifier
                    .padding(15.dp)
                    .size(100.dp, 80.dp)
                    .animatePlacement(this)
                    .background(colors[2], RoundedCornerShape(20))
            )
        }
    }

    val item3 = remember {
        movableContentWithReceiverOf<LookaheadLayoutScope> {
            Box(
                Modifier
                    .padding(15.dp)
                    .size(100.dp, 80.dp)
                    .animatePlacement(this)
                    .background(colors[3], RoundedCornerShape(20))
            )
        }
    }

    var isInColumn by remember { mutableStateOf(true) }
    LookaheadLayout(
        content = {
            if (isInColumn) {
                Column {
                    item0()
                    item1()
                    Row {
                        item2()
                        item3()
                    }
                }
            } else {
                Row {
                    Row {
                        item0()
                        item1()
                    }
                    item2()
                    item3()
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .clickable { isInColumn = !isInColumn }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val maxWidth: Int = placeables.maxOf { it.width }
        val maxHeight = placeables.maxOf { it.height }
        layout(maxWidth, maxHeight) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.animatePlacement(lookaheadScope: LookaheadLayoutScope) = composed {
    var offsetAnimation by remember { mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null) }
    var placementOffset by remember { mutableStateOf(IntOffset.Zero) }
    var targetOffset by remember { mutableStateOf<IntOffset?>(null) }
    LaunchedEffect(Unit) {
        snapshotFlow { targetOffset }.collect { target ->
            Log.d(TAG, "target: $target")
            if (target != null && target != offsetAnimation?.targetValue) {
                offsetAnimation?.run { launch { animateTo(target) } }
                if (offsetAnimation == null) {
                    offsetAnimation = Animatable(target, IntOffset.VectorConverter)
                }
            }
        }
    }
    with(lookaheadScope) {
        this@composed
            .onPlaced { lookaheadScopeCoordinates, layoutCoordinates ->
                Log.d(TAG, "lookaheadScopeCoordinates: $lookaheadScopeCoordinates")
                Log.d(TAG, "layoutCoordinates: $layoutCoordinates")

                targetOffset =
                    lookaheadScopeCoordinates
                        .localLookaheadPositionOf(layoutCoordinates)
                        .round()
                Log.d(TAG, "targetOffset: $targetOffset")

                placementOffset =
                    lookaheadScopeCoordinates
                        .localPositionOf(layoutCoordinates, Offset.Zero)
                        .round()
                Log.d(TAG, "placementOffset: $placementOffset")
            }
            .intermediateLayout { measurable, constraints, _ ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val (x, y) = offsetAnimation?.run { value - placementOffset }
                        ?: (targetOffset!! - placementOffset)
                    placeable.place(x, y)
                }
            }
    }
}