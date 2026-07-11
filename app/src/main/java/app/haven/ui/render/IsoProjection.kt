package app.haven.ui.render

import androidx.compose.ui.geometry.Offset
import app.haven.data.world.GridCoordinate

/**
 * Projects the world's logical tile grid onto the 2.5D isometric canvas.
 *
 * A tile at (x, y) maps to screen space with the classic 2:1 iso diamond; the
 * optional z lifts a tile (bridges, elevated squares). Camera pan/zoom is
 * applied on top so the whole scene can glide and scale for the building-focus
 * transition without any layer needing to know about the camera.
 */
class IsoProjection(
    private val tileWidth: Float,
    private val tileHeight: Float,
    private val origin: Offset,
    private val cameraOffset: Offset,
    private val zoom: Float,
) {
    fun project(coord: GridCoordinate): Offset {
        val isoX = (coord.x - coord.y) * (tileWidth / 2f)
        val isoY = (coord.x + coord.y) * (tileHeight / 2f) - coord.z * tileHeight
        val world = Offset(isoX, isoY)
        return (world + cameraOffset) * zoom + origin
    }

    /** Screen-space size of one tile edge at the current zoom. */
    val scaledTileWidth: Float get() = tileWidth * zoom
    val scaledTileHeight: Float get() = tileHeight * zoom

    companion object {
        /** Raw (camera-independent) iso offset of a tile; used to compute the
         *  camera pan that centers a structure during a focus transition. */
        fun isoOf(coord: GridCoordinate, tileWidth: Float, tileHeight: Float): Offset =
            Offset(
                (coord.x - coord.y) * (tileWidth / 2f),
                (coord.x + coord.y) * (tileHeight / 2f) - coord.z * tileHeight,
            )
    }
}
